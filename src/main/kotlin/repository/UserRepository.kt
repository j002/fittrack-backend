package com.fittrack.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import com.fittrack.db.UsersTable
import com.fittrack.plugins.BadRequestException
import com.fittrack.plugins.ConflictException
import com.fittrack.plugins.NotFoundException
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class UserRecord(val id: String, val email: String, val name: String)

class UserRepository {

    fun register(email: String, password: String, name: String): UserRecord = transaction {
        val existing = UsersTable.selectAll()
            .where { UsersTable.email eq email.lowercase() }
            .firstOrNull()

        if (existing != null) throw ConflictException("Email déjà utilisé")

        val hash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        val id = UUID.randomUUID()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        UsersTable.insert {
            it[UsersTable.id] = EntityID(id, UsersTable)
            it[UsersTable.email] = email.lowercase()
            it[UsersTable.passwordHash] = hash
            it[UsersTable.name] = name
            it[createdAt] = now
        }

        UserRecord(id.toString(), email, name)
    }

    fun login(email: String, password: String): UserRecord = transaction {
        val row = UsersTable.selectAll()
            .where { UsersTable.email eq email.lowercase() }
            .firstOrNull()
            ?: throw NotFoundException("Utilisateur introuvable")

        val valid = BCrypt.verifyer().verify(
            password.toCharArray(),
            row[UsersTable.passwordHash]
        ).verified

        if (!valid) throw BadRequestException("Mot de passe incorrect")

        UserRecord(
            row[UsersTable.id].value.toString(),
            row[UsersTable.email],
            row[UsersTable.name]
        )
    }
}