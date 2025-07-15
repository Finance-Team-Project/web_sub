console.log('news.js loaded');
// 뉴스/섹션/탭/indicator 관련 함수만 분리
function toggleIssueTab(tabName) {
    const issues = document.querySelectorAll('.issue-tab');
    issues.forEach(tab => tab.style.display = 'none');
    document.getElementById(tabName).style.display = 'grid';
    document.querySelectorAll('.tab-menu button').forEach(btn => btn.classList.remove('active'));
    document.querySelector(`.tab-menu button[data-tab="${tabName}"]`).classList.add('active');
}
window.addEventListener('DOMContentLoaded', () => {
    toggleIssueTab('issue-tab-finance');
    loadTodayNews();
    const sections = document.querySelectorAll('section');
    const indicators = document.querySelectorAll('.indicator button');
    document.getElementById('fullpage').addEventListener('scroll', () => {
        let scrollTop = document.getElementById('fullpage').scrollTop;
        let index = Math.round(scrollTop / window.innerHeight);
        indicators.forEach((btn, i) => {
            btn.classList.toggle('active', i === index);
        });
    });
    document.querySelector('button[data-tab="issue-tab-interest"]').addEventListener('click', () => loadInterestNews());
});
function scrollToSection(i) {
    document.getElementById('fullpage').scrollTo({
        top: i * window.innerHeight,
        behavior: 'smooth'
    });
}
async function loadTodayNews(page = 0, size = 8) { // Changed default size from 10 to 8
    if (!window.userId) {
        console.log('로그인되지 않은 사용자');
        return;
    }
    // Find or create the section wrapper
    let sectionWrapper = document.querySelector('.news-section-wrapper');
    if (!sectionWrapper) {
        sectionWrapper = document.createElement('div');
        sectionWrapper.className = 'news-section-wrapper';
        const container = document.getElementById('issue-tab-finance');
        container.parentNode.replaceChild(sectionWrapper, container);
        // Create news-list and pagination-container
        const newsList = document.createElement('div');
        newsList.className = 'news-list';
        newsList.id = 'issue-tab-finance';
        const paginationContainer = document.createElement('div');
        paginationContainer.className = 'pagination-container';
        sectionWrapper.appendChild(newsList);
        sectionWrapper.appendChild(paginationContainer);
    }
    const newsList = sectionWrapper.querySelector('.news-list');
    const paginationContainer = sectionWrapper.querySelector('.pagination-container');
    // Fetch news
    const response = await fetch(`/api/news?page=${page}&size=${size}`);
    const newsPage = await response.json();
    const newsListData = newsPage.content;
    const totalPages = newsPage.totalPages;
    // Render news cards
    newsList.innerHTML = '';
    newsListData.forEach(news => {
        const card = document.createElement('div');
        card.className = 'issue-card';
        card.innerHTML = `
            ${news.imageUrl ? `<img src="${news.imageUrl}" alt="뉴스 이미지" class="news-thumb">` : ''}
            <h3 onclick='loadNewsDetail(${news.id})'>${news.title}</h3>
            <p>${news.publisher} | ${new Date(news.publishedAt).toLocaleDateString()}</p>
            <a href="${news.url}" target="_blank" class="news-link">기사 원문</a>
        `;
        newsList.appendChild(card);
    });
    // Render pagination
    paginationContainer.innerHTML = '';
    const pagination = document.createElement('div');
    pagination.className = 'pagination';
    // Add previous arrow
    if (page > 0) {
        const prevBtn = document.createElement('button');
        prevBtn.className = 'arrow';
        prevBtn.innerHTML = '‹';
        prevBtn.onclick = () => loadTodayNews(page - 1, 8);
        pagination.appendChild(prevBtn);
    }
    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement('button');
        btn.textContent = i + 1;
        btn.className = (i === page) ? 'active' : '';
        btn.onclick = () => loadTodayNews(i, 8); // Always use 8 per page
        pagination.appendChild(btn);
    }
    // Add next arrow
    if (page < totalPages - 1) {
        const nextBtn = document.createElement('button');
        nextBtn.className = 'arrow';
        nextBtn.innerHTML = '›';
        nextBtn.onclick = () => loadTodayNews(page + 1, 8);
        pagination.appendChild(nextBtn);
    }
    paginationContainer.appendChild(pagination);
}
async function loadInterestNews(limit = 5) {
    if (!window.userId) {
        console.log('로그인되지 않은 사용자');
        return;
    }
    const container = document.getElementById('issue-tab-interest');
    container.innerHTML = '';
    const response = await fetch(`/api/news/interest?limit=${limit}`);
    if (!response.ok) {
        container.innerHTML = '<p>관심 뉴스가 없습니다.</p>';
        return;
    }
    const newsList = await response.json();
    if (newsList.length === 0) {
        container.innerHTML = '<p>관심 뉴스가 없습니다.</p>';
        return;
    }
    newsList.forEach(news => {
        const card = document.createElement('div');
        card.className = 'issue-card';
        card.innerHTML = `
            ${news.imageUrl ? `<img src="${news.imageUrl}" alt="뉴스 이미지" class="news-thumb">` : ''}
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
    modal.innerHTML = '<div class="modal-content news-style">' +
        '<button class="close-btn" onclick="document.body.removeChild(this.parentNode.parentNode)">✖</button>' +
        '<h2>' + news.title + '</h2>' +
        '<div class="news-meta">' + news.publisher + ' | ' + new Date(news.publishedAt).toLocaleDateString() + '</div>' +
        '<div class="news-action-bar">' +
            '<button class="origin-btn" onclick="window.open(\'' + news.url + '\',\'_blank\')">기사원문</button>' +
            '<div class="ai-summary-group">' +
                '<button class="ai-btn-circle" onclick="toggleAISummaryOptions(this)" title="AI 요약">AI</button>' +
                '<div class="ai-summary-options" style="display:none;">' +
                    '<button onclick="requestSummary(' + news.id + ', \'normal\', this)">일반 요약</button>' +
                    '<button onclick="requestSummary(' + news.id + ', \'custom\', this)">맞춤 요약</button>' +
                '</div>' +
            '</div>' +
        '</div>' +
        '<div class="news-body">' + news.content + '</div>' +
        '<div id="ai-summary-result"></div>' +
    '</div>';
    document.body.appendChild(modal);

    // 진단: .news-body와 mark 태그 개수 출력
    const newsBody = document.querySelector('.news-body');
    console.log('[진단] .news-body:', newsBody);
    console.log('[진단] .news-body 내 mark 개수:', newsBody ? newsBody.querySelectorAll('mark').length : 0);

    // .news-body 교체 및 이벤트 위임 등록
    const newNewsBody = newsBody.cloneNode(true);
    newsBody.parentNode.replaceChild(newNewsBody, newsBody);
    newNewsBody.addEventListener('click', function(e) {
        console.log('[진단] .news-body 클릭됨, e.target:', e.target);
        if (e.target.tagName === 'MARK') {
            console.log('mark clicked', {
                newsId: news.id,
                newsTitle: news.title,
                newsUrl: news.url
            });
            showTermPopup(e.target.innerText, {
                newsId: news.id,
                newsTitle: news.title,
                newsUrl: news.url
            });
        }
    });
} 