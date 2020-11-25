package dev.stevecrow.vatsim.data.flight

import dev.stevecrow.vatsim.data.airport.AirportEntity
import dev.stevecrow.vatsim.data.client.ClientWithLocation
import dev.stevecrow.vatsim.data.http.Metadata
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.math.absoluteValue

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
                hasFlightLanded(potentialFlight, it) -> {
                    registerLanding(potentialFlight, it, metadata)
                }
                hasDepartureChanged(potentialFlight, it) -> {
                    changeDeparture(potentialFlight, it, metadata)
                }
            }
        }
    }

    private fun hasDepartureChanged(flight: FlightEntity, client: ClientWithLocation) =
        if (flight.status != FlightEntity.Status.DEPARTING) {
            false
        } else {
            !flightIsNearAirport(flight.startLocation, client.airport)
        }

    private fun hasFlightLanded(flight: FlightEntity, client: ClientWithLocation) =
        if (flight.endLocation == null || flight.status != FlightEntity.Status.IN_FLIGHT) {
            false
        } else {
            flightIsNearAirport(flight.endLocation!!, client.airport)
        }

    private fun flightIsNearAirport(flightAirport: AirportEntity, airport: AirportEntity) =
        flightAirport.id == airport.id || (flightAirport.distance(airport)).absoluteValue <= AIRPORT_BUFFER_DISTANCE

    private fun registerNewFlight(clientWithLocation: ClientWithLocation, metadata: Metadata): FlightEntity {
        val flightEntity = FlightEntity(
            callsign = clientWithLocation.client.callsign,
            cid = clientWithLocation.client.cid,
            realname = clientWithLocation.client.realname,
            startTime = metadata.updateTimestamp,
            startLocation = clientWithLocation.airport,
            status = FlightEntity.Status.DEPARTING
        )

        log.info { "[New Flight] ${flightEntity.callsign}:${flightEntity.cid} @ ${flightEntity.startLocation.name} (${flightEntity.startLocation.code})" }
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
        log.info { "[Landing] ${flightEntity.callsign}:${flightEntity.cid}- ${flightEntity.startLocation.name} (${flightEntity.startLocation.code}) => ${flightEntity.endLocation!!.name} (${flightEntity.endLocation!!.code})" }
        return flightRepository.save(flightEntity)
    }

    private fun changeDeparture(
        flightEntity: FlightEntity,
        clientWithLocation: ClientWithLocation,
        metadata: Metadata
    ): FlightEntity {
        log.info { "[Departure Change] ${flightEntity.callsign}:${flightEntity.cid}- ${flightEntity.startLocation.name} (${flightEntity.startLocation.code}) => ${clientWithLocation.airport.name} (${clientWithLocation.airport.code})" }
        flightEntity.startLocation = clientWithLocation.airport
        flightEntity.startTime = metadata.updateTimestamp
        flightEntity.status = FlightEntity.Status.DEPARTING
        return flightRepository.save(flightEntity)
    }

    fun retrieveExistingFlight(clientWithLocation: ClientWithLocation) =
        retrieveDepartingFlight(clientWithLocation) ?: retrieveAirborneFlight(clientWithLocation)

    fun retrieveDepartingFlight(clientWithLocation: ClientWithLocation) =
        flightRepository.findByCidAndStatus(clientWithLocation.client.cid, FlightEntity.Status.DEPARTING)

    fun retrieveAirborneFlight(clientWithLocation: ClientWithLocation) =
        flightRepository.findByCidAndStatus(clientWithLocation.client.cid, FlightEntity.Status.IN_FLIGHT)
}
