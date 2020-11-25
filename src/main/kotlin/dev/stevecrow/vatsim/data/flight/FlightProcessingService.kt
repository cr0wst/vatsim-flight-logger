package dev.stevecrow.vatsim.data.flight

import dev.stevecrow.vatsim.data.airport.AirportEntity
import dev.stevecrow.vatsim.data.airport.AirportRepository
import dev.stevecrow.vatsim.data.client.Client
import dev.stevecrow.vatsim.data.client.ClientWithLocation
import dev.stevecrow.vatsim.data.http.Batch
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.math.pow

@Service
class FlightProcessingService(
    private val groundFlightProcessingService: GroundFlightProcessingService,
    private val airborneFlightProcessingService: AirborneFlightProcessingService,
    private val airportRepository: AirportRepository
) {
    private val log = KotlinLogging.logger {}
    private var airportCache: List<AirportEntity> = emptyList()

    fun process(batch: Batch) {
        log.info { "Processing batch (${batch.metadata.updateTimestamp}) with ${batch.clients.size} clients." }
        val clientsWithLocations = pairClientsWithLocations(batch.clients.filter { it.clientType == Client.Type.PILOT })
        val (ground, air) = clientsWithLocations.partition { it.position == ClientWithLocation.Position.GROUND }

        groundFlightProcessingService.process(batch.metadata, ground)
        airborneFlightProcessingService.process(batch.metadata, air)
    }

    private fun pairClientsWithLocations(clients: List<Client>) =
        clients.map {
            ClientWithLocation(
                it,
                findNearestAirport(it.latitude, it.longitude)
            )
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
