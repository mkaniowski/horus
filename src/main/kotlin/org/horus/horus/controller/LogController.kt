package org.horus.horus.controller

import org.horus.horus.model.LogAnomalyEntity
import org.horus.horus.model.LogEntity
import org.horus.horus.service.LogService
import org.springframework.web.bind.annotation.*
import java.time.Instant


@RestController
@RequestMapping("/api/v1/log")
class LogController(private val logService: LogService) {

    @GetMapping("")
    fun getLogs(@RequestParam from: String, @RequestParam to: String): List<LogEntity> {
        val fromInstant = Instant.parse(from)
        val toInstant = Instant.parse(to)
        return logService.getLogs(fromInstant, toInstant)
    }

    @GetMapping("/anomalies")
    fun getAnomalies(@RequestParam from: String, @RequestParam to: String): List<LogAnomalyEntity> {
        val fromInstant = Instant.parse(from)
        val toInstant = Instant.parse(to)
        return logService.getAnomalies(fromInstant, toInstant)
    }

    @DeleteMapping("/anomalies")
    fun clearAnomalies(@RequestParam from: String, @RequestParam to: String): String {
        return "Logs"
    }

    @PostMapping("/anomalies")
    fun detectAnomalies(@RequestParam from: String, @RequestParam to: String): String {
        return "Logs"
    }
}