package org.horus.horus.model

import com.fasterxml.jackson.annotation.JsonFormat
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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    var timestamp: Instant? = null
)