package com.tryfirst.api.services

import com.tryfirst.api.models.PracticeHistoryItem
import com.tryfirst.api.models.StatsResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
/**
 * Database table - sama persis dengan PracticeEntity di Android kamu
 * Bedanya pakai Exposed (Kotlin ORM untuk server) bukan Room (untuk Android)
 */
object PracticeTable : Table("practice_history") {
    val id        = integer("id").autoIncrement()
    val type      = varchar("type", 50)          // "writing" atau "speaking"
    val userInput = text("user_input")
    val feedback  = text("feedback")
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(id)
}

class PracticeRepository {

    // Sama dengan insert() di PracticeDao Android
    fun save(type: String, userInput: String, feedback: String): PracticeHistoryItem {
        return transaction {
            val insertedId = PracticeTable.insert {
                it[PracticeTable.type]      = type
                it[PracticeTable.userInput] = userInput
                it[PracticeTable.feedback]  = feedback
                it[PracticeTable.timestamp] = System.currentTimeMillis()
            } get PracticeTable.id

            PracticeTable
                .select { PracticeTable.id eq insertedId }
                .single()
                .toPracticeHistoryItem()
        }
    }

    // Sama dengan getAllHistory() di PracticeDao Android
    fun getAll(): List<PracticeHistoryItem> {
        return transaction {
            PracticeTable
                .selectAll()
                .orderBy(PracticeTable.timestamp, SortOrder.DESC)
                .map { it.toPracticeHistoryItem() }
        }
    }

    // Sama dengan getTotalCount() di PracticeDao Android
    fun getStats(): StatsResponse {
        return transaction {
            val total   = PracticeTable.selectAll().count().toInt()
            val writing = PracticeTable.select { PracticeTable.type eq "writing" }.count().toInt()
            val speaking = PracticeTable.select { PracticeTable.type eq "speaking" }.count().toInt()
            StatsResponse(total, writing, speaking)
        }
    }

    // Sama dengan delete() di PracticeDao Android
    fun delete(id: Int): Boolean {
        return transaction {
            PracticeTable.deleteWhere { PracticeTable.id eq id } > 0
        }
    }

    // Sama dengan deleteAll() di PracticeDao Android
    fun deleteAll() {
        transaction {
            PracticeTable.deleteAll()
        }
    }

    private fun ResultRow.toPracticeHistoryItem() = PracticeHistoryItem(
        id        = this[PracticeTable.id],
        type      = this[PracticeTable.type],
        userInput = this[PracticeTable.userInput],
        feedback  = this[PracticeTable.feedback],
        timestamp = this[PracticeTable.timestamp]
    )
}
