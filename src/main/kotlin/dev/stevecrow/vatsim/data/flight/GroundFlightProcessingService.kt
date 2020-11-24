package dev.stevecrow.vatsim.data.flight

import dev.stevecrow.vatsim.data.client.ClientWithLocation
import dev.stevecrow.vatsim.data.http.Metadata
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class GroundFlightProcessingService(private val flightRepository: FlightRepository) {
    private val log = KotlinLogging.logger {}

    fun process(metadata: Metadata, clients: List<ClientWithLocation>) {
        clients.forEach {
            if (it.position != ClientWithLocation.Position.GROUND) {
                throw IllegalArgumentException("Received client that was airborne.")
            }

            val potentialFlight = retrieveExistingFlight(it)
            when {
                potentialFlight == null -> {
                    registerNewFlight(it, metadata)
                }
                potentialFlight.status == FlightEntity.Status.IN_FLIGHT -> {
                    registerLanding(potentialFlight, it, metadata)
                }
                potentialFlight.status == FlightEntity.Status.DEPARTING -> {
                    changeDeparture(potentialFlight, it, metadata)
                }
            }
        }
    }

    private fun registerNewFlight(clientWithLocation: ClientWithLocation, metadata: Metadata): FlightEntity {
        val flightEntity = FlightEntity(
            callsign = clientWithLocation.client.callsign,
            cid = clientWithLocation.client.cid,
            startTime = metadata.updateTimestamp,
            startLocation = clientWithLocation.airport,
            status = FlightEntity.Status.DEPARTING
        )

        log.info { "Registered flight: ${flightEntity.id} to ${flightEntity.callsign}:${flightEntity.cid} departing ${flightEntity.startLocation.code}" }
        return flightRepository.save(flightEntity)
    }

    private fun registerLanding(
        flightEntity: FlightEntity,
        clientWithLocation: ClientWithLocation,
        metadata: Metadata
    ): FlightEntity {
        flightEntity.endLocation = clientWithLocation.airport
        flightEntity.endTime = metadata.updateTimestamp
        flightEntity.status = FlightEntity.Status.LANDED
        log.info { "Registered Landing: ${flightEntity.id} to ${flightEntity.callsign}:${flightEntity.cid} landing ${flightEntity.endLocation!!.code}" }
        return flightRepository.save(flightEntity)
    }

    private fun changeDeparture(
        flightEntity: FlightEntity,
        clientWithLocation: ClientWithLocation,
        metadata: Metadata
    ): FlightEntity {
        flightEntity.startLocation = clientWithLocation.airport
        flightEntity.startTime = metadata.updateTimestamp
        flightEntity.status = FlightEntity.Status.DEPARTING
        log.info { "Changed departure location for: ${flightEntity.id} to ${flightEntity.callsign}:${flightEntity.cid} departing ${flightEntity.startLocation.code}" }
        return flightRepository.save(flightEntity)
    }

    fun retrieveExistingFlight(clientWithLocation: ClientWithLocation) =
        retrieveDepartingFlight(clientWithLocation) ?: retrieveAirborneFlight(clientWithLocation)

    fun retrieveDepartingFlight(clientWithLocation: ClientWithLocation) =
        flightRepository.findByCidAndStatus(clientWithLocation.client.cid, FlightEntity.Status.DEPARTING)

    fun retrieveAirborneFlight(clientWithLocation: ClientWithLocation) =
        flightRepository.findByCidAndStatus(clientWithLocation.client.cid, FlightEntity.Status.IN_FLIGHT)
}
