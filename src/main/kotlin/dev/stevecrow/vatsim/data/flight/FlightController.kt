package dev.stevecrow.vatsim.data.flight

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class FlightController(
    val flightRepository: FlightRepository
) {
    @GetMapping("/v1/flights")
    fun get() = flightRepository.findAll()

    @GetMapping("/v1/flights/{callsign}")
    fun getCallsign(@PathVariable("callsign") callsign: String) = flightRepository.findAllByCallsign(callsign)

    @GetMapping("/v1/airborne")
    fun airborne() = flightRepository.findAllByStatus(FlightEntity.Status.IN_FLIGHT)

    @GetMapping("/v1/departing")
    fun departing() = flightRepository.findAllByStatus(FlightEntity.Status.DEPARTING)

    @GetMapping("/v1/landed")
    fun landed() = flightRepository.findAllByStatus(FlightEntity.Status.LANDED)
}
