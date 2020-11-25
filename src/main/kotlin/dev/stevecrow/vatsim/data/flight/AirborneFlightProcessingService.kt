package dev.stevecrow.vatsim.data.flight

import dev.stevecrow.vatsim.data.airport.AirportEntity
import dev.stevecrow.vatsim.data.client.ClientWithLocation
import dev.stevecrow.vatsim.data.http.Metadata
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.math.absoluteValue

@Service
class AirborneFlightProcessingService(private val flightRepository: FlightRepository) {
    private val log = KotlinLogging.logger {}

    fun process(metadata: Metadata, clients: List<ClientWithLocation>) {
        clients.forEach {
            if (it.position != ClientWithLocation.Position.AIR) {
                throw IllegalArgumentException("Received client that was on the ground.")
            }

            val potentialFlight = retrieveDepartingFlight(it) ?: retrieveAirborneFlight(it)
            if (potentialFlight != null) {
                if (hasFlightDeparted(potentialFlight, it)) {
                    registerTakeoff(potentialFlight, it.airport, metadata.updateTimestamp)
                } else if (hasInFlightLocationChanged(potentialFlight, it)) {
                    updateAirborneLocation(potentialFlight, it.airport, metadata.updateTimestamp)
                }
            } else {
                // If somebody connects in the air or we first pick them up in the air, we'll start them there.
                // This is probably unreliable.
                val flight = FlightEntity(
                    callsign = it.client.callsign,
                    cid = it.client.cid,
                    realname = it.client.realname,
                    startTime = metadata.updateTimestamp,
                    startLocation = it.airport,
                    status = FlightEntity.Status.DEPARTING
                )
                log.info { "[In Flight Discovery] ${flight.callsign}:${flight.cid}- ${flight.startLocation.name} (${flight.startLocation.code})" }
                registerTakeoff(flight, it.airport, metadata.updateTimestamp)
            }
        }
    }

    private fun hasInFlightLocationChanged(flight: FlightEntity, client: ClientWithLocation) =
        (flight.status == FlightEntity.Status.IN_FLIGHT && flight.endLocation != null && flight.endLocation?.id != client.airport.id)

    private fun hasFlightDeparted(flight: FlightEntity, client: ClientWithLocation) =
        if (flight.status != FlightEntity.Status.DEPARTING) {
            false
        } else {
            flightIsNearAirport(flight.startLocation, client.airport)
        }

    private fun flightIsNearAirport(flightAirport: AirportEntity, airport: AirportEntity) =
        flightAirport.id == airport.id || (flightAirport.distance(airport)).absoluteValue <= AIRPORT_BUFFER_DISTANCE

    private fun registerTakeoff(
        flightEntity: FlightEntity,
        airportEntity: AirportEntity,
        time: LocalDateTime
    ): FlightEntity {
        flightEntity.status = FlightEntity.Status.IN_FLIGHT
        flightEntity.endLocation = airportEntity
        flightEntity.endTime = time
        log.info { "[Takeoff] ${flightEntity.callsign}:${flightEntity.cid}- ${flightEntity.startLocation.name} (${flightEntity.startLocation.code})" }
        return flightRepository.save(flightEntity)
    }

    private fun updateAirborneLocation(
        flightEntity: FlightEntity,
        airportEntity: AirportEntity,
        time: LocalDateTime
    ): FlightEntity {
        log.info { "[Airborne Position Change] ${flightEntity.callsign}:${flightEntity.cid}- ${flightEntity.startLocation.name} (${flightEntity.startLocation.code}) => ${airportEntity.name} (${airportEntity.code})" }
        flightEntity.endLocation = airportEntity
        flightEntity.endTime = time
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
