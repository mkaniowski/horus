package org.horus.horus.utils

import org.horus.horus.model.LogEntity
import org.springframework.stereotype.Service
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Service
class FakeLogger {

    private val random = Random(System.currentTimeMillis())
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private val endpoints = listOf("/login", "/api/data", "/admin", "/home", "/search")
    private val userAgents = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/605.1.15",
        "curl/7.68.0",
        "Python-urllib/3.9",
        "Googlebot/2.1 (+http://www.google.com/bot.html)"
    )

    private var anomaliesCount = 0

    private fun randomIp(): String {
        return (1..4).joinToString(".") { random.nextInt(1, 255).toString() }
    }

    private fun randomStatusCode(isUnauthorized: Boolean = false): Int {
        return when {
            isUnauthorized -> listOf(401, 403).random(random)
            else -> listOf(200, 201, 404, 500).random(random)
        }
    }

    private fun randomUser(): String {
        return listOf("user1", "user2", "user3", "user4", "user5", "-").random(random)
    }

    private fun randomMethod(): String {
        return listOf("GET", "POST", "PUT", "DELETE").random(random)
    }

    private fun randomProtocol(): String {
        return listOf("HTTP/1.0", "HTTP/1.1", "HTTP/2.0").random(random)
    }

    private fun randomReferer(): String {
        return listOf(
            "https://example.com/login", "https://example.com/data", "https://example.com/user", "-"
        ).random(random)
    }

    private fun randomRequestLength(isDdos: Boolean): Int {
        return when {
            isDdos -> random.nextInt(1000, 10000)
            else -> random.nextInt(100, 1000)
        }
    }

    private fun randomBodyBytesSent(): Int {
        return random.nextInt(100, 1000)
    }

    private fun randomInterval(from: Double, to: Double): Long {
        return (random.nextDouble(from, to) * 1_000_000_000).toLong()
    }

    fun generateFakeLogs(days: Int, path: String): List<LogEntity> {
        val now = LocalDateTime.now()
        val startTime = now.minusDays(days.toLong())

        val logs = mutableListOf<LogEntity>()
        var currentTime = startTime

        while (currentTime.isBefore(now)) {
            val ip = randomIp()
            val endpoint = endpoints.random(random)
            val userAgent = userAgents.random(random)
            val statusCode = randomStatusCode()
            val user = randomUser()
            val method = randomMethod()
            val protocol = randomProtocol()
            val httpReferer = randomReferer()
            val requestLength = randomRequestLength(false)
            val bodyBytesSent = randomBodyBytesSent()

            when (random.nextInt(100)) {
                in 0..1 -> logs.addAll(generateBruteForceLogs(currentTime, ip))
                in 2..3 -> logs.addAll(generateDDoSLogs(currentTime, ip))
                in 4..5 -> logs.addAll(generateBotActivityLogs(currentTime, ip))
                in 6..7 -> logs.add(generateUnauthorizedAccessLog(currentTime, ip))
                else -> logs.add(
                    createLogEntity(
                        timestamp = currentTime,
                        ip = ip,
                        endpoint = endpoint,
                        statusCode = statusCode,
                        userAgent = userAgent,
                        user = user,
                        method = method,
                        protocol = protocol,
                        httpReferer = httpReferer,
                        requestLength = requestLength,
                        bodyBytesSent = bodyBytesSent
                    )
                )
            }

            currentTime = currentTime.plusSeconds(random.nextLong(30, 600))
        }

        saveLogsToFile(logs, path)
        println("anonmaliesCount: $anomaliesCount")
        return logs
    }

    private fun createLogEntity(
        timestamp: LocalDateTime,
        ip: String,
        endpoint: String,
        statusCode: Int,
        userAgent: String,
        user: String?,
        method: String,
        protocol: String,
        httpReferer: String?,
        requestLength: Int,
        bodyBytesSent: Int
    ): LogEntity {
        return LogEntity().apply {
            this.ip = ip
            this.user = user
            this.timestamp = timestamp.toInstant(java.time.ZoneOffset.UTC)
            this.method = method
            this.endpoint = endpoint
            this.protocol = protocol
            this.statusCode = statusCode
            this.bodyBytesSent = bodyBytesSent
            this.httpReferer = httpReferer
            this.userAgent = userAgent
            this.requestLength = requestLength
        }
    }

    private fun generateBruteForceLogs(startTime: LocalDateTime, ip: String): List<LogEntity> {
        val logs = mutableListOf<LogEntity>()
        val status = randomStatusCode(true)
        val agent = userAgents.random(random)
        val protocol = randomProtocol()
        val requestLength = randomRequestLength(false)
        val bodyBytesSent = randomBodyBytesSent()

        val rand = random.nextInt(100)

        anomaliesCount++

        repeat(rand) { i ->
            val timestamp = startTime.plusNanos(i * randomInterval(0.5, 0.8))
            logs.add(
                createLogEntity(
                    timestamp = timestamp,
                    ip = ip,
                    endpoint = "/login",
                    statusCode = status,
                    userAgent = agent,
                    user = null,
                    method = "POST",
                    protocol = protocol,
                    httpReferer = null,
                    requestLength = requestLength,
                    bodyBytesSent = bodyBytesSent
                )
            )
        }

        return logs
    }

    private fun generateDDoSLogs(startTime: LocalDateTime, ip: String): List<LogEntity> {
        val logs = mutableListOf<LogEntity>()
        val agent = userAgents.random(random)

        val rand = random.nextInt(1000)

        anomaliesCount++

        repeat(rand) { i ->
            val timestamp = startTime.plusNanos(i * randomInterval(0.1, 0.5))
            logs.add(
                createLogEntity(
                    timestamp = timestamp,
                    ip = ip,
                    endpoint = endpoints.random(random),
                    statusCode = 200,
                    userAgent = agent,
                    user = null,
                    method = randomMethod(),
                    protocol = randomProtocol(),
                    httpReferer = randomReferer(),
                    requestLength = randomRequestLength(true),
                    bodyBytesSent = randomBodyBytesSent()
                )
            )
        }
        return logs
    }

    private fun generateBotActivityLogs(startTime: LocalDateTime, ip: String): List<LogEntity> {
        val logs = mutableListOf<LogEntity>()

        val rand = random.nextInt(100)

        val user = randomUser()
        val method = randomMethod()
        val protocol = randomProtocol()
        val httpReferer = randomReferer()
        val requestLength = randomRequestLength(false)
        val bodyBytesSent = randomBodyBytesSent()

        anomaliesCount++

        repeat(rand) { i ->
            val timestamp = startTime.plusSeconds(i.toLong())
            logs.add(
                createLogEntity(
                    timestamp = timestamp,
                    ip = ip,
                    endpoint = "/search",
                    statusCode = 200,
                    userAgent = "BotAgent/1.0",
                    user = user,
                    method = method,
                    protocol = protocol,
                    httpReferer = httpReferer,
                    requestLength = requestLength,
                    bodyBytesSent = bodyBytesSent
                )
            )
        }
        return logs
    }

    private fun generateUnauthorizedAccessLog(startTime: LocalDateTime, ip: String): LogEntity {
        anomaliesCount++
        return createLogEntity(
            timestamp = startTime,
            ip = ip,
            endpoint = "/admin",
            statusCode = randomStatusCode(true),
            userAgent = userAgents.random(random),
            user = randomUser(),
            method = randomMethod(),
            protocol = randomProtocol(),
            httpReferer = randomReferer(),
            requestLength = randomRequestLength(false),
            bodyBytesSent = randomBodyBytesSent()
        )
    }


    private fun saveLogsToFile(logs: List<LogEntity>, path: String) {
        val file = File(path)
        BufferedWriter(FileWriter(file)).use { writer ->
            logs.forEach { log ->
                writer.write(
                    "${log.ip} - ${log.user} [${formatter.format(log.timestamp!!.atZone(java.time.ZoneOffset.UTC))}] \"${log.method} ${log.endpoint} ${log.protocol}\" ${log.statusCode} ${log.bodyBytesSent} \"${log.httpReferer}\" \"${log.userAgent}\" ${log.requestLength}\n"
                )
            }
        }
    }
}