package com.izida.backend

import java.math.BigDecimal
import java.util.UUID

data class TransferResult(val transactionId: UUID, val amount: BigDecimal, val currency: String, val status: String)

class TransferService {
    fun transfer(userId: UUID, recipientPhone: String, amount: BigDecimal, currency: String, idempotencyKey: String, reference: String? = null): TransferResult {
        require(recipientPhone.matches(Regex("[0-9]{9}"))) { "Celular inválido" }
        val destinationResolver: (java.sql.Connection) -> Account? = { c -> findAccountByPhone(c, recipientPhone) }
        return executeTransfer(userId, destinationResolver, amount, currency, idempotencyKey, reference)
    }

    fun transferToAccount(userId: UUID, destinationAccountId: UUID, amount: BigDecimal, currency: String, idempotencyKey: String, reference: String? = null): TransferResult {
        val destinationResolver: (java.sql.Connection) -> Account? = { c -> findAccountById(c, destinationAccountId) }
        return executeTransfer(userId, destinationResolver, amount, currency, idempotencyKey, reference)
    }

    private fun executeTransfer(userId: UUID, destinationResolver: (java.sql.Connection) -> Account?, amount: BigDecimal, currency: String, idempotencyKey: String, reference: String?): TransferResult {
        require(amount > BigDecimal.ZERO) { "El monto debe ser mayor que cero" }
        require(amount.scale() <= 2) { "El monto admite hasta 2 decimales" }
        require(currency == "PEN") { "Moneda no soportada" }
        require(idempotencyKey.isNotBlank() && idempotencyKey.length <= 120) { "Idempotencia inválida" }

        Database.connection().use { c ->
            c.autoCommit = false
            try {
                val existing = c.prepareStatement("SELECT id, amount, currency, status FROM ledger_transactions WHERE idempotency_key=?").use { s ->
                    s.setString(1, idempotencyKey)
                    s.executeQuery().use { rs ->
                        if (rs.next()) TransferResult(rs.getObject("id", UUID::class.java), rs.getBigDecimal("amount"), rs.getString("currency").trim(), rs.getString("status")) else null
                    }
                }
                if (existing != null) { c.commit(); return existing }

                val source = findAccount(c, userId) ?: throw IllegalArgumentException("Cuenta de origen no encontrada")
                val destination = destinationResolver(c) ?: throw IllegalArgumentException("Destinatario no encontrado")
                require(source.id != destination.id) { "No puedes enviarte dinero a tu propia cuenta" }
                require(source.currency == currency && destination.currency == currency) { "Moneda no soportada" }
                require(source.status == "ACTIVE" && destination.status == "ACTIVE") { "Cuenta no disponible" }

                val balance = balance(c, source.id)
                require(balance >= amount) { "Saldo insuficiente" }

                val txId = UUID.randomUUID()
                c.prepareStatement("INSERT INTO ledger_transactions(id,idempotency_key,source_account_id,destination_account_id,amount,currency,status,reference) VALUES (?,?,?,?,?,?,?,?)").use { s ->
                    s.setObject(1,txId); s.setString(2,idempotencyKey); s.setObject(3,source.id); s.setObject(4,destination.id)
                    s.setBigDecimal(5,amount); s.setString(6,currency); s.setString(7,"PROCESSED"); s.setString(8,reference)
                    s.executeUpdate()
                }
                insertEntry(c, txId, source.id, "DEBIT", amount, currency)
                insertEntry(c, txId, destination.id, "CREDIT", amount, currency)
                c.commit()
                return TransferResult(txId, amount, currency, "PROCESSED")
            } catch (e: Exception) {
                runCatching { c.rollback() }
                throw e
            }
        }
    }

    private data class Account(val id: UUID, val currency: String, val status: String)

    private fun findAccount(c: java.sql.Connection, userId: UUID): Account? =
        c.prepareStatement("SELECT id,currency,status FROM accounts WHERE user_id=? LIMIT 1 FOR UPDATE").use { s ->
            s.setObject(1,userId); s.executeQuery().use { rs -> if (rs.next()) Account(rs.getObject("id",UUID::class.java),rs.getString("currency").trim(),rs.getString("status")) else null }
        }

    private fun findAccountByPhone(c: java.sql.Connection, phone: String): Account? =
        c.prepareStatement("SELECT a.id,a.currency,a.status FROM accounts a JOIN izida_users u ON u.id=a.user_id WHERE u.phone=? LIMIT 1").use { s ->
            s.setString(1,phone); s.executeQuery().use { rs -> if (rs.next()) Account(rs.getObject("id",UUID::class.java),rs.getString("currency").trim(),rs.getString("status")) else null }
        }

    private fun findAccountById(c: java.sql.Connection, accountId: UUID): Account? =
        c.prepareStatement("SELECT id,currency,status FROM accounts WHERE id=? LIMIT 1").use { s ->
            s.setObject(1,accountId); s.executeQuery().use { rs -> if (rs.next()) Account(rs.getObject("id",UUID::class.java),rs.getString("currency").trim(),rs.getString("status")) else null }
        }

    private fun balance(c: java.sql.Connection, accountId: UUID): BigDecimal =
        c.prepareStatement("SELECT COALESCE(SUM(CASE WHEN entry_type='CREDIT' THEN amount WHEN entry_type='DEBIT' THEN -amount ELSE 0 END),0) FROM ledger_entries WHERE account_id=?").use { s ->
            s.setObject(1,accountId); s.executeQuery().use { rs -> rs.next(); rs.getBigDecimal(1) }
        }

    private fun insertEntry(c: java.sql.Connection, txId: UUID, accountId: UUID, type: String, amount: BigDecimal, currency: String) {
        c.prepareStatement("INSERT INTO ledger_entries(id,transaction_id,account_id,entry_type,amount,currency) VALUES (?,?,?,?,?,?)").use { s ->
            s.setObject(1,UUID.randomUUID()); s.setObject(2,txId); s.setObject(3,accountId); s.setString(4,type); s.setBigDecimal(5,amount); s.setString(6,currency)
            s.executeUpdate()
        }
    }
}