// 용어(단어장) 팝업 및 추가 관련 함수만 분리
async function showTermPopup(term, newsInfo = {}) {
    console.log('[showTermPopup] newsInfo:', newsInfo);
    const existingTermModal = document.getElementById('term-modal');
    if (existingTermModal) {
        document.body.removeChild(existingTermModal);
    }
    try {
        const response = await fetch(`/api/terms/${encodeURIComponent(term)}`);
        if (!response.ok) throw new Error("Not found");
        const data = await response.json();
        const modal = document.createElement('div');
        modal.className = 'news-modal';
        modal.id = 'term-modal';
        modal.innerHTML = `
          <div class="modal-content news-style">
            <button class="close-btn" onclick="document.body.removeChild(this.parentNode.parentNode)">✖</button>
            <h2>📘 용어 해설: ${data.term}</h2>
            <div class="news-body">${data.description}</div>
            ${data.example ? `<div class="term-extra"><strong>예시:</strong> ${data.example}</div>` : ''}
            <button id="addToVocabBtn" class="add-to-vocab-btn" style="margin-top:16px;">
              <span class="btn-label">단어장에 추가</span>
            </button>
          </div>
        `;
        document.body.appendChild(modal);
        document.getElementById('addToVocabBtn').onclick = async function() {
            if (!window.userId) {
                alert('로그인 후 이용 가능합니다.');
                window.location.href = '/login';
                return;
            }
            const btn = this;
            const label = btn.querySelector('.btn-label');
            label.style.display = 'none';
            let spinner = document.createElement('span');
            spinner.className = 'spinner';
            btn.appendChild(spinner);
            btn.disabled = true;
            const payload = {
                termId: data.id,
                ...newsInfo
            };
            const res = await fetch('/api/vocabulary/add', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const result = await res.json();
            btn.removeChild(spinner);
            if (result.success) {
                let check = document.createElement('span');
                check.className = 'checkmark';
                check.innerHTML = '✔';
                btn.appendChild(check);
                label.textContent = '추가 완료!';
                label.style.display = 'inline';
                setTimeout(() => {
                    btn.disabled = false;
                    btn.removeChild(check);
                    label.textContent = '단어장에 추가';
                }, 1500);
            } else {
                label.textContent = result.message || '추가에 실패했습니다.';
                label.style.display = 'inline';
                setTimeout(() => {
                    btn.disabled = false;
                    label.textContent = '단어장에 추가';
                }, 1800);
            }
        };
    } catch (e) {
        const existingErrorModal = document.getElementById('term-modal');
        if (existingErrorModal) {
            document.body.removeChild(existingErrorModal);
        }
        const modal = document.createElement('div');
        modal.className = 'news-modal';
        modal.id = 'term-modal';
        modal.innerHTML = `
          <div class="modal-content news-style">
            <button class="close-btn" onclick="document.body.removeChild(this.parentNode.parentNode)">✖</button>
            <h2>❗ 용어 정보를 찾을 수 없습니다.</h2>
            <div class="news-body">"${term}"에 대한 설명이 없습니다.</div>
          </div>
        `;
        document.body.appendChild(modal);
    }
}
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        const termModal = document.getElementById('term-modal');
        if (termModal) {
            document.body.removeChild(termModal);
            return;
        }
        const newsModal = document.querySelector('.news-modal');
        if (newsModal) {
            document.body.removeChild(newsModal);
        }
    }
}); 