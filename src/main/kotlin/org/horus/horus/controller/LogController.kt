package org.horus.horus.controller

import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/log")
class LogController {

    @GetMapping("")
    fun getLogs(@RequestParam from: String, @RequestParam to: String): String {
        return "Logs"
    }

    @GetMapping("/anomalies")
    fun getAnomalies(@RequestParam from: String, @RequestParam to: String): String {
        return "Logs"
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