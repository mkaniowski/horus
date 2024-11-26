package org.horus.horus.config

import jakarta.annotation.PostConstruct
import org.horus.horus.model.LogEntity
import org.horus.horus.repository.LogEntityRepository
import org.horus.horus.utils.FakeLogger
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
class DataInitializationConfig(
    private val fakeLogger: FakeLogger, private val logRepository: LogEntityRepository
) {

    @PostConstruct
    fun initializeData() {
        val logFilePath = "src/main/resources/data/logs.txt"
        val logFile = File(logFilePath)

        if (!logFile.exists()) {
            logFile.parentFile.mkdirs()
            val logs: List<LogEntity> = fakeLogger.generateFakeLogs(1, logFilePath)
            println("Generated ${logs.size} logs")

            val batchSize = 1000
            var batchCount = 0
            logs.chunked(batchSize).forEach { batch ->
                logRepository.saveAll(batch)
                batchCount++
                println("Progress: ${batchCount * batchSize * 100 / logs.size}% batches saved")
            }

            println("Generated logs at $logFilePath and saved to PostgreSQL")
        } else {
            println("Log file already exists at $logFilePath")
        }
    }
}