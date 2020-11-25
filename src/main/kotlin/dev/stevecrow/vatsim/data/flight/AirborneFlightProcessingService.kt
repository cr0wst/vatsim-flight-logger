package dev.stevecrow.vatsim.data.flight

import dev.stevecrow.vatsim.data.client.ClientWithLocation
import dev.stevecrow.vatsim.data.http.Metadata
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class AirborneFlightProcessingService(private val flightRepository: FlightRepository) {
    private val log = KotlinLogging.logger {}

    fun process(metadata: Metadata, clients: List<ClientWithLocation>) {
        clients.forEach {
            if (it.position != ClientWithLocation.Position.AIR) {
                throw IllegalArgumentException("Received client that was on the ground.")
            }

            val potentialFlight = retrieveDepartingFlight(it)
            if (potentialFlight != null) {
                registerTakeoff(potentialFlight)
            }
        }
    }

    private fun registerTakeoff(flightEntity: FlightEntity): FlightEntity {
        flightEntity.status = FlightEntity.Status.IN_FLIGHT
        log.info { "Registered takeoff: ${flightEntity.id} to ${flightEntity.callsign}:${flightEntity.cid} departing ${flightEntity.startLocation.code}" }
        return flightRepository.save(flightEntity)
    }

    /**
     * Try to match the airborne flight with a departing flight at the same location.
     */
    fun retrieveDepartingFlight(clientWithLocation: ClientWithLocation) =
        flightRepository.findByCidAndStatusAndStartLocation(clientWithLocation.client.cid, FlightEntity.Status.DEPARTING, clientWithLocation.airport)
}
