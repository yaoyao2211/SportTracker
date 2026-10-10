package com.example.sporttracker.data

enum class SportType(
    val displayName: String,
    val needsGps: Boolean = false,
    val isStrength: Boolean = false
) {
    RUNNING("跑步", needsGps = true),
    PLANK("平板支撑", isStrength = true),
    V_UP("两头起", isStrength = true),
    PUSH_UP("俯卧撑", isStrength = true)
}