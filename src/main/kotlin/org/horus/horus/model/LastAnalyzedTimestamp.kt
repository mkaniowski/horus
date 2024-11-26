package org.horus.horus.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "last_analyzed_timestamp")
data class LastAnalyzedTimestamp (
    @Id
    @Column(name = "id", nullable = false)
    val id: String = "last_analyzed",

    @Column(name = "\"timestamp\"", nullable = false)
    var timestamp: Instant? = null
)