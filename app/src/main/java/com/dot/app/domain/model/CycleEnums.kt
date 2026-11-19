package com.dot.app.domain.model

enum class CycleRegularity {
    REGULAR,
    IRREGULAR,
    UNKNOWN
}

enum class ContraceptiveMethod {
    NONE,
    COMBINED_ORAL_PILL,
    PROGESTIN_ONLY_PILL,
    HORMONAL_IUD,
    COPPER_IUD,
    SUBDERMAL_IMPLANT,
    INJECTION,
    VAGINAL_RING_OR_PATCH,
    BARRIER_CONDOM
}

enum class FlowLevel {
    NONE,
    SPOTTING,
    LIGHT,
    MODERATE,
    HEAVY
}

enum class Symptom {
    CRAMPS,
    HEADACHE,
    BLOATING,
    BREAST_TENDERNESS,
    FATIGUE,
    ACNE,
    MOOD_CHANGES,
    LOWER_BACK_PAIN,
    NAUSEA,
    ANXIETY_STRESS,
    CRAVINGS,
    INSOMNIA,
    CERVICAL_MUCUS_CHANGES
}

enum class ProtectionMethod {
    NONE,
    BARRIER_CONDOM,
    HORMONAL_ROUTINE,
    IUD_OR_IMPLANT,
    WITHDRAWAL,
    OTHER
}

enum class EmergencyCompoundType {
    LEVONORGESTREL,
    ULIPRISTAL_ACETATE,
    UNKNOWN
}

enum class CyclePhase {
    MENSTRUAL,
    FOLLICULAR,
    OVULATION_FERTILE,
    LUTEAL
}

enum class CycleClassification {
    NORMAL_REGULAR,
    IRREGULAR,
    ANOMALOUS,
    ALTERED_BY_EMERGENCY,
    IN_PROGRESS
}
