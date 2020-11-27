package dev.stevecrow.vatsim.data.flight

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CleanUpService(
    private val flightRepository: FlightRepository
) {
    private val log = KotlinLogging.logger {}

    @Scheduled(fixedRate = 600000)
    @Transactional
    fun clean() {
        log.info { "[Clean] Starting Clean Up " }
        purgeExpiredEntries()
        log.info { "[Clean] Finished Clean Up" }
    }

    private fun purgeExpiredEntries() {
        log.info { "[Clean] Purging expired flights" }

        val count = flightRepository.deleteOldFlights()

        log.info { "[Clean] Purged $count flights" }
    }
}
