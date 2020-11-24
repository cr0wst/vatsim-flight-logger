package dev.stevecrow.vatsim.data.http

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import java.time.LocalDateTime

data class Metadata(
    val version: String,
    val reload: String,
    val update: String,
    @JsonProperty(value = "update_timestamp")
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val updateTimestamp: LocalDateTime,
    @JsonProperty(value = "connected_clients")
    val connectedClients: Int,
    @JsonProperty(value = "unique_users")
    val uniqueUsers: Int
)
