package dev.stevecrow.vatsim.data.flight

import dev.stevecrow.vatsim.data.airport.AirportRepository
import dev.stevecrow.vatsim.data.client.Client
import dev.stevecrow.vatsim.data.client.ClientWithLocation
import dev.stevecrow.vatsim.data.http.Batch
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class FlightProcessingService(
    private val groundFlightProcessingService: GroundFlightProcessingService,
    private val airborneFlightProcessingService: AirborneFlightProcessingService,
    private val airportRepository: AirportRepository
) {
    private val log = KotlinLogging.logger {}

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
                airportRepository.findNearestAirport(it.latitude, it.longitude)
            )
        }
}
