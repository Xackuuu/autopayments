-- =============================================
-- Таблица для хранения идемпотентных ключей
-- =============================================

CREATE TABLE IF NOT EXISTS autopayment.payment_executions (
    -- Уникальный ключ платежа (генерируется клиентом)
    idempotency_key VARCHAR(100) PRIMARY KEY,

    -- Кто платил
    user_id BIGINT NOT NULL,

    -- Сумма
    amount DECIMAL(19,2) NOT NULL,

    -- Статус: 'SUCCESS' или 'FAILED'
    status VARCHAR(20) NOT NULL,

    -- Когда выполнен
    executed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Результат (текст ошибки или ID транзакции)
    result TEXT
);

-- Индекс для быстрого поиска по времени
CREATE INDEX idx_payment_executions_executed_at
    ON autopayment.payment_executions(executed_at);