#!/usr/bin/env python
# coding: utf-8

# ## Default Setting

# In[15]:


import os
from pathlib import Path
import requests
from bs4 import BeautifulSoup
import re
from konlpy.tag import Okt
import sqlite3
from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Optional
from collections import OrderedDict


# 🔹 제외할 영어 관사(articles) 목록
ARTICLES = {"a", "an", "the"}

# 🔹 한글 조사 목록 (단어 끝에 붙는 경우 의미 분석 방해 → 제거 대상)
PARTICLES = {
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


# ## URL Parsing

# In[16]:




# ✅ 선택한 경제 언론사 목록
ECONOMY_PRESS_LIST = ["한국경제", "매일경제", "서울경제", "머니투데이", "파이낸셜뉴스"]

def parse_time_to_minutes(time_str):
    """
    '1분전', '2시간전', '어제' 등 문자열을 분 단위로 변환
    5분 기준 필터링에만 사용
    """
    if "분전" in time_str:
        return int(re.search(r'(\d+)', time_str).group(1))          
    elif "시간전" in time_str:
        return int(re.search(r'(\d+)', time_str).group(1)) * 60
    else:
        return 9999  # '어제', '3일전' 등은 아주 큰 값으로 처리

def get_naver_economy_news_urls_from_list(pages=10, allowed_press=ECONOMY_PRESS_LIST, max_minutes=100):
    base_url = "https://news.naver.com/main/list.naver"
    headers = {"User-Agent": "Mozilla/5.0"}

    all_results = []
    seen = set()

    for page in range(1, pages + 1):
        params = {"mode": "LSD", "mid": "shm", "sid1": "101", "page": str(page)}
        response = requests.get(base_url, headers=headers, params=params)
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
                    # ✅ URL만 저장
                    all_results.append(href)

    print(f"✅ 총 {len(all_results)}개의 최근 뉴스 링크 수집 완료")
    return all_results



# ## Word crawling

# In[17]:


# 뉴스 본문을 크롤링하는 함수
def get_news_text(url: str) -> str:
    headers = {
        # 크롤링 시 차단을 피하기 위한 User-Agent 설정
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                      "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    }
    response = requests.get(url, headers=headers)  # URL 요청
    if response.status_code != 200:
        print(f"❌ 크롤링 실패: {url} 상태코드 {response.status_code}")
        return ""  # 실패 시 빈 문자열 반환
    
    soup = BeautifulSoup(response.text, "html.parser")  # HTML 파싱
    
    # 네이버 뉴스 본문은 보통 'div#newsct_article' 안에 있음
    article_body = soup.select_one("div#newsct_article")
    if article_body:
        # 본문 텍스트를 줄바꿈 포함해 깔끔하게 추출
        return article_body.get_text(strip=True, separator="\n")
    else:
        print(f"❌ 본문 없음: {url}")
        return ""  # 본문 없으면 빈 문자열 반환
    
# 뉴스 제목과 대표 이미지 URL을 추출하는 함수
def get_title_and_image(url: str):
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                      "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    }
    response = requests.get(url, headers=headers)
    soup = BeautifulSoup(response.text, "html.parser")

    # 제목: id=title_area 안의 h2 > span
    title_tag = soup.select_one("h2#title_area span")
    title = title_tag.text.strip() if title_tag else "제목 없음"

    # 대표 이미지: 여러 후보 위치 확인
    image_tag = (
        soup.select_one("figure img") or
        soup.select_one("span.end_photo_org img") or
        soup.select_one("div#newsct_article img")
    )

    if image_tag and "src" in image_tag.attrs:
        image_url = image_tag["src"]
    else:
        # fallback: <meta property="og:image"> 확인
        meta_tag = soup.find("meta", property="og:image")
        image_url = meta_tag["content"] if meta_tag else None

    return title, image_url


def extract_news_metadata(url: str):
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
    }
    response = requests.get(url, headers=headers)
    if response.status_code != 200:
        print(f"❌ 요청 실패: {url} 상태코드 {response.status_code}")
        return None, None, None, None

    soup = BeautifulSoup(response.text, "html.parser")

      # 📰 제목 추출
    title_tag = soup.select_one("h2#title_area span")
    title = title_tag.text.strip() if title_tag else "제목 없음"

    image_tag = (
        soup.select_one("figure img") or
        soup.select_one("span.end_photo_org img") or
        soup.select_one("div#newsct_article img")
    )

    if image_tag and "src" in image_tag.attrs:
        image_url = image_tag["src"]
    else:
        # fallback: <meta property="og:image"> 확인
        meta_tag = soup.find("meta", property="og:image")
        image_url = meta_tag["content"] if meta_tag else None
    # 언론사
    press_tag = soup.select_one("a.media_end_head_top_logo img")
    press = press_tag.get("alt").strip() if press_tag and press_tag.has_attr("alt") else "언론사 미확인"

    # 날짜
    time_tag = soup.select_one("span.media_end_head_info_datestamp_time") or soup.select_one("span.t11")
    date = time_tag.get_text(strip=True) if time_tag else "날짜 미확인"

    return title, image_url, press, date

# ## Remove particles

# In[18]:

def extract_press_name(soup: BeautifulSoup) -> str:
    """
    네이버 뉴스 기사 페이지에서 언론사 이름 추출
    """
    press_tag = soup.select_one("a.media_end_head_top_logo img")
    return press_tag.get("alt").strip() if press_tag and press_tag.has_attr("alt") else "언론사 미확인"


def extract_article_date(soup: BeautifulSoup) -> str:
    """
    네이버 뉴스 기사 페이지에서 기사 작성 날짜 추출
    """
    time_tag = soup.select_one("span.media_end_head_info_datestamp_time") \
               or soup.select_one("span.t11")
    return time_tag.get_text(strip=True) if time_tag else "날짜 미확인"

def remove_particles(word):
    """
    한글 단어에서 조사(조사 목록에 포함된 단어)를 제거하는 함수
    예) "전산장애로" → "전산장애"
    """
    # 정규표현식을 통해 단어 끝에 붙은 조사 제거
    pattern = r"(" + "|".join(PARTICLES) + r")$"
    return re.sub(pattern, "", word)



def extract_words_okt (text):
    """
    본문에서 명사를 추출하고 조사 제거 후
    중복 없이 처리하지 않고 등장 순서 그대로 반환
    """
    okt = Okt()
    nouns = okt.phrases(text)
    filtered = [remove_particles(n) for n in nouns if n.strip()]
    return filtered  # 중복 제거 없이 그대로 반환



# ## Print Word List

# In[19]:


def print_words_in_rows(words, words_per_row=10):
    """
    단어 리스트를 지정된 개수(words_per_row)만큼 한 줄에 출력
    """
    for i in range(0, len(words), words_per_row):
        print(", ".join(words[i : i + words_per_row]))


# ## DB Scanning

# In[20]:


BASE_DIR = Path(__file__).resolve().parent.parent
TERMS_DB_PATH = os.environ.get("TERMS_DB_PATH")
db_path = TERMS_DB_PATH if TERMS_DB_PATH else str(BASE_DIR / "economics_terms.db")

def find_description_from_db(conn, term_input):
    cursor = conn.cursor()
    query = "SELECT * FROM terms WHERE term = ?"
    cursor.execute(query, (term_input,))
    result = cursor.fetchone()
    if result:
        desc1 = result[2] if result[2] else ""
        desc2 = result[3] if result[3] else ""
        desc3 = result[4] if result[4] else ""
        return desc1, desc2, desc3
    else:
        return None, None, None

def extract_and_explain(url, db_path):
    # get_news_text, extract_words_okt 함수는 외부에 정의되어 있어야 합니다.
    text = get_news_text(url)
    words_Konpy = extract_words_okt(text)
    words_Konpy = list(dict.fromkeys(words_Konpy))  # 중복 제거 (순서 유지)

    db_file = Path(db_path)
    if not db_file.is_absolute():
        db_file = BASE_DIR / db_file

    if not db_file.exists():
        raise FileNotFoundError(f"경제 용어 DB 파일을 찾을 수 없습니다: {db_file}")

    conn = sqlite3.connect(db_file)
    terms = []  # (단어, 해설1, 해설2, 해설3) 저장용 리스트

    try:
        for word in words_Konpy:
            desc1, desc2, desc3 = find_description_from_db(conn, word)
            if any([desc1, desc2, desc3]):
                terms.append({"term": word, "desc1": desc1, "desc2": desc2, "desc3": desc3})
    finally:
        conn.close()

    # if terms:
    #     print("🔍 기사에서 등장한 경제 용어 설명 Konlpy:\n")
    #     for t in terms:
    #         print(f"📌 {t['term']}:")
    #         print(f"    해설1: {t['desc1']}")
    #         print(f"    해설2: {t['desc2']}")
    #         print(f"    해설3: {t['desc3']}\n")
    # else:
    #     print("📝 기사 내에서 설명 가능한 경제 용어를 찾지 못했습니다.")

    return terms  # 추후 다른 곳에 활용 가능


# ## 크롤링 & 단어 matching test

# In[21]:


# ✅ 실행
if __name__ == "__main__":

    # 1. 뉴스 URL 수집
    news_urls = get_naver_economy_news_urls_from_list(1)

    # 2. 각 뉴스 URL에 대해 처리
    for url in news_urls:
        extract_and_explain(url, db_path)


# ## Fast API Code

# In[ ]:


from fastapi import FastAPI, Query
from konlpy.tag import Okt
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import requests
from collections import OrderedDict

# 수정
from pydantic import BaseModel
from typing import List, Dict

app = FastAPI()
#수정
NEWS_CACHE: Dict[int, Dict] = {}

# Spring 서버 주소 (컨테이너 환경 고려)
SPRING_ENDPOINT = os.environ.get("SPRING_ENDPOINT", "http://backend:8080/news/upload")


okt = Okt()

STOPWORDS = set([
    "기자", "발표", "관련", "이번", "이날", "계획", "통해", "등", "및", "대한"
])

def extract_nouns(text):
    nouns = okt.nouns(text)
    return " ".join([n for n in nouns if len(n) > 1 and n not in STOPWORDS])

def get_top_keywords(tfidf_matrix, feature_names, top_n=5, min_score=0.05):
    result = []
    for row in tfidf_matrix:
        row_array = row.toarray().flatten()
        top_indices = row_array.argsort()[::-1]
        keywords = []
        for idx in top_indices:
            if row_array[idx] < min_score:
                continue
            keywords.append(feature_names[idx])
            if len(keywords) >= top_n:
                break
        result.append(keywords)
    return result


# def recommend_similar_news(idx, tfidf_matrix, news_data, top_n=2):
#     cosine_sim = cosine_similarity(tfidf_matrix[idx], tfidf_matrix).flatten()
#     similar_indices = cosine_sim.argsort()[::-1][1:top_n+1]
#     return [news_data[i] for i in similar_indices]

#수정
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

@app.post("/crawl_auto/")
def crawl_auto(pages: int = Query(1, description="가져올 뉴스 페이지 수")):
    print(f"📥 [1] {pages} 페이지에 대해 뉴스 크롤링 시작")

    # 1. 자동으로 URL 리스트 수집
    urls = get_naver_economy_news_urls_from_list()
    news_data = []

    print(f"🔗 [2] 총 {len(urls)}개 뉴스 URL 수집 완료")

    for i, url in enumerate(urls):
        print(f"📰 [3.{i+1}] 뉴스 URL 크롤링 중: {url}")
        title, image_url, press, date= extract_news_metadata(url)
        content = get_news_text(url)
        if content:
            news_data.append({
                "url": url,
                "title": title,
                "imageUrl": image_url,
                "content": content,
                "press": press,
                "date": date
            })
            print(f"✅ [3.{i+1}] 제목: {title}")
        else:
            print(f"⚠️ [3.{i+1}] 본문이 비어 있어 제외")

    print(f"🧠 [4] TF-IDF 분석 시작: 총 {len(news_data)}개 뉴스")

    docs = [extract_nouns(n["content"]) for n in news_data]
    vectorizer = TfidfVectorizer()
    tfidf_matrix = vectorizer.fit_transform(docs)
    feature_names = vectorizer.get_feature_names_out()
    keywords_list = get_top_keywords(tfidf_matrix, feature_names)

    results = []

    print(f"📡 [5] FastAPI → Spring 데이터 전송 시작")

    for i, item in enumerate(news_data):
        url = item["url"]
        title = item["title"]
        content = item["content"]
        image_url = item["imageUrl"]
        terms = extract_and_explain(url, db_path)
        keywords = keywords_list[i]

        print(f"\n📦 [5.{i+1}] 전송 준비 - 뉴스: {title}")
        print(f"    🔑 주요 키워드: {keywords}")
        print(f"    📘 용어 수: {len(terms)}개")

        data = {
            "url": url,
            "title": title,
            "content": content,
            "imageUrl": image_url,
            "terms": terms,
            "keywords": keywords,
            "press": item["press"],
            "date": item["date"]
        }

        try:
            res = requests.post(SPRING_ENDPOINT, json=data, timeout=5)
            print(f" [5.{i+1}] 전송 성공 → 응답 코드: {res.status_code}")
            results.append({
                "url": url,
                "status": res.status_code,
                "spring_response": res.text,
                "title": title,
                "imageUrl": image_url,
                "press": item["press"],
                "date": item["date"],
                "terms_found": [t["term"] for t in terms],
                "keywords": keywords
            })
        except Exception as e:
            print(f" [5.{i+1}] 전송 실패: {e}")
            results.append({
                "url": url,
                "error": str(e),
                "message": "Spring 서버로 전송 실패"
            })

    print(f"\n🎉 [6] 전체 뉴스 처리 완료 ({len(results)}개)")
    return {"results": results}

# 사용자 맞춤 추천 피드 임시 구현
#뉴스 캐싱 저장 API
@app.post("/cache/update")
def update_cache(item: NewsCacheItem):
    print(f"\n📥 캐시 업데이트 요청 받음:")
    print(f"- 뉴스 ID: {item.news_id}")
    print(f"- 제목: {item.title}")
    print(f"- 키워드: {item.keywords}")
    
    NEWS_CACHE[item.news_id] = {
        "title": item.title,
        "url": item.url,
        "keywords": item.keywords
    }
    print(f"✅ 뉴스 {item.news_id} 캐시에 저장 완료")
    print(f"📦 현재 캐시 크기: {len(NEWS_CACHE)}개\n")
    return {"message": f"뉴스 {item.news_id} 캐시에 저장 완료"}

#뉴스 추천 API
@app.post("/recommend", response_model=RecommendResponse)
def recommend(data: RecommendRequest):
    try:
        user_id = data.user_id
        clicked_ids = set(data.clicked_news_ids)
        
        print(f"📊 추천 요청 받음 - user_id: {user_id}")
        print(f"👆 클릭한 뉴스 ID: {clicked_ids}")
        print(f"📦 현재 캐시 상태 - 캐시된 뉴스 수: {len(NEWS_CACHE)}")

        # 사용자가 본 뉴스의 키워드 수집
        user_keywords = set()
        for nid in clicked_ids:
            article = NEWS_CACHE.get(nid)
            if article:
                user_keywords.update(article["keywords"])
                print(f"✅ 뉴스 {nid}의 키워드: {article['keywords']}")
            else:
                print(f"⚠️ 뉴스 {nid}가 캐시에 없음")

        print(f"🔑 수집된 전체 키워드: {user_keywords}")

        # 추천할 뉴스 후보
        scored = []
        for nid, article in NEWS_CACHE.items():
            if nid in clicked_ids:
                continue
            matched = list(user_keywords & set(article["keywords"]))
            score = len(matched)
            if score > 0:
                scored.append((score, matched, nid, article))

        # 정렬 및 상위 20개 선정
        scored.sort(reverse=True, key=lambda x: x[0])
        top_articles = scored[:20]

        # recommendations를 빈 리스트로 초기화
        recommendations = []
        
        # 추천 뉴스가 있는 경우에만 처리
        if top_articles:
            recommendations = [
                Recommendation(
                    news_id=nid,
                    title=article["title"],
                    url=article["url"],
                    matched_keywords=matched
                )
                for _, matched, nid, article in top_articles
            ]

        print(f"📚 추천된 뉴스 수: {len(recommendations)}")
        return RecommendResponse(user_id=user_id, recommendations=recommendations)

    except Exception as e:
        print(f"❌ 오류 발생: {str(e)}")
        import traceback
        print(f"상세 오류: {traceback.format_exc()}")
        raise

#캐시 상태 확인 API / 선택 자유
@app.get("/cache/list")
def list_cached_news():
    return {"cached_news_ids": list(NEWS_CACHE.keys())}


#1. FastAPI -> spring 뉴스 등록 / 크롤링된 뉴스 기사를 FASTAPI에 캐시 저장
#2. spring -> FastAPI 사용자 맞춤 추천 요청 / user_id, clicked_news_ids 전송
#3. FastAPI input: clicked_news_ids, NEWS_CACHE output: 동일 키워드 많은 순대로 상위 20개
#4. 사용자가 클릭한 뉴스 id에 해당되는 기사를 찾아 키워드들만 모아서 사용자 관심 키워드 집합 생성
#5. 각 기사마다 사용자 키워드와 몇개씩 겹치는지 계산
#6 겹치는 키워드가 높은 순으로 정렬, 상위 20개 추천
#7. 사용자 id, 추천 뉴스 id, 추천 뉴스 제목, 추천 뉴스 url, 추천 뉴스 키워드 반환
# FastAPI ->spring 추천 경과 반환 

#파이썬 실행 python -m uvicorn crawling_keyword_v3:app --reload --port 8000
#로컬 호스트 http://localhost:8000
#FastAPI http://localhost:8000/docs
