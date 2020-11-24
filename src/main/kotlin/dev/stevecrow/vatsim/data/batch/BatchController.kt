package dev.stevecrow.vatsim.data.batch

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class BatchController(
    val batchRepository: BatchRepository
) {
    @GetMapping("/v1/batch")
    fun get(): List<BatchEntity> = batchRepository.findAll()
}
