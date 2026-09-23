CREATE TABLE IF NOT EXISTS members (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(30) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    membership_type VARCHAR(30) NOT NULL CHECK (membership_type IN ('BASIC', 'PREMIUM', 'ANNUAL')),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS training_registrations (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE RESTRICT,
    training_name VARCHAR(120) NOT NULL,
    trainer_name VARCHAR(120) NOT NULL,
    training_date DATE NOT NULL,
    start_time TIME NOT NULL,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes BETWEEN 30 AND 240),
    status VARCHAR(30) NOT NULL CHECK (status IN ('PLANNED', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    category VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_registration_member_slot
    ON training_registrations (member_id, training_date, start_time);

INSERT INTO members (full_name, phone, email, membership_type) VALUES
('Иванов Иван', '+79990000001', 'ivanov@example.com', 'PREMIUM'),
('Петрова Анна', '+79990000002', 'petrova@example.com', 'ANNUAL'),
('Сидоров Максим', '+79990000003', 'sidorov@example.com', 'BASIC'),
('Кузнецова Мария', '+79990000004', 'kuznetsova@example.com', 'PREMIUM'),
('Орлов Дмитрий', '+79990000005', 'orlov@example.com', 'BASIC')
ON CONFLICT DO NOTHING;

INSERT INTO training_registrations
(member_id, training_name, trainer_name, training_date, start_time, duration_minutes, status, category)
SELECT m.id, v.training_name, v.trainer_name, v.training_date::date, v.start_time::time,
       v.duration_minutes, v.status, v.category
FROM members m
JOIN (VALUES
    ('ivanov@example.com', 'Функциональный тренинг', 'Смирнов Алексей', '2026-10-01', '18:00', 60, 'CONFIRMED', 'FUNCTIONAL'),
    ('petrova@example.com', 'Йога', 'Волкова Елена', '2026-10-02', '10:00', 90, 'PLANNED', 'YOGA'),
    ('sidorov@example.com', 'Плавание', 'Морозов Павел', '2026-10-03', '09:00', 60, 'COMPLETED', 'SWIMMING'),
    ('kuznetsova@example.com', 'Силовая тренировка', 'Смирнов Алексей', '2026-10-04', '19:00', 75, 'CONFIRMED', 'STRENGTH'),
    ('orlov@example.com', 'Кардио', 'Волкова Елена', '2026-10-05', '17:30', 45, 'CANCELLED', 'CARDIO'),
    ('ivanov@example.com', 'Йога', 'Волкова Елена', '2026-10-06', '11:00', 90, 'PLANNED', 'YOGA'),
    ('petrova@example.com', 'Плавание', 'Морозов Павел', '2026-10-07', '08:30', 60, 'CONFIRMED', 'SWIMMING'),
    ('sidorov@example.com', 'Кардио', 'Смирнов Алексей', '2026-10-08', '18:30', 45, 'PLANNED', 'CARDIO'),
    ('kuznetsova@example.com', 'Функциональный тренинг', 'Смирнов Алексей', '2026-10-09', '20:00', 60, 'PLANNED', 'FUNCTIONAL'),
    ('orlov@example.com', 'Силовая тренировка', 'Морозов Павел', '2026-10-10', '12:00', 75, 'CONFIRMED', 'STRENGTH')
) AS v(email, training_name, trainer_name, training_date, start_time, duration_minutes, status, category)
ON m.email = v.email
WHERE NOT EXISTS (SELECT 1 FROM training_registrations);
