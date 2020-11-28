package dev.stevecrow.vatsim.data.flight

import dev.stevecrow.vatsim.data.airport.AirportEntity
import dev.stevecrow.vatsim.data.airport.AirportRepository
import dev.stevecrow.vatsim.data.client.Client
import dev.stevecrow.vatsim.data.client.ClientWithLocation
import dev.stevecrow.vatsim.data.http.Batch
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.math.pow

// Sometimes airports can transition from air to ground can be over
// another airport. We need to buffer the range of acceptable transition airports.
// Theoretically they shouldn't be this far out between pulls.
const val AIRPORT_BUFFER_DISTANCE = 10

@Service
class FlightProcessingService(
    private val groundFlightProcessingService: GroundFlightProcessingService,
    private val airborneFlightProcessingService: AirborneFlightProcessingService,
    private val disconnectedFlightProcessingService: DisconnectedFlightProcessingService,
    private val airportRepository: AirportRepository
) {
    private val log = KotlinLogging.logger {}
    private var airportCache: List<AirportEntity> = emptyList()

    fun process(batch: Batch) {
        log.info { "Processing batch (${batch.metadata.updateTimestamp}) with ${batch.clients.size} clients." }
        disconnectedFlightProcessingService.process(batch)

        val clientsWithLocations = pairClientsWithLocations(batch.clients.filter { it.clientType == Client.Type.PILOT })
        val (ground, air) = clientsWithLocations.partition { it.position == ClientWithLocation.Position.GROUND }

        log.info { "Ground: ${ground.size} | Air: ${air.size}" }
        groundFlightProcessingService.process(batch.metadata, ground)
        airborneFlightProcessingService.process(batch.metadata, air)

        log.info { "Batch (${batch.metadata.updateTimestamp}) finished." }
    }

    private fun pairClientsWithLocations(clients: List<Client>) =
        clients.map {
            ClientWithLocation(it, findNearestAirport(it.latitude, it.longitude))
        }

    private fun findNearestAirport(latitude: Float, longitude: Float): AirportEntity {
        if (airportCache.isEmpty()) {
            airportCache = airportRepository.findAll()
            log.info { "Populating airport cache with ${airportCache.size}" }
        }

        return airportCache.minByOrNull {
            ((it.latitude - latitude).pow(2) + (it.longitude - longitude).pow(2)).pow(2)
        }!!
    }
}
