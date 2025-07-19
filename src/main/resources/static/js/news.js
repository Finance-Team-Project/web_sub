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
            <p>${news.publisher} | ${new Date(news.publishedAt).toLocaleDateString()}</p>
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
            <p>${news.publisher} | ${new Date(news.publishedAt).toLocaleDateString()}</p>
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

    const modal = document.createElement('div');
    modal.className = 'news-modal';
    modal.innerHTML = `
      <div class="modal-content news-style" style="width:900px;max-width:95vw;padding:48px;">
        <button class="close-btn" onclick="document.body.removeChild(this.closest('.news-modal'))">✖</button>
        <h2>${news.title}</h2>
        <div class="news-meta">${news.publisher} | ${new Date(news.publishedAt).toLocaleDateString()}</div>
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
        <div id="ai-summary-result"></div>
      </div>
    `;
    document.body.appendChild(modal);

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
