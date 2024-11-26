package org.horus.horus.repository

import org.horus.horus.model.LogAnomalyEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.*

interface LogsAnomalyRepository : JpaRepository<LogAnomalyEntity, UUID> {
    fun findByAnomalyTypeAndTimestampToAfter(anomalyType: String, timestamp: Instant): List<LogAnomalyEntity>
}