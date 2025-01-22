package org.horus.horus.model

import org.horus.horus.enums.AnomalyType

data class ActiveAnomaly(
    val type: AnomalyType,
    val ip: String,
    val stopProbability: Double
)
