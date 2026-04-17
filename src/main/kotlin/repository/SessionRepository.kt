package com.fittrack.repository

import com.fittrack.db.*
import com.fittrack.model.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class SessionRepository {

    fun logSession(userId: String, request: SessionLogRequest): SessionLogResponse = transaction {
        val uid = UUID.fromString(userId)
        val sessionId = UUID.randomUUID()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        SessionLogsTable.insert {
            it[id] = EntityID(sessionId, SessionLogsTable)
            it[SessionLogsTable.userId] = EntityID(uid, UsersTable)
            it[trainingDayId] = EntityID(UUID.fromString(request.trainingDayId), TrainingDaysTable)
            it[date] = LocalDate.parse(request.date)
            it[durationMinutes] = request.durationMinutes
            it[notes] = request.notes
            it[createdAt] = now
        }

        request.sets.forEach { set ->
            SetLogsTable.insert {
                it[id] = EntityID(UUID.randomUUID(), SetLogsTable)
                it[sessionLogId] = EntityID(sessionId, SessionLogsTable)
                it[exerciseId] = EntityID(UUID.fromString(set.exerciseId), ExercisesTable)
                it[setIndex] = set.setIndex
                it[reps] = set.reps
                it[weightKg] = set.weightKg
                it[durationSeconds] = set.durationSeconds
                it[completed] = set.completed
            }
        }

        getSession(sessionId.toString())
    }

    fun getSessions(userId: String): List<SessionLogResponse> = transaction {
        val uid = UUID.fromString(userId)
        SessionLogsTable.selectAll()
            .where { SessionLogsTable.userId eq uid }
            .orderBy(SessionLogsTable.date, SortOrder.DESC)
            .map { getSession(it[SessionLogsTable.id].value.toString()) }
    }

    fun deleteSession(sessionId: String) = transaction {
        val sid = UUID.fromString(sessionId)
        SetLogsTable.deleteWhere { sessionLogId eq sid }
        SessionLogsTable.deleteWhere { id eq sid }
    }

    fun getSession(sessionId: String): SessionLogResponse = transaction {
        val sid = UUID.fromString(sessionId)
        val session = SessionLogsTable.selectAll()
            .where { SessionLogsTable.id eq sid }
            .first()

        val dayName = TrainingDaysTable.selectAll()
            .where { TrainingDaysTable.id eq session[SessionLogsTable.trainingDayId] }
            .first()[TrainingDaysTable.name]

        val sets = (SetLogsTable innerJoin ExercisesTable)
            .selectAll()
            .where { SetLogsTable.sessionLogId eq sid }
            .orderBy(SetLogsTable.setIndex)
            .map { row ->
                SetLogResponse(
                    id = row[SetLogsTable.id].value.toString(),
                    exerciseId = row[SetLogsTable.exerciseId].value.toString(),
                    exerciseName = row[ExercisesTable.name],
                    setIndex = row[SetLogsTable.setIndex],
                    reps = row[SetLogsTable.reps],
                    weightKg = row[SetLogsTable.weightKg],
                    durationSeconds = row[SetLogsTable.durationSeconds],
                    completed = row[SetLogsTable.completed]
                )
            }

        SessionLogResponse(
            id = sessionId,
            trainingDayId = session[SessionLogsTable.trainingDayId].value.toString(),
            trainingDayName = dayName,
            date = session[SessionLogsTable.date].toString(),
            durationMinutes = session[SessionLogsTable.durationMinutes],
            notes = session[SessionLogsTable.notes],
            sets = sets
        )
    }

    fun getStats(userId: String): StatsResponse = transaction {
        val uid = UUID.fromString(userId)
        val sessions = SessionLogsTable.selectAll()
            .where { SessionLogsTable.userId eq uid }
            .orderBy(SessionLogsTable.date)
            .toList()

        val totalSessions = sessions.size
        val dates = sessions.map { it[SessionLogsTable.date] }.toSortedSet()

        var bestStreak = 0
        var streak = 0
        var prev: LocalDate? = null
        dates.forEach { date ->
            streak = if (prev != null && date == prev!!.plusDays(1)) streak + 1 else 1
            if (streak > bestStreak) bestStreak = streak
            prev = date
        }

        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        val currentStreak = if (dates.isNotEmpty() && dates.last() >= today.plusDays(-1)) {
            var s = 0
            var d = today
            while (dates.contains(d)) { s++; d = d.plusDays(-1) }
            s
        } else 0

        val progressByExercise = (SetLogsTable innerJoin SessionLogsTable innerJoin ExercisesTable)
            .selectAll()
            .where { SessionLogsTable.userId eq uid and (SetLogsTable.completed eq true) }
            .groupBy { it[ExercisesTable.id].value.toString() }
            .map { (exId, rows) ->
                val exName = rows.first()[ExercisesTable.name]
                val byDate = rows.groupBy { it[SessionLogsTable.date].toString() }
                ExerciseProgress(
                    exerciseId = exId,
                    exerciseName = exName,
                    history = byDate.map { (date, setRows) ->
                        ExerciseProgressPoint(
                            date = date,
                            maxWeightKg = setRows.mapNotNull { it[SetLogsTable.weightKg] }.maxOrNull(),
                            totalReps = setRows.sumOf { it[SetLogsTable.reps] ?: 0 }
                        )
                    }.sortedBy { it.date }
                )
            }

        StatsResponse(totalSessions, currentStreak, bestStreak, progressByExercise)
    }
}

private fun LocalDate.plusDays(days: Int): LocalDate =
    LocalDate.fromEpochDays(this.toEpochDays() + days)