package dev.stevecrow.vatsim.data.client

import dev.stevecrow.vatsim.data.airport.AirportEntity

private const val ALT_BUFFER: Int = 500

data class ClientWithLocation(
    val client: Client,
    val airport: AirportEntity
) {
    val position
        get(): Position = if (client.altitude <= airport.elevation + ALT_BUFFER) {
            Position.GROUND
        } else {
            Position.AIR
        }

    enum class Position {
        GROUND, AIR
    }
}
