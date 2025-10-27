-- 1. 자산
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('자산', '기업이나 개인이 보유한 경제적 가치를 지닌 자원으로 미래에 현금흐름을 창출할 수 있는 항목.', 1, '재무회계', NOW());

-- 2. 금리
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금리', '자금의 대여·차입 시 적용되는 이자율로, 자본의 시간가치를 반영하는 가격.', 1, '거시/금융', NOW());

-- 3. 시장
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('시장', '재화·서비스가 수요자와 공급자 간에 교환되는 제도적 장치 또는 공간.', 1, '미시/시장', NOW());

-- 4. 환율
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('환율', '서로 다른 통화 간의 교환비율로, 한 단위 외국통화를 자국통화로 표시한 가격.', 1, '국제금융', NOW());

-- 5. 수요
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('수요', '주어진 가격에서 소비자가 구매하고자 하는 재화·서비스의 수량.', 1, '미시/수요', NOW());

-- 6. 자본
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('자본', '생산활동에 투입되는 재무적·물적 자원 또는 기업의 순투자자 지분.', 1, '재무/성장', NOW());

-- 7. 부채
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('부채', '과거 사건으로 발생한 현재의무로, 미래에 경제적 자원 유출이 예상되는 항목.', 1, '재무회계', NOW());

-- 8. 순자산
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('순자산', '자산에서 부채를 차감한 잔여가치로, 소유주 지분을 의미.', 1, '재무회계', NOW());

-- 9. 이자
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('이자', '자본 사용의 대가로 지급되는 금액 또는 비율.', 1, '금융', NOW());

-- 10. 실질금리
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('실질금리', '명목금리에서 기대 또는 실제 인플레이션을 차감한 이자율.', 1, '거시/통화', NOW());

-- 11. 기준금리
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('기준금리', '중앙은행이 통화정책 수행을 위해 설정하는 대표적 단기 정책금리.', 1, '통화정책', NOW());

-- 12. 주식시장
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('주식시장', '기업의 지분증권이 발행·유통되는 자본시장 부문.', 1, '자본시장', NOW());

-- 13. 노동시장
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('노동시장', '노동력의 수요자(기업)와 공급자(가계)가 임금과 고용량을 결정하는 시장.', 1, '거시/노동', NOW());

-- 14. 시장가치
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('시장가치', '자유로운 경쟁 시장에서 정상거래로 실현될 것으로 기대되는 자산의 가치.', 1, '재무/평가', NOW());

-- 15. 통화
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('통화', '교환매개·가치저장·가치척도 기능을 수행하는 법정화폐 및 유사자산.', 1, '거시/통화체계', NOW());

-- 16. 외환
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('외환', '대외 결제에 사용되는 외국통화, 예치금, 어음 등 국제지급수단.', 1, '국제금융', NOW());

-- 17. 환리스크
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('환리스크', '환율 변동으로 인해 보유 외화자산·부채의 가치가 변동하는 위험.', 1, '금융/리스크', NOW());

-- 18. 공급
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('공급', '생산자가 주어진 가격에서 시장에 제공하려는 재화·서비스의 수량.', 1, '미시/공급', NOW());

-- 19. 수요곡선
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('수요곡선', '가격과 수요량의 역의 관계를 나타내는 그래프 또는 함수.', 1, '미시/수요', NOW());

-- 20. 수요탄력성
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('수요탄력성', '가격 변동에 대한 수요량의 민감도를 측정한 지표(탄력성 계수).', 1, '미시/탄력성', NOW());

-- 21. 금융
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융', '자금의 조달과 운용을 담당하는 경제활동의 한 분야.', 1, '금융', NOW());

-- 22. 리스크
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('리스크', '투자나 사업에서 발생할 수 있는 손실의 가능성.', 1, '금융/리스크', NOW());

-- 23. 금융권
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융권', '은행, 증권사, 보험사 등 금융기관들의 총칭.', 1, '금융', NOW());

-- 24. 이자율
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('이자율', '자본 사용의 대가로 지급되는 비율.', 1, '금융', NOW());

-- 25. 금융시장
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융시장', '자금의 수요자와 공급자가 만나 거래하는 시장.', 1, '금융', NOW());

-- 26. 금융정책
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융정책', '통화공급량과 금리를 조절하는 정책.', 1, '통화정책', NOW());

-- 27. 금융위기
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융위기', '금융시장의 불안정으로 인한 경제적 위기.', 1, '금융/위기', NOW());

-- 28. 금융감독
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융감독', '금융기관의 건전성을 감시하는 활동.', 1, '금융/감독', NOW());

-- 29. 금융개혁
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융개혁', '금융시스템을 개선하기 위한 제도적 변화.', 1, '금융/개혁', NOW());

-- 30. 금융혁신
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융혁신', '새로운 금융상품이나 서비스를 개발하는 활동.', 1, '금융/혁신', NOW());

-- 31. 금융자유화
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융자유화', '금융시장의 규제를 완화하는 정책.', 1, '금융/정책', NOW());

-- 32. 금융안정
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융안정', '금융시장의 안정적인 운영 상태.', 1, '금융/안정', NOW());

-- 33. 금융규제
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융규제', '금융기관의 활동을 제한하는 법규.', 1, '금융/규제', NOW());

-- 34. 금융서비스
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융서비스', '금융기관이 제공하는 다양한 서비스.', 1, '금융/서비스', NOW());

-- 35. 금융산업
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융산업', '금융 관련 경제활동의 총체.', 1, '금융/산업', NOW());

-- 36. 금융기관
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융기관', '금융업무를 수행하는 기관.', 1, '금융/기관', NOW());

-- 37. 금융상품
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융상품', '금융기관이 제공하는 상품.', 1, '금융/상품', NOW());

-- 38. 금융거래
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융거래', '금융상품의 매매나 거래.', 1, '금융/거래', NOW());

-- 39. 금융시스템
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융시스템', '금융활동을 지원하는 제도적 체계.', 1, '금융/시스템', NOW());

-- 40. 금융환경
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금융환경', '금융활동이 이루어지는 환경.', 1, '금융/환경', NOW());

-- 41. 금리정책 (리와 겹침)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금리정책', '중앙은행의 금리 조절 정책.', 1, '통화정책', NOW());

-- 42. 금리인상 (리와 겹침)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금리인상', '금리를 올리는 정책 조치.', 1, '통화정책', NOW());

-- 43. 금리인하 (리와 겹침)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금리인하', '금리를 내리는 정책 조치.', 1, '통화정책', NOW());

-- 44. 금리동결 (리와 겹침)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금리동결', '금리를 현재 수준으로 유지하는 정책.', 1, '통화정책', NOW());

-- 45. 금리상승 (리와 겹침)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금리상승', '금리가 올라가는 현상.', 1, '금융/시장', NOW());

-- 46. 금리하락 (리와 겹침)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금리하락', '금리가 내려가는 현상.', 1, '금융/시장', NOW());

-- 47. 금리변동 (리와 겹침)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('금리변동', '금리의 변화 현상.', 1, '금융/시장', NOW());

-- 48. 부동산 (산과 겹침)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('부동산', '토지와 그 정착물.', 1, '부동산', NOW());

-- 49. 자본시장 (장과 겹침)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('자본시장', '장기 자금의 수요와 공급이 이루어지는 시장.', 1, '자본시장', NOW());

-- 이제 모든 terms가 삽입된 후 glossaries 삽입
-- 1. 자산의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '경제적 효익을 제공하는 자원', NOW() FROM terms WHERE term = '자산';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '유형·무형으로 구분되는 재무상태표 항목', NOW() FROM terms WHERE term = '자산';

-- 2. 금리의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '돈의 가격을 나타내는 비율', NOW() FROM terms WHERE term = '금리';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '명목·실질로 구분되는 이자율', NOW() FROM terms WHERE term = '금리';

-- 3. 시장의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '거래가 이루어지는 메커니즘', NOW() FROM terms WHERE term = '시장';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '가격이 결정되는 집합적 장소', NOW() FROM terms WHERE term = '시장';

-- 4. 환율의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '통화 간 교환비율', NOW() FROM terms WHERE term = '환율';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '수출입 가격과 자본흐름에 영향', NOW() FROM terms WHERE term = '환율';

-- 5. 수요의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '가격에 따른 구매 의사·능력', NOW() FROM terms WHERE term = '수요';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '수요법칙: 가격↑→수요량↓(기타 불변)', NOW() FROM terms WHERE term = '수요';

-- 6. 자본의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '생산요소로서의 축적된 자원', NOW() FROM terms WHERE term = '자본';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '자본금·잉여금 등 소유주 지분', NOW() FROM terms WHERE term = '자본';

-- 7. 부채의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '상환 의무가 있는 채무', NOW() FROM terms WHERE term = '부채';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '유동·비유동 부채로 분류', NOW() FROM terms WHERE term = '부채';

-- 8. 순자산의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '자본=자산-부채', NOW() FROM terms WHERE term = '순자산';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '기업의 청산가치 지표', NOW() FROM terms WHERE term = '순자산';

-- 9. 이자의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '대부자본 사용료', NOW() FROM terms WHERE term = '이자';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '단리·복리 계산 가능', NOW() FROM terms WHERE term = '이자';

-- 10. 실질금리의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '물가상승을 반영한 금리', NOW() FROM terms WHERE term = '실질금리';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '피셔 방정식: r≈i-π', NOW() FROM terms WHERE term = '실질금리';

-- 11. 기준금리의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '중앙은행의 정책 신호금리', NOW() FROM terms WHERE term = '기준금리';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '시장금리와 대출·예금 금리에 파급', NOW() FROM terms WHERE term = '기준금리';

-- 12. 주식시장의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, 'IPO와 거래소 유통시장 포함', NOW() FROM terms WHERE term = '주식시장';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '가격발견과 자금조달 기능', NOW() FROM terms WHERE term = '주식시장';

-- 13. 노동시장의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '임금·고용이 결정되는 장', NOW() FROM terms WHERE term = '노동시장';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '마찰적·구조적 실업 존재', NOW() FROM terms WHERE term = '노동시장';

-- 14. 시장가치의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '공정가치와 유사 개념', NOW() FROM terms WHERE term = '시장가치';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '수요·공급과 유동성의 함수', NOW() FROM terms WHERE term = '시장가치';

-- 15. 통화의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '현금·예금 등 화폐공급 구성', NOW() FROM terms WHERE term = '통화';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '법정통화와 디지털화폐 포함 가능', NOW() FROM terms WHERE term = '통화';

-- 16. 외환의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '외화표시 자산·부채', NOW() FROM terms WHERE term = '외환';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '외환시장 현물·선물 포함', NOW() FROM terms WHERE term = '외환';

-- 17. 환리스크의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '거래·환산·경제적 노출 위험', NOW() FROM terms WHERE term = '환리스크';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '헤지: 선물·옵션·스왑 등', NOW() FROM terms WHERE term = '환리스크';

-- 18. 공급의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '가격↑→공급량↑(기타 불변)', NOW() FROM terms WHERE term = '공급';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '생산비·기술이 좌우', NOW() FROM terms WHERE term = '공급';

-- 19. 수요곡선의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '우하향 형태가 일반적', NOW() FROM terms WHERE term = '수요곡선';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '소득·기호 등 이동요인 존재', NOW() FROM terms WHERE term = '수요곡선';

-- 20. 수요탄력성의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '탄력적(>1)·비탄력적(<1) 구분', NOW() FROM terms WHERE term = '수요탄력성';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '가격탄력성·소득탄력성 등 유형', NOW() FROM terms WHERE term = '수요탄력성';

-- 21. 금융의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '자금의 융통과 신용 활동', NOW() FROM terms WHERE term = '금융';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '은행, 증권, 보험 등 포함', NOW() FROM terms WHERE term = '금융';

-- 22. 리스크의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '손실 발생 가능성', NOW() FROM terms WHERE term = '리스크';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '시장·신용·유동성 리스크 등', NOW() FROM terms WHERE term = '리스크';

-- 23. 금융권의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융기관들의 총칭', NOW() FROM terms WHERE term = '금융권';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '은행·증권·보험·카드사 등', NOW() FROM terms WHERE term = '금융권';

-- 24. 이자율의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '이자의 비율 지표', NOW() FROM terms WHERE term = '이자율';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '명목·실질 이자율 구분', NOW() FROM terms WHERE term = '이자율';

-- 25. 금융시장의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '자금 수요·공급 시장', NOW() FROM terms WHERE term = '금융시장';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '화폐·자본·외환시장 포함', NOW() FROM terms WHERE term = '금융시장';

-- 26. 금융정책의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '통화공급량과 금리 조절', NOW() FROM terms WHERE term = '금융정책';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '중앙은행의 주요 정책수단', NOW() FROM terms WHERE term = '금융정책';

-- 27. 금융위기의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융시장 불안정으로 인한 위기', NOW() FROM terms WHERE term = '금융위기';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '유동성 위기와 신용위기 포함', NOW() FROM terms WHERE term = '금융위기';

-- 28. 금융감독의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융기관 건전성 감시', NOW() FROM terms WHERE term = '금융감독';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '규제와 감독 체계', NOW() FROM terms WHERE term = '금융감독';

-- 29. 금융개혁의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융시스템 개선 변화', NOW() FROM terms WHERE term = '금융개혁';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '제도적 변화와 혁신', NOW() FROM terms WHERE term = '금융개혁';

-- 30. 금융혁신의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '새로운 금융상품 개발', NOW() FROM terms WHERE term = '금융혁신';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '핀테크와 디지털 금융', NOW() FROM terms WHERE term = '금융혁신';

-- 31. 금융자유화의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융규제 완화 정책', NOW() FROM terms WHERE term = '금융자유화';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '시장 개방과 경쟁 촉진', NOW() FROM terms WHERE term = '금융자유화';

-- 32. 금융안정의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융시장 안정성', NOW() FROM terms WHERE term = '금융안정';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '위험 관리와 감독', NOW() FROM terms WHERE term = '금융안정';

-- 33. 금융규제의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융기관 활동 제한', NOW() FROM terms WHERE term = '금융규제';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '법규와 감독 체계', NOW() FROM terms WHERE term = '금융규제';

-- 34. 금융서비스의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융기관 제공 서비스', NOW() FROM terms WHERE term = '금융서비스';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '다양한 금융 상품', NOW() FROM terms WHERE term = '금융서비스';

-- 35. 금융산업의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융 관련 경제활동', NOW() FROM terms WHERE term = '금융산업';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융 부문의 총체', NOW() FROM terms WHERE term = '금융산업';

-- 36. 금융기관의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융업무 수행 기관', NOW() FROM terms WHERE term = '금융기관';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '은행, 증권, 보험사 등', NOW() FROM terms WHERE term = '금융기관';

-- 37. 금융상품의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융기관 제공 상품', NOW() FROM terms WHERE term = '금융상품';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '다양한 금융 서비스', NOW() FROM terms WHERE term = '금융상품';

-- 38. 금융거래의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융상품 매매 거래', NOW() FROM terms WHERE term = '금융거래';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융시장에서의 거래', NOW() FROM terms WHERE term = '금융거래';

-- 39. 금융시스템의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융활동 지원 체계', NOW() FROM terms WHERE term = '금융시스템';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '제도적 금융 구조', NOW() FROM terms WHERE term = '금융시스템';

-- 40. 금융환경의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융활동 환경', NOW() FROM terms WHERE term = '금융환경';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금융시장 여건', NOW() FROM terms WHERE term = '금융환경';

-- 41. 금리정책의 glossaries (리와 겹침)
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '중앙은행의 금리 조절 정책', NOW() FROM terms WHERE term = '금리정책';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '통화정책의 핵심 수단', NOW() FROM terms WHERE term = '금리정책';

-- 42. 금리인상의 glossaries (리와 겹침)
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금리를 올리는 정책 조치', NOW() FROM terms WHERE term = '금리인상';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '인플레이션 억제 목적', NOW() FROM terms WHERE term = '금리인상';

-- 43. 금리인하의 glossaries (리와 겹침)
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금리를 내리는 정책 조치', NOW() FROM terms WHERE term = '금리인하';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '경기 부양 목적', NOW() FROM terms WHERE term = '금리인하';

-- 44. 금리동결의 glossaries (리와 겹침)
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금리를 현재 수준으로 유지', NOW() FROM terms WHERE term = '금리동결';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '정책 방향성 유지', NOW() FROM terms WHERE term = '금리동결';

-- 45. 금리상승의 glossaries (리와 겹침)
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금리가 올라가는 현상', NOW() FROM terms WHERE term = '금리상승';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '시장 금리 변동', NOW() FROM terms WHERE term = '금리상승';

-- 46. 금리하락의 glossaries (리와 겹침)
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금리가 내려가는 현상', NOW() FROM terms WHERE term = '금리하락';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '시장 금리 변동', NOW() FROM terms WHERE term = '금리하락';

-- 47. 금리변동의 glossaries (리와 겹침)
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '금리의 변화 현상', NOW() FROM terms WHERE term = '금리변동';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '시장 금리 변동', NOW() FROM terms WHERE term = '금리변동';

-- 48. 부동산의 glossaries (산과 겹침)
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '토지와 그 정착물', NOW() FROM terms WHERE term = '부동산';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '부동산 투자', NOW() FROM terms WHERE term = '부동산';

-- 49. 자본시장의 glossaries (장과 겹침)
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '장기 자금 시장', NOW() FROM terms WHERE term = '자본시장';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '주식·채권 시장', NOW() FROM terms WHERE term = '자본시장';

-- 3글자 이상 단어들 추가 (새로운 후보군 설정 알고리즘 테스트용)

-- 50. 인플레이션 (3글자)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('인플레이션', '일반적인 물가수준의 지속적 상승 현상으로 화폐의 구매력이 하락하는 경제적 상황.', 1, '거시/물가', NOW());

-- 51. 디플레이션 (3글자)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('디플레이션', '일반적인 물가수준의 지속적 하락 현상으로 경제활동이 위축되는 상황.', 1, '거시/물가', NOW());

-- 52. 스태그플레이션 (4글자)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('스태그플레이션', '경기침체와 물가상승이 동시에 발생하는 경제적 현상.', 1, '거시/경기', NOW());

-- 53. 인플레이션의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '물가상승으로 화폐가치 하락', NOW() FROM terms WHERE term = '인플레이션';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '수요견인·비용상승 인플레이션', NOW() FROM terms WHERE term = '인플레이션';

-- 54. 디플레이션의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '물가하락으로 경제위축', NOW() FROM terms WHERE term = '디플레이션';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '수요부족과 경기침체', NOW() FROM terms WHERE term = '디플레이션';

-- 55. 스태그플레이션의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '경기침체와 물가상승 동시발생', NOW() FROM terms WHERE term = '스태그플레이션';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '정책 대응이 어려운 상황', NOW() FROM terms WHERE term = '스태그플레이션';

-- 56. 통화정책 (3글자)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('통화정책', '중앙은행이 통화공급량과 금리를 조절하여 경제안정을 도모하는 정책.', 1, '통화정책', NOW());

-- 57. 재정정책 (3글자)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('재정정책', '정부가 세입과 세출을 조절하여 경제안정과 성장을 도모하는 정책.', 1, '재정정책', NOW());

-- 58. 통화정책의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '중앙은행의 금리·통화량 조절', NOW() FROM terms WHERE term = '통화정책';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '경기조절과 물가안정 목적', NOW() FROM terms WHERE term = '통화정책';

-- 59. 재정정책의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '정부의 세입세출 조절', NOW() FROM terms WHERE term = '재정정책';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '경기부양과 소득재분배', NOW() FROM terms WHERE term = '재정정책';

-- 60. 자본주의 (3글자)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('자본주의', '사유재산제도와 자유시장경제를 기반으로 한 경제체제.', 1, '경제체제', NOW());

-- 61. 사회주의 (3글자)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('사회주의', '생산수단의 공유와 계획경제를 특징으로 하는 경제체제.', 1, '경제체제', NOW());

-- 62. 자본주의의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '사유재산과 자유시장 경제', NOW() FROM terms WHERE term = '자본주의';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '경쟁과 이윤동기 중심', NOW() FROM terms WHERE term = '자본주의';

-- 63. 사회주의의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '생산수단 공유와 계획경제', NOW() FROM terms WHERE term = '사회주의';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '평등과 공동체 중심', NOW() FROM terms WHERE term = '사회주의';

-- 64. 경제성장 (3글자)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('경제성장', '국민경제의 생산능력이 증가하여 실질국민소득이 증대하는 현상.', 1, '거시/성장', NOW());

-- 65. 경제위기 (3글자)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('경제위기', '경제활동이 급격히 위축되어 실업과 파산이 증가하는 상황.', 1, '거시/위기', NOW());

-- 66. 경제성장의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '경제성장 실질GDP 증가와 생산능력 확대', NOW() FROM terms WHERE term = '경제성장';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '자본축적과 기술진보가 핵심', NOW() FROM terms WHERE term = '경제성장';

-- 67. 경제위기의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '경기침체와 경제활동 위축', NOW() FROM terms WHERE term = '경제위기';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '실업증가와 소득감소', NOW() FROM terms WHERE term = '경제위기';

-- 68. 시장경제 (3글자)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('시장경제', '수요와 공급의 자유로운 작동에 의해 가격과 생산량이 결정되는 경제체제.', 1, '경제체제', NOW());

-- 69. 계획경제 (3글자)
INSERT IGNORE INTO terms (term, description, frequency, category, created_at)
VALUES ('계획경제', '정부가 중앙에서 경제활동을 계획하고 통제하는 경제체제.', 1, '경제체제', NOW());

-- 70. 시장경제의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '수요공급에 의한 자유경쟁', NOW() FROM terms WHERE term = '시장경제';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '가격기구의 자동조절 기능', NOW() FROM terms WHERE term = '시장경제';

-- 71. 계획경제의 glossaries
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '정부의 중앙집권적 계획', NOW() FROM terms WHERE term = '계획경제';
INSERT IGNORE INTO glossaries (term_id, short_def, created_at)
SELECT term_id, '자원배분의 정부통제', NOW() FROM terms WHERE term = '계획경제';
