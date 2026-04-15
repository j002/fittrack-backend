package com.fittrack.model

import kotlinx.serialization.Serializable

@Serializable
data class SessionLogRequest(
    val trainingDayId: String,
    val date: String,
    val durationMinutes: Int? = null,
    val notes: String? = null,
    val sets: List<SetLogRequest>
)

@Serializable
data class SetLogRequest(
    val exerciseId: String,
    val setIndex: Int,
    val reps: Int? = null,
    val weightKg: Float? = null,
    val durationSeconds: Int? = null,
    val completed: Boolean
)

@Serializable
data class SessionLogResponse(
    val id: String,
    val trainingDayId: String,
    val trainingDayName: String,
    val date: String,
    val durationMinutes: Int?,
    val notes: String?,
    val sets: List<SetLogResponse>
)

@Serializable
data class SetLogResponse(
    val id: String,
    val exerciseId: String,
    val exerciseName: String,
    val setIndex: Int,
    val reps: Int?,
    val weightKg: Float?,
    val durationSeconds: Int?,
    val completed: Boolean
)

@Serializable
data class StatsResponse(
    val totalSessions: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val progressByExercise: List<ExerciseProgress>
)

@Serializable
data class ExerciseProgress(
    val exerciseId: String,
    val exerciseName: String,
    val history: List<ExerciseProgressPoint>
)

@Serializable
data class ExerciseProgressPoint(
    val date: String,
    val maxWeightKg: Float?,
    val totalReps: Int
)