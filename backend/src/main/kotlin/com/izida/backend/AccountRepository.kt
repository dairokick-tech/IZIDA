package com.izida.backend

import java.math.BigDecimal
import java.util.UUID

data class AccountSnapshot(val id: UUID, val userId: UUID, val currency: String, val status: String, val balance: BigDecimal, val fullName: String)
data class MovementSnapshot(val id: UUID, val transactionId: UUID, val type: String, val amount: BigDecimal, val currency: String, val createdAt: String, val reference: String?, val status: String)

class AccountRepository {
    fun findAccount(accountId: UUID): AccountSnapshot? {
        Database.connection().use { c ->
            c.prepareStatement("""SELECT a.id,a.user_id,a.currency,a.status,u.full_name,COALESCE(SUM(CASE WHEN e.entry_type='CREDIT' THEN e.amount WHEN e.entry_type='DEBIT' THEN -e.amount ELSE 0 END),0) balance FROM accounts a JOIN izida_users u ON u.id=a.user_id LEFT JOIN ledger_entries e ON e.account_id=a.id WHERE a.id=? GROUP BY a.id,a.user_id,a.currency,a.status,u.full_name""").use { s ->
                s.setObject(1, accountId); s.executeQuery().use { rs ->
                    if (!rs.next()) return null
                    return AccountSnapshot(rs.getObject("id",UUID::class.java),rs.getObject("user_id",UUID::class.java),rs.getString("currency").trim(),rs.getString("status"),rs.getBigDecimal("balance"),rs.getString("full_name"))
                }
            }
        }
    }
    fun movements(accountId: UUID, limit: Int): List<MovementSnapshot> {
        Database.connection().use { c ->
            c.prepareStatement("""SELECT e.id,e.transaction_id,e.entry_type,e.amount,e.currency,e.created_at,t.reference,t.status FROM ledger_entries e JOIN ledger_transactions t ON t.id=e.transaction_id WHERE e.account_id=? ORDER BY e.created_at DESC LIMIT ?""").use { s ->
                s.setObject(1,accountId); s.setInt(2,limit.coerceIn(1,100)); s.executeQuery().use { rs ->
                    val out=mutableListOf<MovementSnapshot>()
                    while(rs.next()) out += MovementSnapshot(rs.getObject("id",UUID::class.java),rs.getObject("transaction_id",UUID::class.java),rs.getString("entry_type"),rs.getBigDecimal("amount"),rs.getString("currency").trim(),rs.getTimestamp("created_at").toInstant().toString(),rs.getString("reference"),rs.getString("status"))
                    return out
                }
            }
        }
    }
    fun findAccountByUserId(userId: UUID): AccountSnapshot? {
        Database.connection().use { c ->
            c.prepareStatement("""SELECT a.id,a.user_id,a.currency,a.status,u.full_name,COALESCE(SUM(CASE WHEN e.entry_type='CREDIT' THEN e.amount WHEN e.entry_type='DEBIT' THEN -e.amount ELSE 0 END),0) balance FROM accounts a JOIN izida_users u ON u.id=a.user_id LEFT JOIN ledger_entries e ON e.account_id=a.id WHERE a.user_id=? GROUP BY a.id,a.user_id,a.currency,a.status,u.full_name ORDER BY a.created_at ASC LIMIT 1""").use { s ->
                s.setObject(1,userId); s.executeQuery().use { rs ->
                    if(!rs.next()) return null
                    return AccountSnapshot(rs.getObject("id",UUID::class.java),rs.getObject("user_id",UUID::class.java),rs.getString("currency").trim(),rs.getString("status"),rs.getBigDecimal("balance"),rs.getString("full_name"))
                }
            }
        }
    }
}