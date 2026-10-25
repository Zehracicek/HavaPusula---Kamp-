package com.example.hava.planb

enum class ActivityType {
    PICNIC,
    WALK,
    RUN,
    BIKE,
    BEACH,
}

enum class SuitabilityLevel {
    SUITABLE,
    CAUTION,
    UNSUITABLE,
}

enum class FactorKind {
    PRECIPITATION_PROBABILITY,
    PRECIPITATION_AMOUNT,
    TEMPERATURE,
    WIND_SPEED,
    WIND_GUSTS,
    WIND_DIRECTION,
    UV_INDEX,
    WEATHER_CODE,
    AIR_QUALITY,
    WAVE_HEIGHT,
}

enum class MeasurementUnit {
    CELSIUS,
    PERCENT,
    MILLIMETER,
    KILOMETERS_PER_HOUR,
    UV_INDEX,
    NONE,
}

enum class FactorPolarity {
    POSITIVE,
    NEGATIVE,
    NEUTRAL,
}

/** Önerinin hangi hava koşulundan çıktığı. */
enum class ReasonCode {
    PRECIPITATION,
    HEAT,
    COLD,
    WIND,
    UV,
    SEVERE_WEATHER,
    WEATHER,
    COMFORTABLE,
}

enum class InsufficientReason {
    NO_HOURLY_DATA,
    MISSING_CRITICAL,
}

enum class IndoorSuggestion {
    CAFE,
    INDOOR_WALK,
    GYM_RUN,
    INDOOR_BIKE,
    INDOOR_POOL,
}
