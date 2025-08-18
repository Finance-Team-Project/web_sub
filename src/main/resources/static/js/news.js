// news.js

console.log('news.js loaded');

// 뉴스/섹션/탭/indicator 관련 함수만 분리
function toggleIssueTab(tabName) {
    const issues = document.querySelectorAll('.issue-tab');
    issues.forEach(tab => tab.style.display = 'none');
    document.getElementById(tabName).style.display = 'grid';
    document.querySelectorAll('.tab-menu button').forEach(btn => btn.classList.remove('active'));
    document.querySelector(`.tab-menu button[data-tab="${tabName}"]`).classList.add('active');

    // 페이지네이션 토글
    const paginationFinance = document.getElementById('pagination-finance');
    const paginationInterest = document.getElementById('pagination-interest');
    if (tabName === 'issue-tab-finance') {
        if (paginationFinance) paginationFinance.style.display = 'flex';
        if (paginationInterest) paginationInterest.style.display = 'none';
    } else if (tabName === 'issue-tab-interest') {
        if (paginationFinance) paginationFinance.style.display = 'none';
        if (paginationInterest) paginationInterest.style.display = 'flex';
    }
}

window.addEventListener('DOMContentLoaded', () => {
    // 초기 Finance 탭 로드
    toggleIssueTab('issue-tab-finance');
    loadTodayNews();

    // 탭 메뉴 클릭 시 각 로딩 함수 호출
    document.querySelector('.tab-menu button[data-tab="issue-tab-finance"]')
        .addEventListener('click', () => {
            toggleIssueTab('issue-tab-finance');
            loadTodayNews();
        });
    document.querySelector('.tab-menu button[data-tab="issue-tab-interest"]')
        .addEventListener('click', () => {
            toggleIssueTab('issue-tab-interest');
            loadInterestNews();
        });

    // fullpage scroll indicator
    const indicators = document.querySelectorAll('.indicator button');
    document.getElementById('fullpage').addEventListener('scroll', () => {
        let scrollTop = document.getElementById('fullpage').scrollTop;
        let index = Math.round(scrollTop / window.innerHeight);
        indicators.forEach((btn, i) => {
            btn.classList.toggle('active', i === index);
        });
    });
});

document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        // 가장 마지막(맨 위) .news-modal만 닫기
        const modals = document.querySelectorAll('.news-modal');
        if (modals.length > 0) {
            const topModal = modals[modals.length - 1];
            document.body.removeChild(topModal);
            // 이벤트가 중복 처리되지 않도록 중단
            e.preventDefault();
            e.stopImmediatePropagation();
        }
    }
}, true); // 캡처 단계에서 처리

function scrollToSection(i) {
    document.getElementById('fullpage').scrollTo({
        top: i * window.innerHeight,
        behavior: 'smooth'
    });
}

async function loadTodayNews(page = 0, size = 8) {
    if (!window.userId) {
        console.log('로그인되지 않은 사용자');
        return;
    }

    // Finance 탭 내부만 초기화
    const newsList = document.getElementById('issue-tab-finance');
    const paginationContainer = document.getElementById('pagination-finance');
    newsList.innerHTML = '';
    if (paginationContainer) paginationContainer.innerHTML = '';

    // Fetch news
    const response = await fetch(`/api/news?page=${page}&size=${size}`);
    const newsPage = await response.json();
    const newsListData = newsPage.content;
    const totalPages = newsPage.totalPages;

    // Render news cards
    newsListData.forEach(news => {
        const card = document.createElement('div');
        card.className = 'issue-card';
        card.innerHTML = `
            <h3 onclick='loadNewsDetail(${news.id})'>${news.title}</h3>
            <p>${news.press} | ${new Date(news.publishedAt).toLocaleDateString()}</p>
            <a href="${news.url}" target="_blank" class="news-link">기사 원문</a>
        `;
        newsList.appendChild(card);
    });

    // Render pagination
    if (paginationContainer) {
        const pagination = document.createElement('div');
        pagination.className = 'pagination';

        if (page > 0) {
            const prevBtn = document.createElement('button');
            prevBtn.className = 'arrow';
            prevBtn.innerHTML = '‹';
            prevBtn.onclick = () => loadTodayNews(page - 1, size);
            pagination.appendChild(prevBtn);
        }

        for (let i = 0; i < totalPages; i++) {
            const btn = document.createElement('button');
            btn.textContent = i + 1;
            btn.className = (i === page) ? 'active' : '';
            btn.onclick = () => loadTodayNews(i, size);
            pagination.appendChild(btn);
        }

        if (page < totalPages - 1) {
            const nextBtn = document.createElement('button');
            nextBtn.className = 'arrow';
            nextBtn.innerHTML = '›';
            nextBtn.onclick = () => loadTodayNews(page + 1, size);
            pagination.appendChild(nextBtn);
        }

        paginationContainer.appendChild(pagination);
    }
}

async function loadInterestNews(limit = 5) {
    if (!window.userId) {
        console.log('로그인되지 않은 사용자');
        return;
    }

    // 오직 interest 탭 영역만 초기화
    const container = document.getElementById('issue-tab-interest');
    container.innerHTML = '';

    // 페이지네이션도 비우기
    const paginationContainer = document.getElementById('pagination-interest');
    if (paginationContainer) paginationContainer.innerHTML = '';

    // Fetch interest news
    const response = await fetch(`/api/news/interest?limit=${limit}`);
    if (!response.ok) {
        return;
    }

    const newsList = await response.json();
    if (newsList.length === 0) {
        return;
    }

    newsList.forEach(news => {
        const card = document.createElement('div');
        card.className = 'issue-card';
        card.innerHTML = `
            <h3 onclick='loadNewsDetail(${news.id})'>${news.title}</h3>
            <p>${news.press} | ${new Date(news.publishedAt).toLocaleDateString()}</p>
            <a href="${news.url}" target="_blank" class="news-link">기사 원문</a>
        `;
        container.appendChild(card);
    });
}

async function loadNewsDetail(newsId) {
    console.log('loadNewsDetail called', newsId);
    // AI 요약, 용어 팝업 등은 별도 파일에서 구현
    const response = await fetch(`/api/news/${newsId}`);
    const news = await response.json();

    // 키워드 클라우드 데이터 가져오기
    const keywordsRes = await fetch(`/api/news/${newsId}/keywords`);
    const keywords = await keywordsRes.json();

    const modal = document.createElement('div');
    modal.className = 'news-modal';
    modal.innerHTML = `
      <div class="modal-content news-style" style="width:900px;max-width:95vw;padding:48px;">
        <button class="close-btn" onclick="document.body.removeChild(this.closest('.news-modal'))">✖</button>
        <h2>${news.title}</h2>
        <div class="news-meta">${news.press} | ${new Date(news.publishedAt).toLocaleDateString()}</div>
        <div class="news-action-bar">
          <button class="origin-btn" onclick="window.open('${news.url}','_blank')">기사원문</button>
          <div class="ai-summary-group">
            <button class="ai-btn-circle" onclick="toggleAISummaryOptions(this)" title="AI 요약">AI</button>
            <div class="ai-summary-options" style="display:none;">
              <button onclick="requestSummary(${news.id}, 'normal', this)">일반 요약</button>
              <button onclick="requestSummary(${news.id}, 'custom', this)">맞춤 요약</button>
            </div>
          </div>
        </div>
        <div class="news-body">
          ${news.imageUrl ? `<img src="${news.imageUrl}" class="news-thumb" style="max-width:100%;max-height:220px;display:block;margin:0 auto 18px;border-radius:10px;">` : ''}
          ${news.content}
        </div>
        <div id="keyword-cloud-tags" style="margin:24px 0 0 0;"></div>
        <div id="ai-summary-result"></div>
      </div>
    `;
    document.body.appendChild(modal);

    // 뷰 이벤트 전송 (간단한 1건 배치 형태)
    try {
        if (window.userId) {
            await fetch('/events/batch', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    events: [{
                        event_id: crypto.randomUUID ? crypto.randomUUID() : String(Date.now()),
                        user_id: Number(window.userId),
                        news_id: Number(newsId),
                        event_type: 'view',
                        timestamp: new Date().toISOString(),
                        dwell_time_ms: 0
                    }]
                })
            });
        }
    } catch (e) {
        console.warn('event send failed', e);
    }

    // 키워드 태그 클라우드 렌더링
    const tagCloud = modal.querySelector('#keyword-cloud-tags');
    if (keywords && keywords.length > 0) {
        tagCloud.innerHTML = keywords.map(kw => `<span class="keyword-tag" style="display:inline-block;margin:0 8px 8px 0;padding:6px 14px;background:#eaf2ff;border-radius:16px;cursor:pointer;font-size:1.08em;transition:background 0.13s;" data-kw="${kw}">#${kw}</span>`).join('');
        tagCloud.addEventListener('click', e => {
            if (e.target.classList.contains('keyword-tag')) {
                const kw = e.target.getAttribute('data-kw');
                showNewsListByKeyword(kw); // 단어 뜻 대신 관련 뉴스 모달 표시
            }
        });
    } else {
        tagCloud.innerHTML = '<span style="color:#aaa;">키워드 없음</span>';
    }

    // mark 클릭 이벤트 위임
    const newsBody = modal.querySelector('.news-body');
    newsBody.addEventListener('click', e => {
        if (e.target.tagName === 'MARK') {
            showTermPopup(e.target.innerText, {
                newsId: news.id,
                newsTitle: news.title,
                newsUrl: news.url
            });
        }
    });
}

// 키워드 클릭 시 해당 키워드가 포함된 뉴스 목록을 모달로 표시
async function showNewsListByKeyword(keyword) {
    const response = await fetch(`/api/news?keyword=${encodeURIComponent(keyword)}&page=0&size=8`);
    const newsPage = await response.json();
    const newsList = newsPage.content;
    const modal = document.createElement('div');
    modal.className = 'news-modal';
    modal.innerHTML = `
      <div class="modal-content news-style" style="width:700px;max-width:95vw;padding:36px;">
        <button class="close-btn" onclick="document.body.removeChild(this.closest('.news-modal'))">✖</button>
        <h3 style="margin-bottom:18px;">'${keyword}' 키워드 관련 뉴스</h3>
        <div id="keyword-news-list" style="max-height:400px; overflow-y:auto;">
          ${newsList.length === 0 ? '<div style="color:#888;">해당 키워드의 뉴스가 없습니다.</div>' :
            newsList.map(n => `
              <div class="issue-card" style="margin-bottom:16px;">
                <h4 style="margin:0 0 6px 0;cursor:pointer;" onclick="loadNewsDetail(${n.id})">${n.title}</h4>
                <div style="color:#555;font-size:0.97em;">${n.press} | ${new Date(n.publishedAt).toLocaleDateString()}</div>
                <a href="${n.url}" target="_blank" class="news-link">기사 원문</a>
              </div>
            `).join('')}
        </div>
      </div>
    `;
    document.body.appendChild(modal);
}
