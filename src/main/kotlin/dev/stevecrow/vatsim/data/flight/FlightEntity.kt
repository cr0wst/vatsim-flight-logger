package dev.stevecrow.vatsim.data.flight

import dev.stevecrow.vatsim.data.airport.AirportEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "flights")
class FlightEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val callsign: String,
    val cid: String,
    val realname: String,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime? = null,

    @ManyToOne
    var startLocation: AirportEntity,

    @ManyToOne
    var endLocation: AirportEntity? = null,

    @Enumerated(value = EnumType.STRING)
    var status: Status = Status.UNKNOWN
) {
    val duration
        get(): Duration? =
            if (endTime != null) {
                Duration.between(startTime, endTime)
            } else null

    enum class Status {
        DEPARTING, IN_FLIGHT, LANDED, UNKNOWN
    }
}

@Repository
interface FlightRepository : JpaRepository<FlightEntity, Long> {
    fun findByCidAndStatus(cid: String, status: FlightEntity.Status): FlightEntity?
    fun findAllByCallsign(callsign: String): List<FlightEntity>
    fun findAllByStatus(status: FlightEntity.Status): List<FlightEntity>
}
