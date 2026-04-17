package com.fittrack.repository

import com.fittrack.db.ExercisesTable
import com.fittrack.db.TrainingDaysTable
import com.fittrack.db.UsersTable
import com.fittrack.model.ExerciseResponse
import com.fittrack.model.TrainingDayRequest
import com.fittrack.model.TrainingDayResponse
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ProgramRepository {

    fun getProgram(userId: String): List<TrainingDayResponse> = transaction {
        val uid = UUID.fromString(userId)
        TrainingDaysTable.selectAll()
            .where { TrainingDaysTable.userId eq uid }
            .orderBy(TrainingDaysTable.orderIndex)
            .map { dayRow ->
                val dayId = dayRow[TrainingDaysTable.id].value
                val exercises = ExercisesTable.selectAll()
                    .where { ExercisesTable.trainingDayId eq dayId }
                    .orderBy(ExercisesTable.orderIndex)
                    .map { exRow ->
                        ExerciseResponse(
                            id = exRow[ExercisesTable.id].value.toString(),
                            name = exRow[ExercisesTable.name],
                            targetSets = exRow[ExercisesTable.targetSets],
                            targetReps = exRow[ExercisesTable.targetReps],
                            targetWeightKg = exRow[ExercisesTable.targetWeightKg],
                            durationSeconds = exRow[ExercisesTable.durationSeconds],
                            orderIndex = exRow[ExercisesTable.orderIndex]
                        )
                    }
                TrainingDayResponse(
                    id = dayId.toString(),
                    dayOfWeek = dayRow[TrainingDaysTable.dayOfWeek],
                    name = dayRow[TrainingDaysTable.name],
                    orderIndex = dayRow[TrainingDaysTable.orderIndex],
                    exercises = exercises
                )
            }
    }

    fun upsertProgram(userId: String, days: List<TrainingDayRequest>): List<TrainingDayResponse> = transaction {
        val uid = UUID.fromString(userId)

        val existingDayIds = TrainingDaysTable.selectAll()
            .where { TrainingDaysTable.userId eq uid }
            .map { it[TrainingDaysTable.id].value }

        ExercisesTable.deleteWhere { trainingDayId inList existingDayIds }
        TrainingDaysTable.deleteWhere { TrainingDaysTable.userId eq uid }

        days.forEach { day ->
            val dayId = UUID.randomUUID()
            TrainingDaysTable.insert {
                it[id] = EntityID(dayId, TrainingDaysTable)
                it[TrainingDaysTable.userId] = EntityID(uid, UsersTable)
                it[dayOfWeek] = day.dayOfWeek
                it[name] = day.name
                it[orderIndex] = day.orderIndex
            }
            day.exercises.forEach { ex ->
                ExercisesTable.insert {
                    it[id] = EntityID(UUID.randomUUID(), ExercisesTable)
                    it[trainingDayId] = EntityID(dayId, TrainingDaysTable)
                    it[name] = ex.name
                    it[targetSets] = ex.targetSets
                    it[targetReps] = ex.targetReps
                    it[targetWeightKg] = ex.targetWeightKg
                    it[durationSeconds] = ex.durationSeconds
                    it[orderIndex] = ex.orderIndex
                }
            }
        }

        getProgram(userId)
    }
}