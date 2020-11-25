package dev.stevecrow.vatsim.data.flight

import dev.stevecrow.vatsim.data.airport.AirportEntity
import dev.stevecrow.vatsim.data.client.ClientWithLocation
import dev.stevecrow.vatsim.data.http.Metadata
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AirborneFlightProcessingService(private val flightRepository: FlightRepository) {
    private val log = KotlinLogging.logger {}

    fun process(metadata: Metadata, clients: List<ClientWithLocation>) {
        clients.forEach {
            if (it.position != ClientWithLocation.Position.AIR) {
                throw IllegalArgumentException("Received client that was on the ground.")
            }

            val potentialFlight = retrieveDepartingFlight(it) ?: retrieveAirborneFlight(it)
            if (potentialFlight?.status == FlightEntity.Status.DEPARTING) {
                registerTakeoff(potentialFlight)
            } else if (potentialFlight?.status == FlightEntity.Status.IN_FLIGHT) {
                updateAirborneLocation(potentialFlight, it.airport, metadata.updateTimestamp)
            }
        }
    }

    private fun registerTakeoff(flightEntity: FlightEntity): FlightEntity {
        flightEntity.status = FlightEntity.Status.IN_FLIGHT
        log.info { "Registered takeoff: ${flightEntity.id} to ${flightEntity.callsign}:${flightEntity.cid} departing ${flightEntity.startLocation.code}" }
        return flightRepository.save(flightEntity)
    }

    private fun updateAirborneLocation(
        flightEntity: FlightEntity,
        airportEntity: AirportEntity,
        time: LocalDateTime
    ): FlightEntity {
        flightEntity.endLocation = airportEntity
        flightEntity.endTime = time
        log.info { "Updated In Flight Position: ${flightEntity.id} to ${flightEntity.callsign}:${flightEntity.cid} to ${flightEntity.endLocation!!.code}" }
        return flightRepository.save(flightEntity)
    }

    /**
     * Try to match the airborne flight with a departing flight at the same location.
     */
    private fun retrieveDepartingFlight(clientWithLocation: ClientWithLocation) =
        flightRepository.findByCidAndStatusAndStartLocation(
            clientWithLocation.client.cid,
            FlightEntity.Status.DEPARTING,
            clientWithLocation.airport
        )

    private fun retrieveAirborneFlight(clientWithLocation: ClientWithLocation) =
        flightRepository.findByCidAndStatus(clientWithLocation.client.cid, FlightEntity.Status.IN_FLIGHT)
}
