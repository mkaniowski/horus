package org.horus.horus.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.util.*

@Entity
@Table(name = "logs_anomalies")
open class LogAnomalyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    open var id: UUID? = null

    @NotNull
    @Column(name = "timestamp_from", nullable = false)
    open var timestampFrom: Instant? = null

    @NotNull
    @Column(name = "timestamp_to", nullable = false)
    open var timestampTo: Instant? = null

    @Size(max = 20)
    @NotNull
    @Column(name = "level", nullable = false, length = 20)
    open var level: String? = null

    @Size(max = 255)
    @NotNull
    @Column(name = "endpoint", nullable = false)
    open var endpoint: String? = null

    @NotNull
    @Column(name = "number_of_hits", nullable = false)
    open var numberOfHits: Int? = null

    @Size(max = 255)
    @Column(name = "anomaly_type")
    open var anomalyType: String? = null

    @NotNull
    @Column(name = "body_bytes_sent", nullable = false)
    open var bodyBytesSent: Int? = null

    @NotNull
    @Column(name = "request_length", nullable = false)
    open var requestLength: Int? = null
}