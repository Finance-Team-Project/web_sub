async function openShortAnswerQuiz() {
  console.log("퀴즈 함수 실행됨");

  try {
      const response = await fetch('/api/quiz/short-answer?userId=1');
      const quiz = await response.json();

      console.log('quiz 응답:', quiz);

      const modal = document.createElement('div');
      modal.className = 'news-modal';
      modal.innerHTML = `
        <div class="modal-content news-style">
          <button class="close-btn" onclick="document.body.removeChild(this.parentNode.parentNode)">✖</button>
          <h2>📋 단어 퀴즈</h2>
          <form onsubmit="submitQuiz(event, ${quiz.quizId})">
            ${quiz.items.map(qt => `
              <div class="quiz-item">
                <p><strong>해설:</strong> ${qt.question}</p>
                <input type="text" name="answer_${qt.termId}" placeholder="정답 입력" style="width: 100%; padding: 10px; margin-bottom: 10px;">
              </div>
            `).join('')}
            <button type="submit">제출하기</button>
          </form>
        </div>
      `;

      document.body.appendChild(modal);
  } catch (err) {
      alert("퀴즈를 불러오는 데 실패했습니다.");
      console.error(err);
  }
}

async function submitQuiz(event, quizId) {
  event.preventDefault();

  const formData = new FormData(event.target);
  const answers = {};
  for (let [key, value] of formData.entries()) {
      answers[key.replace('answer_', '')] = value.trim();
  }

  const response = await fetch(`/api/quiz/submit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ quizId, userId: 1, answers })
  });

  const result = await response.json();
  alert(`퀴즈 결과: ${result.score}점`);
  document.querySelector('.news-modal').remove();
}
