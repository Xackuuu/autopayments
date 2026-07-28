-- =============================================
-- Индексы для таблицы autopay_rule
-- =============================================

-- 1. Основной индекс для поиска активных правил
-- Используется в findByEnabledTrue() каждые 10 секунд
CREATE INDEX IF NOT EXISTS idx_autopay_rule_deleted_enabled
    ON autopayment.autopay_rule(deleted_at, enabled);

-- 2. Индекс для поиска по пользователю
-- Будет использоваться, когда добавим API "все правила пользователя"
CREATE INDEX IF NOT EXISTS idx_autopay_rule_user_id
    ON autopayment.autopay_rule(user_id);

-- 3. Индекс для поиска по last_executed_at
-- Понадобится для аналитики (сколько правил выполнялось за день)
CREATE INDEX IF NOT EXISTS idx_autopay_rule_last_executed
    ON autopayment.autopay_rule(last_executed_at);

-- =============================================
-- Индексы для таблицы payment_executions
-- =============================================

-- 4. Индекс для очистки старых записей
-- Например, удалять записи старше 30 дней
CREATE INDEX IF NOT EXISTS idx_payment_executions_executed_at
    ON autopayment.payment_executions(executed_at);