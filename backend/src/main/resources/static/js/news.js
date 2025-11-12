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
    // window.userId가 설정될 때까지 대기 (Thymeleaf 스크립트 실행 대기)
    const waitForUserId = (retries = 10) => {
        if (window.userId) {
            // 초기 Finance 탭 로드
            toggleIssueTab('issue-tab-finance');
            loadTodayNews();
        } else if (retries > 0) {
            // 100ms 후 재시도
            setTimeout(() => waitForUserId(retries - 1), 100);
        } else {
            console.warn('window.userId가 설정되지 않았습니다. 로그인 상태를 확인해주세요.');
        }
    };
    waitForUserId();

    // 탭 메뉴 클릭 시 각 로딩 함수 호출
    const financeTabBtn = document.querySelector('.tab-menu button[data-tab="issue-tab-finance"]');
    const interestTabBtn = document.querySelector('.tab-menu button[data-tab="issue-tab-interest"]');
    
    if (financeTabBtn) {
        financeTabBtn.addEventListener('click', () => {
            toggleIssueTab('issue-tab-finance');
            loadTodayNews();
        });
    }
    
    if (interestTabBtn) {
        interestTabBtn.addEventListener('click', () => {
            toggleIssueTab('issue-tab-interest');
            loadInterestNews();
        });
    }

    // fullpage scroll indicator
    const fullpage = document.getElementById('fullpage');
    if (fullpage) {
        const indicators = document.querySelectorAll('.indicator button');
        fullpage.addEventListener('scroll', () => {
            let scrollTop = fullpage.scrollTop;
            let index = Math.round(scrollTop / window.innerHeight);
            indicators.forEach((btn, i) => {
                btn.classList.toggle('active', i === index);
            });
        });
    }
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
    
    if (!newsList) {
        console.error('뉴스 리스트 컨테이너를 찾을 수 없습니다.');
        return;
    }
    
    newsList.innerHTML = '<div style="text-align: center; padding: 20px;">뉴스를 불러오는 중...</div>';
    if (paginationContainer) paginationContainer.innerHTML = '';

    try {
        // Fetch news
        const response = await fetch(`/api/news?page=${page}&size=${size}`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const newsPage = await response.json();
        
        if (!newsPage || !newsPage.content) {
            throw new Error('뉴스 데이터 형식이 올바르지 않습니다.');
        }
        
        const newsListData = newsPage.content;
        const totalPages = newsPage.totalPages || 0;

        // Render news cards
        newsList.innerHTML = ''; // 로딩 메시지 제거
        
        if (newsListData.length === 0) {
            newsList.innerHTML = '<div style="text-align: center; padding: 20px; color: #888;">표시할 뉴스가 없습니다.</div>';
            return;
        }
        
        newsListData.forEach(news => {
            const card = document.createElement('div');
            card.className = 'issue-card';
            card.innerHTML = `
                <h3 onclick='loadNewsDetail(${news.id})'>${news.title || '제목 없음'}</h3>
                <p>${news.press || '출처 없음'} | ${news.publishedAt ? new Date(news.publishedAt).toLocaleDateString() : '날짜 없음'}</p>
                <a href="${news.url || '#'}" target="_blank" class="news-link">기사 원문</a>
            `;
            newsList.appendChild(card);
        });

        // Render pagination
        if (paginationContainer && totalPages > 1) {
            const pagination = document.createElement('div');
            pagination.className = 'pagination';

            if (page > 0) {
                const prevBtn = document.createElement('button');
                prevBtn.className = 'arrow';
                prevBtn.innerHTML = '‹';
                prevBtn.onclick = () => loadTodayNews(page - 1, size);
                pagination.appendChild(prevBtn);
            }

            // 페이지 번호 버튼 (최대 5개만 표시)
            const maxVisiblePages = 5;
            const startPage = Math.max(0, Math.min(page - Math.floor(maxVisiblePages / 2), totalPages - maxVisiblePages));
            const endPage = Math.min(startPage + maxVisiblePages, totalPages);

            // 첫 페이지
            if (startPage > 0) {
                const firstBtn = document.createElement('button');
                firstBtn.textContent = '1';
                firstBtn.onclick = () => loadTodayNews(0, size);
                pagination.appendChild(firstBtn);

                if (startPage > 1) {
                    const ellipsis = document.createElement('span');
                    ellipsis.textContent = '...';
                    ellipsis.className = 'ellipsis';
                    pagination.appendChild(ellipsis);
                }
            }

            // 중간 페이지들
            for (let i = startPage; i < endPage; i++) {
                const btn = document.createElement('button');
                btn.textContent = i + 1;
                btn.className = (i === page) ? 'active' : '';
                btn.onclick = () => loadTodayNews(i, size);
                pagination.appendChild(btn);
            }

            // 마지막 페이지
            if (endPage < totalPages) {
                if (endPage < totalPages - 1) {
                    const ellipsis = document.createElement('span');
                    ellipsis.textContent = '...';
                    ellipsis.className = 'ellipsis';
                    pagination.appendChild(ellipsis);
                }

                const lastBtn = document.createElement('button');
                lastBtn.textContent = totalPages;
                lastBtn.onclick = () => loadTodayNews(totalPages - 1, size);
                pagination.appendChild(lastBtn);
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
    } catch (error) {
        console.error('뉴스 로딩 중 오류 발생:', error);
        if (newsList) {
            newsList.innerHTML = '<div style="text-align: center; padding: 20px; color: #dc3545;">뉴스를 불러오는데 실패했습니다. 페이지를 새로고침해주세요.</div>';
        }
        if (paginationContainer) {
            paginationContainer.innerHTML = '';
        }
    }
}

async function loadInterestNews(page = 0, size = 8) {
    if (!window.userId) {
        console.log('로그인되지 않은 사용자');
        return;
    }

    const container = document.getElementById('issue-tab-interest');
    const paginationContainer = document.getElementById('pagination-interest');
    container.innerHTML = '<div class="loading">추천 뉴스를 불러오는 중...</div>';
    if (paginationContainer) paginationContainer.innerHTML = '';

    try {
        // 추천 뉴스 요청
        const recommendResponse = await fetch('/news/recommendations');
        const recommendData = await recommendResponse.json();
        
        container.innerHTML = ''; // 로딩 메시지 제거
        
        if (recommendData.recommendations && recommendData.recommendations.length > 0) {
            // 페이지네이션 적용
            const totalNews = recommendData.recommendations;
            const startIndex = page * size;
            const endIndex = Math.min(startIndex + size, totalNews.length);
            const pageNews = totalNews.slice(startIndex, endIndex);
            const totalPages = Math.ceil(totalNews.length / size);

            // 뉴스 카드 표시
            pageNews.forEach(news => {
                const card = document.createElement('div');
                card.className = 'issue-card';
                card.innerHTML = `
                    <h3 onclick='loadNewsDetail(${news.news_id})'>${news.title}</h3>
                    <p class="keywords">매칭 키워드: ${news.matched_keywords.join(', ')}</p>
                    <a href="${news.url}" target="_blank" class="news-link" 
                       onclick="event.stopPropagation();">기사 원문</a>
                `;
                container.appendChild(card);
            });

            // 페이지네이션 UI 생성
            if (paginationContainer && totalPages > 1) {
                const pagination = document.createElement('div');
                pagination.className = 'pagination';

                // 이전 페이지 버튼
                if (page > 0) {
                    const prevBtn = document.createElement('button');
                    prevBtn.className = 'arrow';
                    prevBtn.innerHTML = '‹';
                    prevBtn.onclick = () => loadInterestNews(page - 1, size);
                    pagination.appendChild(prevBtn);
                }

                // 페이지 번호 버튼 (최대 5개만 표시)
                const maxVisiblePages = 5;
                const startPage = Math.max(0, Math.min(page - Math.floor(maxVisiblePages / 2), totalPages - maxVisiblePages));
                const endPage = Math.min(startPage + maxVisiblePages, totalPages);

                // 첫 페이지
                if (startPage > 0) {
                    const firstBtn = document.createElement('button');
                    firstBtn.textContent = '1';
                    firstBtn.onclick = () => loadInterestNews(0, size);
                    pagination.appendChild(firstBtn);

                    if (startPage > 1) {
                        const ellipsis = document.createElement('span');
                        ellipsis.textContent = '...';
                        ellipsis.className = 'ellipsis';
                        pagination.appendChild(ellipsis);
                    }
                }

                // 중간 페이지들
                for (let i = startPage; i < endPage; i++) {
                    const btn = document.createElement('button');
                    btn.textContent = i + 1;
                    btn.className = (i === page) ? 'active' : '';
                    btn.onclick = () => loadInterestNews(i, size);
                    pagination.appendChild(btn);
                }

                // 마지막 페이지
                if (endPage < totalPages) {
                    if (endPage < totalPages - 1) {
                        const ellipsis = document.createElement('span');
                        ellipsis.textContent = '...';
                        ellipsis.className = 'ellipsis';
                        pagination.appendChild(ellipsis);
                    }

                    const lastBtn = document.createElement('button');
                    lastBtn.textContent = totalPages;
                    lastBtn.onclick = () => loadInterestNews(totalPages - 1, size);
                    pagination.appendChild(lastBtn);
                }

                // 다음 페이지 버튼
                if (page < totalPages - 1) {
                    const nextBtn = document.createElement('button');
                    nextBtn.className = 'arrow';
                    nextBtn.innerHTML = '›';
                    nextBtn.onclick = () => loadInterestNews(page + 1, size);
                    pagination.appendChild(nextBtn);
                }

                paginationContainer.appendChild(pagination);
            }
        } else {
            container.innerHTML = '<div class="no-news" style="text-align: center; padding: 20px;">추천할 뉴스가 없습니다. 더 많은 뉴스를 읽어보세요!</div>';
        }
    } catch (error) {
        console.error('추천 뉴스 로딩 실패:', error);
        container.innerHTML = '<div class="error" style="text-align: center; padding: 20px; color: #dc3545;">추천 뉴스를 불러오는데 실패했습니다.</div>';
    }
}

async function loadNewsDetail(newsId) {
    console.log('loadNewsDetail called', newsId);
    
    if (!newsId) {
        console.error('뉴스 ID가 없습니다.');
        alert('뉴스 정보를 불러올 수 없습니다.');
        return;
    }
    
    try {
        // AI 요약, 용어 팝업 등은 별도 파일에서 구현
        const response = await fetch(`/api/news/${newsId}`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const news = await response.json();
        
        if (!news) {
            throw new Error('뉴스 데이터를 받을 수 없습니다.');
        }

        // 키워드 클라우드 데이터 가져오기
        let keywords = [];
        try {
            const keywordsRes = await fetch(`/api/news/${newsId}/keywords`);
            if (keywordsRes.ok) {
                keywords = await keywordsRes.json();
            }
        } catch (kwError) {
            console.warn('키워드 로딩 실패:', kwError);
            // 키워드 로딩 실패는 치명적이지 않으므로 계속 진행
        }

        const modal = document.createElement('div');
        modal.className = 'news-modal';
        modal.innerHTML = `
          <div class="modal-content news-style" style="width:900px;max-width:95vw;padding:48px;">
            <button class="close-btn" onclick="document.body.removeChild(this.closest('.news-modal'))">✖</button>
            <h2>${news.title || '제목 없음'}</h2>
            <div class="news-meta">${news.press || '출처 없음'} | ${news.publishedAt ? new Date(news.publishedAt).toLocaleDateString() : '날짜 없음'}</div>
            <div class="news-action-bar">
              <button class="origin-btn" onclick="window.open('${news.url || '#'}','_blank')">기사원문</button>
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
              ${news.content || '내용 없음'}
            </div>
            <div id="keyword-cloud-tags" style="margin:24px 0 0 0;"></div>
            <div id="ai-summary-result"></div>
          </div>
        `;
        document.body.appendChild(modal);

        // 뷰 이벤트 및 클릭 로그 전송
        try {
            if (window.userId) {
                // 이벤트 전송
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

                // 클릭 로그 전송
                await fetch(`/api/news/${newsId}/click`, {
                    method: 'POST'
                });
            }
        } catch (e) {
            console.warn('event/log send failed', e);
            // 이벤트 전송 실패는 치명적이지 않으므로 계속 진행
        }

        // 키워드 태그 클라우드 렌더링
        const tagCloud = modal.querySelector('#keyword-cloud-tags');
        if (tagCloud) {
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
        }

        // mark 클릭 이벤트 위임
        const newsBody = modal.querySelector('.news-body');
        if (newsBody) {
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
    } catch (error) {
        console.error('뉴스 상세 로딩 중 오류 발생:', error);
        alert('뉴스 정보를 불러오는데 실패했습니다. 다시 시도해주세요.');
    }
}

// 키워드 클릭 시 해당 키워드가 포함된 뉴스 목록을 모달로 표시
async function showNewsListByKeyword(keyword) {
    if (!keyword) {
        console.error('키워드가 없습니다.');
        return;
    }
    
    try {
        const response = await fetch(`/api/news?keyword=${encodeURIComponent(keyword)}&page=0&size=8`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const newsPage = await response.json();
        
        if (!newsPage || !newsPage.content) {
            throw new Error('뉴스 데이터 형식이 올바르지 않습니다.');
        }
        
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
                    <h4 style="margin:0 0 6px 0;cursor:pointer;" onclick="loadNewsDetail(${n.id})">${n.title || '제목 없음'}</h4>
                    <div style="color:#555;font-size:0.97em;">${n.press || '출처 없음'} | ${n.publishedAt ? new Date(n.publishedAt).toLocaleDateString() : '날짜 없음'}</div>
                    <a href="${n.url || '#'}" target="_blank" class="news-link">기사 원문</a>
                  </div>
                `).join('')}
            </div>
          </div>
        `;
        document.body.appendChild(modal);
    } catch (error) {
        console.error('키워드 뉴스 목록 로딩 중 오류 발생:', error);
        alert('키워드 관련 뉴스를 불러오는데 실패했습니다. 다시 시도해주세요.');
    }
}
