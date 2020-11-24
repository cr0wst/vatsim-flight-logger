package dev.stevecrow.vatsim.data.airport

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "airports")
class AirportEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "gps_code")
    val code: String,

    @Column(name = "longitude_deg")
    val longitude: Float,

    @Column(name = "latitude_deg")
    val latitude: Float,

    @Column(name = "elevation_ft")
    val elevation: Int = 0
)

@Repository
interface AirportRepository : JpaRepository<AirportEntity, Long> {
    @Query(
        value = "SELECT * FROM airports ORDER BY ((longitude_deg - ?2) ^ (2)) + ((latitude_deg - ?1) ^ (2)) LIMIT 1",
        nativeQuery = true
    )
    fun findNearestAirport(latitude: Float, longitude: Float): AirportEntity
}
