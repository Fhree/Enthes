package com.enthes.domain.model

data class DiseaseActivity(
    val id: String,
    val entryDate: String,
    val morningStiffnessMinutes: Int,
    val globalPainLevel: Int,
    val fatigueLevel: Int,
    val hasActiveFlare: Boolean,
    val createdAt: Long,
) {
    init {
        require(globalPainLevel in 0..10) {
            "globalPainLevel must be between 0 and 10, but was $globalPainLevel"
        }
        require(fatigueLevel in 0..10) {
            "fatigueLevel must be between 0 and 10, but was $fatigueLevel"
        }
        require(morningStiffnessMinutes >= 0) {
            "morningStiffnessMinutes must be greater than or equal to 0, but was $morningStiffnessMinutes"
        }
    }
}
