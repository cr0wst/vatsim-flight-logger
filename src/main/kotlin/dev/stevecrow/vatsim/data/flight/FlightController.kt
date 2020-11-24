package dev.stevecrow.vatsim.data.flight

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class FlightController(
    val flightRepository: FlightRepository
) {
    @GetMapping("/v1/flights")
    fun get() = flightRepository.findAll()

    @GetMapping("/v1/flights/{callsign}")
    fun getCallsign(@PathVariable("callsign") callsign: String) = flightRepository.findAllByCallsign(callsign)
}
