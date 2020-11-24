package dev.stevecrow.vatsim.data.batch

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

/**
 * Batches represent a historical record of pulling data from VATSIM.
 */
@Entity
@Table(name = "batches")
class BatchEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val connectedClients: Int,

    val uniqueUsers: Int,

    val timestamp: LocalDateTime,
)

@Repository
interface BatchRepository : JpaRepository<BatchEntity, Long>
