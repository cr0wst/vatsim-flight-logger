package dev.stevecrow.vatsim.data.client

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Client(
    val callsign: String,
    val cid: String,
    val realname: String,
    @JsonProperty(value = "clienttype")
    val clientType: Type,
    val frequency: String?,
    val latitude: Float,
    val longitude: Float,
    val altitude: Int,
    @JsonProperty(value = "groundspeed")
    val groundSpeed: Int,
    val server: String,
    @JsonProperty(value = "protrevision")
    val protRevision: Int,
    val transponder: Int,
    val heading: Int,
    @JsonProperty(value = "qnh_i_hg")
    val qnhHg: Float,
    @JsonProperty(value = "qnh_mb")
    val qnhMb: Int
) {
    enum class Type {
        PILOT,
        ATC,
        UNKNOWN;

        @JsonCreator
        fun fromString(type: String) =
            values().find { it.name == type.toUpperCase() } ?: UNKNOWN
    }
}
