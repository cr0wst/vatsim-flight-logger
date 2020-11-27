package dev.stevecrow.vatsim.data.flight

import dev.stevecrow.vatsim.data.airport.AirportEntity
import dev.stevecrow.vatsim.data.http.Batch
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DisconnectedFlightProcessingService(
    private val flightRepository: FlightRepository
) {
    private val log = KotlinLogging.logger {}
    private var airportCache: List<AirportEntity> = emptyList()

    @Transactional
    fun process(batch: Batch) {
        log.info { "Purging potential disconnects (${batch.metadata.updateTimestamp}) with ${batch.clients.size} clients." }

        val callsigns = batch.clients.map { it.callsign }
        val count = flightRepository.cancelFlightsAndDepartures(callsigns)

        log.info { "Purged $count cancelled flights." }
    }
}
