package org.horus.horus.utils

import org.horus.horus.enums.AnomalyType
import org.horus.horus.model.ActiveAnomaly
import org.horus.horus.model.LogEntity
import org.horus.horus.repository.LogEntityRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class FakeLogger(
    private val logRepository: LogEntityRepository
) {
    private val geoIP: GeoIP = GeoIP("src/main/resources/geoip/GeoLite2-City-Blocks-IPv4.csv")
    private val random = Random(System.currentTimeMillis())

    // Probability of starting a new anomaly each run
    private val ANOMALY_START_CHANCE = 0.15

    // A list of all the anomaly types we simulate
    private val anomalyTypes = listOf(
        AnomalyType.DDOS,
        AnomalyType.HIGH_UNIQUE_ENDPOINTS,
        AnomalyType.BRUTE_FORCE,
        AnomalyType.LARGE_POST,
        AnomalyType.LARGE_DOWNLOAD,
        AnomalyType.SUSPICIOUS_COUNTRY
    )

    // Track which anomalies are active, so they persist between scheduled runs
    private val activeAnomalies: MutableList<ActiveAnomaly> = mutableListOf()

    /**
     *  Use a random delay for scheduling (30-60 seconds),
     *  so runs are not evenly spaced in time.
     */
    @Scheduled(fixedRate = 30000)
    fun generateLogs() {
        println("Generating logs at ${LocalDateTime.now()}")
        // 1) Record the time this batch started
        val jobStart = Instant.now()

        // 2) Possibly stop or start anomalies
        stopActiveAnomaliesWithChance()
        if (activeAnomalies.size < anomalyTypes.size) {
            maybeStartNewAnomaly()
        }

        // 3) We will store new logs in a temporary list
        val newLogs = mutableListOf<LogEntity>()

        // 4) Generate a random volume of normal traffic
        val normalCount = random.nextInt(40, 70)  // e.g. 20..69 logs
        newLogs += generateNormalTraffic(normalCount, jobStart)

        // 5) Generate logs from active anomalies
        for (anomaly in activeAnomalies) {
            newLogs += generateAnomalyTraffic(anomaly, jobStart)
        }

        // 6) Save the logs
        logRepository.saveAll(newLogs)
        println("Generated ${newLogs.size} logs at ${LocalDateTime.now()}")
    }

    // ------------------------------------------------------------------------
    // Generate Normal Traffic
    // ------------------------------------------------------------------------
    /**
     * Spread timestamps randomly across the scheduling window (e.g. 30s).
     */
    private fun generateNormalTraffic(count: Int, jobStart: Instant): List<LogEntity> {
        val logs = mutableListOf<LogEntity>()
        val maxOffsetSeconds = 30L  // if your min schedule is 30s

        repeat(count) {
            val ip = geoIP.getRandomIp()
            val method = getRandomMethod()
            val endpoint = getRandomEndpoint()
            val protocol = "HTTP/1.1"
            val statusCode = getRandomStatus()
            val bodyBytesSent = random.nextInt(500, 20000)
            val requestLength = random.nextInt(200, 2000)
            val referer = getRandomReferer()
            val userAgent = getRandomUserAgent()
            val user = getRandomUserName()

            // Offset each log's timestamp to spread them across ~30s
            val offsetSec = random.nextLong(0, maxOffsetSeconds)
            val logTime = jobStart.plusSeconds(offsetSec)

            logs += createLog(
                ip = ip,
                user = user,
                timestamp = logTime,
                method = method,
                endpoint = endpoint,
                protocol = protocol,
                statusCode = statusCode,
                bodyBytesSent = bodyBytesSent,
                httpReferer = referer,
                userAgent = userAgent,
                requestLength = requestLength
            )
        }
        return logs
    }

    // ------------------------------------------------------------------------
    // Generate Anomalies
    // ------------------------------------------------------------------------
    /**
     * We'll pass jobStart to each anomaly generator so it can spread out timestamps similarly.
     */
    private fun generateAnomalyTraffic(anomaly: ActiveAnomaly, jobStart: Instant): List<LogEntity> {
        return when (anomaly.type) {
            AnomalyType.DDOS -> generateDDoSTraffic(anomaly, jobStart)
            AnomalyType.HIGH_UNIQUE_ENDPOINTS -> generateHighUniqueEndpointsTraffic(anomaly, jobStart)
            AnomalyType.BRUTE_FORCE -> generateBruteForceTraffic(anomaly, jobStart)
            AnomalyType.LARGE_POST -> generateLargePostTraffic(anomaly, jobStart)
            AnomalyType.LARGE_DOWNLOAD -> generateLargeDownloadTraffic(anomaly, jobStart)
            AnomalyType.SUSPICIOUS_COUNTRY -> generateSuspiciousCountryTraffic(anomaly, jobStart)
        }
    }

    private fun generateDDoSTraffic(anomaly: ActiveAnomaly, jobStart: Instant): List<LogEntity> {
        val logs = mutableListOf<LogEntity>()
        val maxOffsetSeconds = 30L

        // e.g., produce 50 requests
        repeat(50) {
            val method = getRandomMethod()
            val endpoint = getRandomEndpoint()
            val statusCode = if (random.nextDouble() < 0.1) 404 else 200

            // random offset from 0..30
            val offsetSec = random.nextLong(0, maxOffsetSeconds)
            val logTime = jobStart.plusSeconds(offsetSec)

            logs += createLog(
                ip = anomaly.ip,
                user = getRandomUserName(),
                timestamp = logTime,
                method = method,
                endpoint = endpoint,
                protocol = "HTTP/1.1",
                statusCode = statusCode,
                bodyBytesSent = random.nextInt(500, 5000),
                httpReferer = getRandomReferer(),
                userAgent = getRandomUserAgent(),
                requestLength = random.nextInt(300, 500)
            )
        }
        return logs
    }

    private fun generateHighUniqueEndpointsTraffic(anomaly: ActiveAnomaly, jobStart: Instant): List<LogEntity> {
        val logs = mutableListOf<LogEntity>()
        val maxOffsetSeconds = 30L

        repeat(30) {
            val endpoint = "/secure/" + random.nextInt(1, 500)
            val statusCode = if (random.nextDouble() < 0.5) 401 else 403

            val offsetSec = random.nextLong(0, maxOffsetSeconds)
            val logTime = jobStart.plusSeconds(offsetSec)

            logs += createLog(
                ip = anomaly.ip,
                user = getRandomUserName(),
                timestamp = logTime,
                method = "GET",
                endpoint = endpoint,
                protocol = "HTTP/1.1",
                statusCode = statusCode,
                bodyBytesSent = random.nextInt(500, 1500),
                httpReferer = getRandomReferer(),
                userAgent = getRandomUserAgent(),
                requestLength = random.nextInt(200, 600)
            )
        }
        return logs
    }

    private fun generateBruteForceTraffic(anomaly: ActiveAnomaly, jobStart: Instant): List<LogEntity> {
        val logs = mutableListOf<LogEntity>()
        val maxOffsetSeconds = 30L

        repeat(20) {
            val endpoint = if (random.nextBoolean()) "/login" else "/admin"
            val status = if (random.nextDouble() < 0.7) 401 else 403

            val offsetSec = random.nextLong(0, maxOffsetSeconds)
            val logTime = jobStart.plusSeconds(offsetSec)

            logs += createLog(
                ip = anomaly.ip,
                user = getRandomUserName(),
                timestamp = logTime,
                method = "POST",
                endpoint = endpoint,
                protocol = "HTTP/1.1",
                statusCode = status,
                bodyBytesSent = random.nextInt(300, 800),
                httpReferer = getRandomReferer(),
                userAgent = getRandomUserAgent(),
                requestLength = random.nextInt(500, 2000)
            )
        }
        return logs
    }

    private fun generateLargePostTraffic(anomaly: ActiveAnomaly, jobStart: Instant): List<LogEntity> {
        val logs = mutableListOf<LogEntity>()
        val maxOffsetSeconds = 30L

        repeat(10) {
            val offsetSec = random.nextLong(0, maxOffsetSeconds)
            val logTime = jobStart.plusSeconds(offsetSec)

            logs += createLog(
                ip = anomaly.ip,
                user = getRandomUserName(),
                timestamp = logTime,
                method = "POST",
                endpoint = "/upload/data",
                protocol = "HTTP/1.1",
                statusCode = 200,
                bodyBytesSent = random.nextInt(1000, 5000),
                httpReferer = getRandomReferer(),
                userAgent = getRandomUserAgent(),
                requestLength = random.nextInt(2_000_001, 4_000_000)
            )
        }
        return logs
    }

    private fun generateLargeDownloadTraffic(anomaly: ActiveAnomaly, jobStart: Instant): List<LogEntity> {
        val logs = mutableListOf<LogEntity>()
        val maxOffsetSeconds = 30L

        repeat(10) {
            val endpoint = "/files/hugefile-${random.nextInt(1000)}.bin"

            val offsetSec = random.nextLong(0, maxOffsetSeconds)
            val logTime = jobStart.plusSeconds(offsetSec)

            logs += createLog(
                ip = anomaly.ip,
                user = getRandomUserName(),
                timestamp = logTime,
                method = "GET",
                endpoint = endpoint,
                protocol = "HTTP/1.1",
                statusCode = 200,
                bodyBytesSent = random.nextInt(10_000_001, 20_000_000),
                httpReferer = getRandomReferer(),
                userAgent = getRandomUserAgent(),
                requestLength = random.nextInt(300, 800)
            )
        }
        return logs
    }

    private fun generateSuspiciousCountryTraffic(anomaly: ActiveAnomaly, jobStart: Instant): List<LogEntity> {
        val logs = mutableListOf<LogEntity>()
        val maxOffsetSeconds = 30L

        repeat(30) {
            val method = getRandomMethod()
            val endpoint = getRandomEndpoint()
            val statusCode = getRandomStatus()

            val offsetSec = random.nextLong(0, maxOffsetSeconds)
            val logTime = jobStart.plusSeconds(offsetSec)

            logs += createLog(
                ip = anomaly.ip,
                user = getRandomUserName(),
                timestamp = logTime,
                method = method,
                endpoint = endpoint,
                protocol = "HTTP/1.1",
                statusCode = statusCode,
                bodyBytesSent = random.nextInt(500, 20000),
                httpReferer = getRandomReferer(),
                userAgent = getRandomUserAgent(),
                requestLength = random.nextInt(300, 2000)
            )
        }
        return logs
    }

    // ------------------------------------------------------------------------
    // Start/Stop of Anomalies
    // ------------------------------------------------------------------------
    private fun maybeStartNewAnomaly() {
        if (random.nextDouble() < ANOMALY_START_CHANCE) {
            val possible = anomalyTypes.filter { type ->
                activeAnomalies.none { it.type == type }
            }
            if (possible.isNotEmpty()) {
                val chosenType = possible.random(random)

                val newIp = if (chosenType == AnomalyType.SUSPICIOUS_COUNTRY) {
                    val countries = listOf("RU", "CN", "BR", "IR", "AF")
                    geoIP.getRandomIpByCountry(countries.random(random))
                } else {
                    geoIP.getRandomIp()
                }

                val newAnomaly = ActiveAnomaly(
                    type = chosenType,
                    ip = newIp,
                    stopProbability = getStopProbability(chosenType)
                )
                activeAnomalies += newAnomaly
                println("Starting anomaly: $chosenType on IP: $newIp")
            }
        }
    }

    private fun stopActiveAnomaliesWithChance() {
        val toRemove = mutableListOf<ActiveAnomaly>()
        for (anomaly in activeAnomalies) {
            if (random.nextDouble() < anomaly.stopProbability) {
                println("Stopping anomaly: ${anomaly.type} on IP: ${anomaly.ip}")
                toRemove += anomaly
            }
        }
        activeAnomalies.removeAll(toRemove)
    }

    private fun getStopProbability(type: AnomalyType): Double {
        return when (type) {
            AnomalyType.DDOS -> 0.20            // 20% chance to stop each cycle
            AnomalyType.LARGE_DOWNLOAD -> 0.50  // 50% chance to stop each cycle
            else -> 0.30                        // default 30%
        }
    }

    // ------------------------------------------------------------------------
    // Create LogEntity & Random Helpers
    // ------------------------------------------------------------------------
    private fun createLog(
        ip: String,
        user: String?,
        timestamp: Instant,
        method: String,
        endpoint: String,
        protocol: String,
        statusCode: Int,
        bodyBytesSent: Int,
        httpReferer: String?,
        userAgent: String,
        requestLength: Int
    ): LogEntity {
        val log = LogEntity()
        log.ip = ip
        log.user = user
        log.timestamp = timestamp
        log.method = method
        log.endpoint = endpoint
        log.protocol = protocol
        log.statusCode = statusCode
        log.bodyBytesSent = bodyBytesSent
        log.httpReferer = httpReferer
        log.userAgent = userAgent
        log.requestLength = requestLength

        val safeUser = user ?: "-"
        val safeReferer = httpReferer ?: "-"

        val formattedMessage = "$ip - $safeUser [$timestamp] " +
                "\"$method $endpoint $protocol\" " +
                "$statusCode $bodyBytesSent \"$safeReferer\" \"$userAgent\" $requestLength\n"

        log.message = formattedMessage
        return log
    }

    private fun getRandomMethod(): String {
        val methods = listOf("GET", "POST", "PUT", "DELETE")
        return methods.random(random)
    }

    private fun getRandomEndpoint(): String {
        val endpoints = listOf("/home", "/products", "/api/items", "/contact", "/about")
        return endpoints.random(random)
    }

    private fun getRandomStatus(): Int {
        // Weighted random status: mostly 200, occasional 404 or 500
        return when {
            random.nextDouble() < 0.8 -> 200
            random.nextDouble() < 0.9 -> 404
            else -> 500
        }
    }

    private fun getRandomReferer(): String? {
        // 20% chance of no referer
        if (random.nextDouble() < 0.2) return null
        val referers = listOf(
            "https://www.google.com",
            "https://www.bing.com",
            "https://example.com/home",
            "https://mysite.com/landing"
        )
        return referers.random(random)
    }

    private fun getRandomUserAgent(): String {
        val userAgents = listOf(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)",
            "curl/7.64.1",
            "PostmanRuntime/7.28.4"
        )
        return userAgents.random(random)
    }

    private fun getRandomUserName(): String {
        val names = listOf("alice", "bob", "charlie", "david", "-")
        return names.random(random)
    }
}
