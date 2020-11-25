package dev.stevecrow.vatsim.data.airport

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import kotlin.math.pow

@Entity
@Table(name = "airports")
class AirportEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "name")
    val name: String,

    @Column(name = "iso_country")
    val country: String,

    @Column(name = "municipality")
    val municipality: String,

    @Column(name = "gps_code")
    val code: String,

    @Column(name = "longitude_deg")
    val longitude: Float,

    @Column(name = "latitude_deg")
    val latitude: Float,

    @Column(name = "elevation_ft")
    val elevation: Int = 0
) {
    fun distance(airport: AirportEntity) =
        ((airport.latitude - latitude).pow(2) + (airport.longitude - longitude).pow(2)).pow(0.5F)
}

@Repository
interface AirportRepository : JpaRepository<AirportEntity, Long>
