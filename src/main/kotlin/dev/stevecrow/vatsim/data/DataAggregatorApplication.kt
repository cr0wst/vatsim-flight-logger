package dev.stevecrow.vatsim.data

import dev.stevecrow.vatsim.data.batch.BatchEntity
import dev.stevecrow.vatsim.data.batch.BatchRepository
import dev.stevecrow.vatsim.data.flight.CleanUpService
import dev.stevecrow.vatsim.data.flight.FlightProcessingService
import dev.stevecrow.vatsim.data.http.VatsimDataRetrievalService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@SpringBootApplication
@EnableScheduling
class DataAggregatorApplication(
    private val vatsimDataRetrievalService: VatsimDataRetrievalService,
    private val batchRepository: BatchRepository,
    private val flightProcessingService: FlightProcessingService,
    private val cleanUpService: CleanUpService
) {
    @Scheduled(fixedRate = 60000)
    fun aggregate() {
        val response = vatsimDataRetrievalService.get()
        batchRepository.save(
            BatchEntity(
                connectedClients = response.metadata.connectedClients,
                uniqueUsers = response.metadata.uniqueUsers,
                timestamp = response.metadata.updateTimestamp
            )
        )

        flightProcessingService.process(response)
    }
}

fun main(args: Array<String>) {
    runApplication<DataAggregatorApplication>(*args)
}
