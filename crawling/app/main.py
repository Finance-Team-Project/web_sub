from __future__ import annotations

import re
import sqlite3
from pathlib import Path
from typing import Dict, List, Optional

import requests
from bs4 import BeautifulSoup
from fastapi import FastAPI, Query
from konlpy.tag import Okt
from pydantic import BaseModel
from sklearn.feature_extraction.text import TfidfVectorizer

app = FastAPI()

economy_press_list = ["한국경제", "매일경제", "서울경제", "머니투데이", "파이낸셜뉴스"]
particles = {
    "은",
    "는",
    "이",
    "가",
    "을",
    "를",
    "에",
    "에서",
    "와",
    "과",
    "도",
    "의",
    "한",
    "로",
    "으로",
    "하고",
    "및",
    "등",
    "까지",
    "부터",
    "만",
    "보다",
    "처럼",
    "같이",
    "께서",
}

stopwords = {"기자", "발표", "관련", "이번", "이날", "계획", "통해", "등", "및", "대한"}
okt = Okt()

base_dir = Path(__file__).resolve().parent.parent
DB_PATH = base_dir / "economics_terms.db"
SPRING_ENDPOINT = "http://localhost:8080/news/upload"

NEWS_CACHE: Dict[int, Dict[str, object]] = {}


def parse_time_to_minutes(time_str: str) -> int:
    """Convert relative time strings (e.g., '5분전') to minutes."""
    if "분전" in time_str:
        return int(re.search(r"(\\d+)", time_str).group(1))
    if "시간전" in time_str:
        return int(re.search(r"(\\d+)", time_str).group(1)) * 60
    return 9999


def get_naver_economy_news_urls_from_list(
    pages: int = 10,
    allowed_press: Optional[List[str]] = None,
    max_minutes: int = 100,
) -> List[str]:
    """Collect recent economy news URLs from Naver news list pages."""
    allowed_press = allowed_press or economy_press_list
    base_url = "https://news.naver.com/main/list.naver"
    headers = {"User-Agent": "Mozilla/5.0"}

    all_results: List[str] = []
    seen: set[str] = set()

    for page in range(1, pages + 1):
        params = {"mode": "LSD", "mid": "shm", "sid1": "101", "page": str(page)}
        response = requests.get(base_url, headers=headers, params=params, timeout=5)
        if response.status_code != 200:
            continue

        soup = BeautifulSoup(response.text, "html.parser")
        news_blocks = soup.select("ul.type06_headline li") + soup.select("ul.type06 li")

        for block in news_blocks:
            a_tag = block.select_one("dt > a")
            press_tag = block.select_one("span.writing")
            time_tag = block.select_one("dd > span.date")
            if not a_tag or not press_tag or not time_tag:
                continue

            href = a_tag.get("href")
            press_name = press_tag.get_text(strip=True)
            time_text = time_tag.get_text(strip=True)

            minutes = parse_time_to_minutes(time_text)
            if minutes <= max_minutes and any(name in press_name for name in allowed_press):
                if href not in seen:
                    seen.add(href)
                    all_results.append(href)

    return all_results


def get_news_text(url: str) -> str:
    headers = {
        "User-Agent": (
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        )
    }
    response = requests.get(url, headers=headers, timeout=5)
    if response.status_code != 200:
        return ""

    soup = BeautifulSoup(response.text, "html.parser")
    article_body = soup.select_one("div#newsct_article")
    if article_body:
        return article_body.get_text(strip=True, separator="\n")
    return ""


def get_title_and_image(url: str) -> tuple[str, Optional[str]]:
    headers = {
        "User-Agent": (
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        )
    }
    response = requests.get(url, headers=headers, timeout=5)
    soup = BeautifulSoup(response.text, "html.parser")

    title_tag = soup.select_one("h2#title_area span")
    title = title_tag.text.strip() if title_tag else "제목 없음"

    image_tag = (
        soup.select_one("figure img")
        or soup.select_one("span.end_photo_org img")
        or soup.select_one("div#newsct_article img")
    )

    if image_tag and "src" in image_tag.attrs:
        return title, image_tag["src"]

    meta_tag = soup.find("meta", property="og:image")
    return title, (meta_tag["content"] if meta_tag else None)


def extract_news_metadata(url: str) -> tuple[str, Optional[str], str, str]:
    headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"}
    response = requests.get(url, headers=headers, timeout=5)
    if response.status_code != 200:
        return "제목 없음", None, "언론사 미확인", "날짜 미확인"

    soup = BeautifulSoup(response.text, "html.parser")

    title_tag = soup.select_one("h2#title_area span")
    title = title_tag.text.strip() if title_tag else "제목 없음"

    image_tag = (
        soup.select_one("figure img")
        or soup.select_one("span.end_photo_org img")
        or soup.select_one("div#newsct_article img")
    )
    if image_tag and "src" in image_tag.attrs:
        image_url = image_tag["src"]
    else:
        meta_tag = soup.find("meta", property="og:image")
        image_url = meta_tag["content"] if meta_tag else None

    press_tag = soup.select_one("a.media_end_head_top_logo img")
    press = press_tag.get("alt").strip() if press_tag and press_tag.has_attr("alt") else "언론사 미확인"

    time_tag = soup.select_one("span.media_end_head_info_datestamp_time") or soup.select_one("span.t11")
    date = time_tag.get_text(strip=True) if time_tag else "날짜 미확인"

    return title, image_url, press, date


def remove_particles(word: str) -> str:
    pattern = r"(" + "|".join(particles) + r")$"
    return re.sub(pattern, "", word)


def extract_words_okt(text: str) -> List[str]:
    nouns = okt.phrases(text)
    return [remove_particles(n).strip() for n in nouns if n.strip()]


def print_words_in_rows(words: List[str], words_per_row: int = 10) -> None:
    for i in range(0, len(words), words_per_row):
        print(", ".join(words[i : i + words_per_row]))


def find_description_from_db(conn: sqlite3.Connection, term_input: str) -> tuple[Optional[str], Optional[str], Optional[str]]:
    cursor = conn.cursor()
    query = "SELECT * FROM terms WHERE term = ?"
    cursor.execute(query, (term_input,))
    result = cursor.fetchone()
    if result:
        desc1 = result[2] if result[2] else ""
        desc2 = result[3] if result[3] else ""
        desc3 = result[4] if result[4] else ""
        return desc1, desc2, desc3
    return None, None, None


def extract_and_explain(url: str, db_path: Path = DB_PATH) -> List[Dict[str, str]]:
    text = get_news_text(url)
    words_konlpy = extract_words_okt(text)
    words_konlpy = list(dict.fromkeys(words_konlpy))

    if not db_path.exists():
        return []

    conn = sqlite3.connect(db_path)
    terms: List[Dict[str, str]] = []

    try:
        for word in words_konlpy:
            desc1, desc2, desc3 = find_description_from_db(conn, word)
            if any([desc1, desc2, desc3]):
                terms.append({"term": word, "desc1": desc1 or "", "desc2": desc2 or "", "desc3": desc3 or ""})
    finally:
        conn.close()

    return terms


class NewsCacheItem(BaseModel):
    news_id: int
    title: str
    url: str
    keywords: List[str]


class RecommendRequest(BaseModel):
    user_id: int
    clicked_news_ids: List[int]


class Recommendation(BaseModel):
    news_id: int
    title: str
    url: str
    matched_keywords: List[str]


class RecommendResponse(BaseModel):
    user_id: int
    recommendations: List[Recommendation]


def extract_nouns(text: str) -> str:
    nouns = okt.nouns(text)
    return " ".join([n for n in nouns if len(n) > 1 and n not in stopwords])


def get_top_keywords(tfidf_matrix, feature_names, top_n: int = 5, min_score: float = 0.05) -> List[List[str]]:
    result: List[List[str]] = []
    for row in tfidf_matrix:
        row_array = row.toarray().flatten()
        top_indices = row_array.argsort()[::-1]
        keywords: List[str] = []
        for idx in top_indices:
            if row_array[idx] < min_score:
                continue
            keywords.append(feature_names[idx])
            if len(keywords) >= top_n:
                break
        result.append(keywords)
    return result


@app.post("/crawl_auto/")
def crawl_auto(pages: int = Query(1, description="가져올 뉴스 페이지 수")) -> Dict[str, object]:
    urls = get_naver_economy_news_urls_from_list(pages=pages)
    news_data: List[Dict[str, object]] = []

    for url in urls:
        title, image_url, press, date = extract_news_metadata(url)
        content = get_news_text(url)
        if not content:
            continue
        news_data.append(
            {
                "url": url,
                "title": title,
                "imageUrl": image_url,
                "content": content,
                "press": press,
                "date": date,
            }
        )

    docs = [extract_nouns(n["content"]) for n in news_data]
    vectorizer = TfidfVectorizer()
    tfidf_matrix = vectorizer.fit_transform(docs) if docs else None
    feature_names = vectorizer.get_feature_names_out() if tfidf_matrix is not None else []
    keywords_list = get_top_keywords(tfidf_matrix, feature_names) if tfidf_matrix is not None else []

    results: List[Dict[str, object]] = []

    for i, item in enumerate(news_data):
        url = item["url"]
        title = item["title"]
        content = item["content"]
        image_url = item["imageUrl"]
        terms = extract_and_explain(url)
        keywords = keywords_list[i] if i < len(keywords_list) else []

        data = {
            "url": url,
            "title": title,
            "content": content,
            "imageUrl": image_url,
            "terms": terms,
            "keywords": keywords,
            "press": item["press"],
            "date": item["date"],
        }

        try:
            response = requests.post(SPRING_ENDPOINT, json=data, timeout=5)
            results.append(
                {
                    "url": url,
                    "status": response.status_code,
                    "spring_response": response.text,
                    "title": title,
                    "imageUrl": image_url,
                    "press": item["press"],
                    "date": item["date"],
                    "terms_found": [t["term"] for t in terms],
                    "keywords": keywords,
                }
            )
        except Exception as exc:  # pylint: disable=broad-except
            results.append({"url": url, "error": str(exc), "message": "Spring 서버로 전송 실패"})

    return {"results": results}


@app.post("/cache/update")
def update_cache(item: NewsCacheItem) -> Dict[str, object]:
    NEWS_CACHE[item.news_id] = {"title": item.title, "url": item.url, "keywords": item.keywords}
    return {"message": f"뉴스 {item.news_id} 캐시에 저장 완료"}


@app.post("/recommend", response_model=RecommendResponse)
def recommend(data: RecommendRequest) -> RecommendResponse:
    user_id = data.user_id
    clicked_ids = set(data.clicked_news_ids)

    user_keywords: set[str] = set()
    for news_id in clicked_ids:
        article = NEWS_CACHE.get(news_id)
        if article:
            user_keywords.update(article["keywords"])

    scored: List[tuple[int, List[str], int, Dict[str, object]]] = []
    for news_id, article in NEWS_CACHE.items():
        if news_id in clicked_ids:
            continue
        matched = list(user_keywords & set(article["keywords"]))
        score = len(matched)
        if score > 0:
            scored.append((score, matched, news_id, article))

    scored.sort(reverse=True, key=lambda item: item[0])
    top_articles = scored[:20]

    recommendations = [
        Recommendation(news_id=news_id, title=article["title"], url=article["url"], matched_keywords=matched)
        for _, matched, news_id, article in top_articles
    ]

    return RecommendResponse(user_id=user_id, recommendations=recommendations)


@app.get("/cache/list")
def list_cached_news() -> Dict[str, List[int]]:
    return {"cached_news_ids": list(NEWS_CACHE.keys())}


__all__ = [
    "app",
    "crawl_auto",
    "update_cache",
    "recommend",
    "list_cached_news",
]
