// AI 요약 관련 함수만 분리
function toggleAISummaryOptions(btn) {
    const options = btn.nextElementSibling;
    options.style.display = options.style.display === 'none' ? 'block' : 'none';
}
async function requestSummary(newsId, type, btn) {
    if (!window.userId) {
        alert('로그인 후 이용 가능합니다.');
        window.location.href = '/login';
        return;
    }
    btn.disabled = true;
    btn.innerText = '요약 중...';
    const endpoint = type === 'normal' ? '/api/summarize' : '/api/summarize/chat';
    const res = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ newsId: newsId, userId: window.userId })
    });
    const summary = await res.text();
    document.getElementById('ai-summary-result').innerHTML = `<div class='ai-summary-box'><b>AI 요약 결과:</b><br>${summary}</div>`;
    btn.disabled = false;
    btn.innerText = type === 'normal' ? '일반 요약' : '맞춤 요약';
    btn.parentNode.style.display = 'none';
} 