package dev.stevecrow.vatsim.data.http

import com.fasterxml.jackson.annotation.JsonProperty
import dev.stevecrow.vatsim.data.client.Client

data class Batch(
    @JsonProperty("general")
    val metadata: Metadata,
    val clients: List<Client>
)
