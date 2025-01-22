package org.horus.horus.model

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "logs")
open class LogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    open var id: UUID? = null

    @Size(max = 15)
    @NotNull
    @Column(name = "ip", nullable = false, length = 15)
    open var ip: String? = null

    @Size(max = 255)
    @Column(name = "\"user\"")
    open var user: String? = null

    @NotNull
    @Column(name = "\"timestamp\"", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    open var timestamp: Instant? = null

    @Size(max = 10)
    @NotNull
    @Column(name = "method", nullable = false, length = 10)
    open var method: String? = null

    @Size(max = 255)
    @NotNull
    @Column(name = "endpoint", nullable = false)
    open var endpoint: String? = null

    @Size(max = 10)
    @NotNull
    @Column(name = "protocol", nullable = false, length = 10)
    open var protocol: String? = null

    @NotNull
    @Column(name = "status_code", nullable = false)
    open var statusCode: Int? = null

    @NotNull
    @Column(name = "body_bytes_sent", nullable = false)
    open var bodyBytesSent: Int? = null

    @Size(max = 255)
    @Column(name = "http_referer")
    open var httpReferer: String? = null

    @Size(max = 255)
    @NotNull
    @Column(name = "user_agent", nullable = false)
    open var userAgent: String? = null

    @NotNull
    @Column(name = "request_length", nullable = false)
    open var requestLength: Int? = null

    @NotNull
    @Column(name = "message", nullable = false)
    open var message: String? = null
}