package dev.stevecrow.vatsim.data.http

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.jackson.responseObject
import org.springframework.stereotype.Service

private const val ENDPOINT = "http://cluster.data.vatsim.net/vatsim-data.json"

@Service
class VatsimDataRetrievalService {
    fun get() = Fuel.get(ENDPOINT).responseObject<Batch>().third.get()
}
