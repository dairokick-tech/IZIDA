package com.izida.backend

import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class AuthUser(val id: UUID, val fullName: String, val phone: String)
data class AuthResult(val user: AuthUser, val token: String)

class AuthService {
    fun register(fullName: String, phone: String, pin: String): AuthResult {
        require(fullName.trim().length >= 3) { "Nombre inválido" }
        require(phone.matches(Regex("[0-9]{9}"))) { "Celular inválido" }
        require(pin.matches(Regex("[0-9]{6}"))) { "PIN inválido" }

        val userId = UUID.randomUUID()
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hash(pin, salt)

        Database.connection().use { c ->
            c.prepareStatement(
                "INSERT INTO izida_users(id,full_name,phone,pin_salt,pin_hash) VALUES (?,?,?,?,?)"
            ).use { s ->
                s.setObject(1,userId); s.setString(2,fullName.trim()); s.setString(3,phone)
                s.setBytes(4,salt); s.setBytes(5,hash); s.executeUpdate()
            }
            c.commit()
        }
        return createSession(userId, fullName.trim(), phone)
    }

    fun login(phone: String, pin: String): AuthResult? {
        Database.connection().use { c ->
            c.prepareStatement("SELECT id,full_name,pin_salt,pin_hash,status FROM izida_users WHERE phone=?").use { s ->
                s.setString(1,phone)
                s.executeQuery().use { rs ->
                    if(!rs.next() || rs.getString("status")!="ACTIVE") return null
                    val candidate=hash(pin,rs.getBytes("pin_salt"))
                    if(!MessageDigest.isEqual(candidate,rs.getBytes("pin_hash"))) return null
                    return createSession(UUID.fromString(rs.getObject("id").toString()),rs.getString("full_name"),phone)
                }
            }
        }
    }

    fun userForToken(token:String):AuthUser? {
        val hash=sha256(token)
        Database.connection().use { c ->
            c.prepareStatement("""
                SELECT u.id,u.full_name,u.phone
                FROM auth_sessions s JOIN izida_users u ON u.id=s.user_id
                WHERE s.token_hash=? AND s.revoked_at IS NULL AND s.expires_at > now()
            """.trimIndent()).use { s ->
                s.setBytes(1,hash); s.executeQuery().use { rs ->
                    if(!rs.next()) return null
                    return AuthUser(UUID.fromString(rs.getObject("id").toString()),rs.getString("full_name"),rs.getString("phone"))
                }
            }
        }
    }

    private fun createSession(id:UUID,name:String,phone:String):AuthResult {
        val raw=ByteArray(32).also{SecureRandom().nextBytes(it)}
        val token=Base64.getUrlEncoder().withoutPadding().encodeToString(raw)
        Database.connection().use { c ->
            c.prepareStatement("INSERT INTO auth_sessions(id,user_id,token_hash,expires_at) VALUES (?,?,?,?)").use { s ->
                s.setObject(1,UUID.randomUUID()); s.setObject(2,id); s.setBytes(3,sha256(token))
                s.setObject(4,java.sql.Timestamp.from(Instant.now().plus(12,ChronoUnit.HOURS))); s.executeUpdate(); c.commit()
            }
        }
        return AuthResult(AuthUser(id,name,phone),token)
    }

    private fun hash(pin:String,salt:ByteArray):ByteArray {
        val spec=PBEKeySpec(pin.toCharArray(),salt,120_000,256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded } finally { spec.clearPassword() }
    }
    private fun sha256(value:String)=MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
}
