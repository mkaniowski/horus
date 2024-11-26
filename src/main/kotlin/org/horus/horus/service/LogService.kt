package org.horus.horus.service

import org.horus.horus.model.LogEntity
import org.horus.horus.model.LogAnomalyEntity
import org.horus.horus.repository.LogEntityRepository
import org.horus.horus.repository.LogsAnomalyRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class LogService(
    private val logsRepository: LogEntityRepository,
    private val anomaliesRepository: LogsAnomalyRepository,
    private val logAnalyzerService: LogAnalyzerService
) {

    fun getLogs(from: Instant, to: Instant): List<LogEntity> {
        return logsRepository.findAll().filter { it.timestamp!!.isAfter(from) && it.timestamp!!.isBefore(to) }
    }

    fun getAnomalies(from: Instant, to: Instant): List<LogAnomalyEntity> {
        return anomaliesRepository.findAll().filter { it.timestampFrom!!.isAfter(from) && it.timestampTo!!.isBefore(to) }
    }

    fun clearAnomalies(from: Instant, to: Instant) {
        val anomalies = anomaliesRepository.findAll().filter { it.timestampFrom!!.isAfter(from) && it.timestampTo!!.isBefore(to) }
        anomaliesRepository.deleteAll(anomalies)
    }

    fun detectAnomalies(from: Instant, to: Instant) {
        val logs = getLogs(from, to)
        if (logs.isNotEmpty()) {
            logAnalyzerService.analyzeLogs(from, to)
        }
    }
}