package com.fittrack.db

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object UsersTable : UUIDTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val name = varchar("name", 100)
    val createdAt = datetime("created_at")
}

object TrainingDaysTable : UUIDTable("training_days") {
    val userId = reference("user_id", UsersTable)
    val dayOfWeek = varchar("day_of_week", 20)
    val name = varchar("name", 100)
    val orderIndex = integer("order_index")
}

object ExercisesTable : UUIDTable("exercises") {
    val trainingDayId = reference("training_day_id", TrainingDaysTable)
    val name = varchar("name", 100)
    val targetSets = integer("target_sets")
    val targetReps = integer("target_reps")
    val targetWeightKg = float("target_weight_kg").nullable()
    val durationSeconds = integer("duration_seconds").nullable()
    val orderIndex = integer("order_index")
}

object SessionLogsTable : UUIDTable("session_logs") {
    val userId = reference("user_id", UsersTable)
    val trainingDayId = reference("training_day_id", TrainingDaysTable)
    val date = date("date")
    val durationMinutes = integer("duration_minutes").nullable()
    val notes = text("notes").nullable()
    val createdAt = datetime("created_at")
}

object SetLogsTable : UUIDTable("set_logs") {
    val sessionLogId = reference("session_log_id", SessionLogsTable)
    val exerciseId = reference("exercise_id", ExercisesTable)
    val setIndex = integer("set_index")
    val reps = integer("reps").nullable()
    val weightKg = float("weight_kg").nullable()
    val durationSeconds = integer("duration_seconds").nullable()
    val completed = bool("completed")
}