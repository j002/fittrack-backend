package com.fittrack.model

import kotlinx.serialization.Serializable

@Serializable
data class TrainingDayRequest(
    val dayOfWeek: String,
    val name: String,
    val orderIndex: Int,
    val exercises: List<ExerciseRequest>
)

@Serializable
data class ExerciseRequest(
    val name: String,
    val targetSets: Int,
    val targetReps: Int,
    val targetWeightKg: Float? = null,
    val durationSeconds: Int? = null,
    val orderIndex: Int
)

@Serializable
data class TrainingDayResponse(
    val id: String,
    val dayOfWeek: String,
    val name: String,
    val orderIndex: Int,
    val exercises: List<ExerciseResponse>
)

@Serializable
data class ExerciseResponse(
    val id: String,
    val name: String,
    val targetSets: Int,
    val targetReps: Int,
    val targetWeightKg: Float?,
    val durationSeconds: Int?,
    val orderIndex: Int
)