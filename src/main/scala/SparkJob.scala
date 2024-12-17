import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.UserDefinedFunction
import sttp.client3._
import sttp.client3.circe._
import io.circe.Json
import io.circe.parser._
import ch.hsr.geohash.GeoHash

object SparkJob {
  def main(args: Array[String]): Unit = {

    // Spark Session 
    val spark = SparkSession.builder()
      .appName("SparkHW")
      .master("local[*]") 
      .getOrCreate()

    // File paths
    val restaurantDataPath = "restaurant_csv/" 
    val weatherDataPath = "weather/year=2017/month=09/"       
    // Read restaurant data 
    val restaurantData = spark.read.option("header", "true").option("inferSchema", "true").csv(restaurantDataPath)
    restaurantData.show(5)  

    // Read weather data 
    val weatherData = spark.read.parquet(weatherDataPath)
    weatherData.show(5) 

    // UDF to call OpenCage API to fetch coordinates if lat/lng is null
    def fetchCoordinatesFromAPI(city: String, country: String): Option[(Double, Double)] = {
      val apiKey = "51f00bcdd3e543f890757aa004da19ab"
      val url = s"https://api.opencagedata.com/geocode/v1/json?q=$city,$country&key=$apiKey"
      
      // Making API call
      val request = basicRequest.get(uri"$url").response(asJson[Json])
      val backend = HttpURLConnectionBackend()

      val response = request.send(backend)

      response.body match {
        case Right(json) =>
          //val lat = json.hcursor.downField("results").downArray.downField("geometry").downField("lat").as[Double].getOrElse(0.0)
          //val lng = json.hcursor.downField("results").downArray.downField("geometry").downField("lng").as[Double].getOrElse(0.0)
          //if (!lat.isNaN && !lng.isNaN) Some((lat, lng)) else None
           val latOption = json.hcursor.downField("results").downArray.downField("geometry").downField("lat").as[Double].toOption
           val lngOption = json.hcursor.downField("results").downArray.downField("geometry").downField("lng").as[Double].toOption
           
    (latOption, lngOption) match {
      case (Some(lat), Some(lng)) if !lat.isNaN && !lng.isNaN =>
        Some((lat, lng))
      case _ => None  
    }

        case Left(error) => 
        println(s"API REQUEST FAILED NOOOO: $error")
        None //if i catch you Ricky!!!
      }
    }

    // Register udf
    val fetchCoordinatesUDF = udf((city: String, country: String) => {
  if (city != null && country != null) {
    fetchCoordinatesFromAPI(city, country).getOrElse((0.0, 0.0))
  } else {
    (0.0, 0.0)  
  }
})

val restaurantWithCoordinates = restaurantData.withColumn("lat",when(col("lat").isNull,fetchCoordinatesUDF(col("city"),col("country")).getField("_1")).otherwise(col("lat"))).withColumn("lng",when(col("lng").isNull,fetchCoordinatesUDF(col("city"),col("country")).getField("_2")).otherwise(col("lng")))
    
    /*val restaurantWithCoordinates = restaurantData
      .withColumn("lat", when(col("lat").isNull, fetchCoordinatesUDF(col("city"), col("country")).getField("_1")/*.cast("double")*/)
        .otherwise(col("lat")/*.cast("double")*/))
      .withColumn("lng", when(col("lng").isNull, fetchCoordinatesUDF(col("city"), col("country")).getField("_2")/*.cast("double")*/)
        .otherwise(col("lng")/*.cast("double")*/))*/

    restaurantWithCoordinates.show(5)


    // UDF to generate geohash from latitude and longitude
    val geohashUDF: UserDefinedFunction = udf((lat: Double, lng: Double) => {
      if (!lat.isNaN && !lng.isNaN) {
        val geoHash = GeoHash.withCharacterPrecision(lat, lng, 4) // 4-character geohash
        geoHash.toBase32
      } else {
        null
      }
    })

    // Add geohash column to restaurant data
    val restaurantWithGeohash = restaurantWithCoordinates
      .withColumn("geohash", geohashUDF(col("lat"), col("lng")))

    restaurantWithGeohash.show(5)

    // Add geohash to weather data
    val weatherWithGeohash = weatherData
      .withColumn("geohash", geohashUDF(col("lat"), col("lng")))

    weatherWithGeohash.show(5)

    // Left join the restaurant data with weather data on geohash
    val enrichedData = restaurantWithGeohash
      .join(weatherWithGeohash, Seq("geohash"), "left")

    enrichedData.show(5)

    // Write tjoined data
    enrichedData.write
      .partitionBy("geohash")
      .mode("overwrite")
      .parquet("output/enriched_data.parquet")


    // Stop Spark session
    spark.stop()
  }
}












