package org.horus.horus

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class HorusApplication

fun main(args: Array<String>) {
	runApplication<HorusApplication>(*args)
}
