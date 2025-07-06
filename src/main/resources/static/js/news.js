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
});
function scrollToSection(i) {
    document.getElementById('fullpage').scrollTo({
        top: i * window.innerHeight,
        behavior: 'smooth'
    });
}
async function loadTodayNews() {
    if (!window.userId) {
        console.log('로그인되지 않은 사용자');
        return;
    }
    const response = await fetch('/api/news');
    const newsList = await response.json();
    const container = document.getElementById('issue-tab-finance');
    container.innerHTML = '';
    newsList.forEach(news => {
        const card = document.createElement('div');
        card.className = 'issue-card';
        card.innerHTML = `
            <h3 onclick='loadNewsDetail(${news.id})'>${news.title}</h3>
            <p>${news.publisher} | ${new Date(news.publishedAt).toLocaleDateString()}</p>
        `;
        container.appendChild(card);
    });
}
async function loadNewsDetail(newsId) {
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
    document.querySelectorAll('.news-body mark').forEach(el => {
        el.style.cursor = 'pointer';
        el.addEventListener('click', () => showTermPopup(el.innerText));
    });
} 