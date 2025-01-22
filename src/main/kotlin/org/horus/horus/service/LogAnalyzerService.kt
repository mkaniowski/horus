package org.horus.horus.service

import org.horus.horus.model.LastAnalyzedTimestamp
import org.horus.horus.model.LogEntity
import org.horus.horus.model.LogAnomalyEntity
import org.horus.horus.repository.LastAnalyzedTimestampRepository
import org.horus.horus.repository.LogEntityRepository
import org.horus.horus.repository.LogsAnomalyRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

@Service
class LogAnalyzerService(
    private val logRepository: LogEntityRepository,
    private val lastAnalyzedTimestampRepository: LastAnalyzedTimestampRepository,
    private val logsAnomalyRepository: LogsAnomalyRepository
) {


    private fun fetchLogs(lastAnalyzedTimestamp: Instant): List<LogEntity> {
        return logRepository.findAll().filter { it.timestamp!!.isAfter(lastAnalyzedTimestamp) }
    }

    private fun detectBruteForce(logs: List<LogEntity>) {
        val threshold = 10
        val timeWindow = 8
        val groupAnomalyTolerance = 5
        val loginEndpoint = "/login"

        var isDetected = false
        var lastTimestamp: Instant? = null
        val attempts = mutableListOf<LogEntity>()

        logs.filter { it.statusCode == 401 || it.statusCode == 403 && it.endpoint == loginEndpoint }.forEach { log ->
            attempts.add(log)

            if (!isDetected) {
                attempts.removeIf { it.timestamp?.isBefore(log.timestamp!!.minusSeconds(timeWindow.toLong()))
                    ?: true }
            }

            if (isDetected && lastTimestamp?.isBefore(log.timestamp!!.minusSeconds(groupAnomalyTolerance.toLong())) == true) {
                val sumOfRequestLength = attempts.sumOf { it.bodyBytesSent!! }
                val sumOfBodyBytesSent = attempts.sumOf { it.bodyBytesSent!! }

                val anomaly = LogAnomalyEntity().apply {
                    timestampFrom = attempts.first().timestamp
                    timestampTo = log.timestamp
                    endpoint = log.endpoint
                    numberOfHits = attempts.size
                    anomalyType = "Brute-force"
                    level = "High"
                    endpoint = log.endpoint
                    numberOfHits = attempts.size
                    bodyBytesSent = sumOfBodyBytesSent
                    requestLength = sumOfRequestLength
                }

                println("Brute-force attack detected, hits: ${attempts.size}")

                isDetected = false
                lastTimestamp = null
                attempts.clear()

                logsAnomalyRepository.save(anomaly)
            }

            if (attempts.size >= threshold) {
                isDetected = true
                lastTimestamp = log.timestamp
            }
        }
    }

    private fun detectDDoS(logs: List<LogEntity>) {
        val threshold = 10
        val timeWindow = 2
        val bytesThreshold = 50000
        val groupAnomalyTolerance = 3

        var isDetected = false
        var lastTimestamp: Instant? = null
        val attempts = mutableListOf<LogEntity>()

        logs.forEach { log ->
            attempts.add(log)

            if (!isDetected) {
                attempts.removeIf { it.timestamp?.isBefore(log.timestamp!!.minusSeconds(timeWindow.toLong()))
                    ?: true }
            }

            if (isDetected && lastTimestamp?.isBefore(log.timestamp!!.minusSeconds(groupAnomalyTolerance.toLong())) == true) {
                val sumOfRequestLength = attempts.sumOf { it.bodyBytesSent!! }
                val sumOfBodyBytesSent = attempts.sumOf { it.bodyBytesSent!! }

                val anomaly = LogAnomalyEntity().apply {
                    timestampFrom = attempts.first().timestamp
                    timestampTo = log.timestamp
                    endpoint = log.endpoint
                    numberOfHits = attempts.size
                    anomalyType = "DDoS"
                    level = "High"
                    endpoint = log.endpoint
                    numberOfHits = attempts.size
                    bodyBytesSent = sumOfBodyBytesSent
                    requestLength = sumOfRequestLength
                }

                println("DDoS attack detected, hits: ${attempts.size}")

                isDetected = false
                lastTimestamp = null
                attempts.clear()

                logsAnomalyRepository.save(anomaly)
            }

            if (attempts.size >= threshold && attempts.sumOf { it.bodyBytesSent!! } >= bytesThreshold) {
                isDetected = true
                lastTimestamp = log.timestamp
            }
        }
    }

    private fun detectBotActivity(logs: List<LogEntity>) {
        val botPatternThreshold = 5
        val timeWindow = 10
        val botUserAgent = "BotAgent/1.0"

        var isDetected = false
        var lastTimestamp: Instant? = null
        val attempts = mutableListOf<LogEntity>()

        logs.forEachIndexed { index, log ->
            attempts.add(log)

            if (!isDetected) {
                attempts.removeIf { it.timestamp?.isBefore(log.timestamp!!.minusSeconds(timeWindow.toLong())) ?: true }
            }

            if (attempts.size >= botPatternThreshold) {
                val intervals = attempts.zipWithNext { first, second ->
                    Timestamp.from(first.timestamp).time - Timestamp.from(second.timestamp).time
                }

                if (intervals.distinct().size == 1 && log.userAgent == botUserAgent) {
                    isDetected = true
                    lastTimestamp = log.timestamp
                }
            }

            if (isDetected && lastTimestamp?.isBefore(log.timestamp!!.minusSeconds(timeWindow.toLong())) == true) {
                val sumOfRequestLength = attempts.sumOf { it.requestLength!! }
                val sumOfBodyBytesSent = attempts.sumOf { it.bodyBytesSent!! }

                val anomaly = LogAnomalyEntity().apply {
                    timestampFrom = attempts.first().timestamp
                    timestampTo = log.timestamp
                    endpoint = log.endpoint
                    numberOfHits = attempts.size
                    anomalyType = "Bot"
                    level = "Medium"
                    bodyBytesSent = sumOfBodyBytesSent
                    requestLength = sumOfRequestLength
                }

                println("Bot activity detected (consistent intervals), hits: ${attempts.size}")

                isDetected = false
                lastTimestamp = null
                attempts.clear()

                logsAnomalyRepository.save(anomaly)
            }
        }
    }


    fun scheduledAnalyzeLogs() {
        val lastAnalyzedTimestamp = lastAnalyzedTimestampRepository.findById("last_analyzed")
            .orElse(LastAnalyzedTimestamp(timestamp = Instant.EPOCH))

        val effectiveFrom = lastAnalyzedTimestamp.timestamp!!
        val effectiveTo = Instant.now()

        val logs = fetchLogs(effectiveFrom).filter { it.timestamp!!.isBefore(effectiveTo) }
        if (logs.isNotEmpty()) {
            detectBruteForce(logs)
            detectDDoS(logs)
            detectBotActivity(logs)

            lastAnalyzedTimestamp.timestamp = logs.maxOf { it.timestamp!! }
            lastAnalyzedTimestampRepository.save(lastAnalyzedTimestamp)
        }
    }

    fun analyzeLogs(from: Instant? = null, to: Instant? = null) {
        val lastAnalyzedTimestamp = lastAnalyzedTimestampRepository.findById("last_analyzed")
            .orElse(LastAnalyzedTimestamp(timestamp = Instant.EPOCH))

        val effectiveFrom = from ?: lastAnalyzedTimestamp.timestamp!!
        val effectiveTo = to ?: Instant.now()

        val logs = fetchLogs(effectiveFrom).filter { it.timestamp!!.isBefore(effectiveTo) }
        if (logs.isNotEmpty()) {
            detectBruteForce(logs)
            detectDDoS(logs)
            detectBotActivity(logs)

            lastAnalyzedTimestamp.timestamp = logs.maxOf { it.timestamp!! }
            lastAnalyzedTimestampRepository.save(lastAnalyzedTimestamp)
        }
    }

}