-- 사용자 알림 설정 컬럼 추가.
-- 기존 사용자는 기본값(예약 문장 알림 켜짐, 마케팅 꺼짐)으로 채운다.
ALTER TABLE users
    ADD COLUMN quote_alarm bit(1) NOT NULL DEFAULT 1,
    ADD COLUMN marketing   bit(1) NOT NULL DEFAULT 0;
