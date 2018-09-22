package kamon.jdbc

import com.typesafe.config.ConfigFactory
import kamon.jdbc.sql.SqlObfuscator2

object Test extends App {

  val sql = """select hotel_id,count(1) from Bulks.booking_bulk where hotel_id in (
              |select mh.* , hl.name, hl.id_city, dh.name, hl.`address`, dh.`address`, hl.latitude,hl.longitude, dh.latitude,dh.longitude
              |from Sourcing.mapping_hotels mh
              |      left join Sourcing.hotels_list hl on mh.id_hotel_c = hl.id_hotel and hl.id_site=2
              |      inner join Sourcing.dpg_hotels dh on dh.id_hotel = mh.id_hotel_dpg
              |where id_hotel_dpg=490230
              | and mh.id_site =2) and process_date > ‘2018-08-01’
              |group by hotel_id""".stripMargin

  val jdbcConfig = ConfigFactory.load().getConfig("kamon.jdbc")

  private val obfuscator = new SqlObfuscator2(jdbcConfig)
  println(obfuscator.obfuscate(sql))
  println(obfuscator.obfuscate(sql))

}
