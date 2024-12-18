import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import ch.hsr.geohash.GeoHash

class SparkJobTest extends AnyFunSuite {

  val spark: SparkSession = SparkSession.builder()
    .appName("SparkTest")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._


test("api mock"){
    //loading our csv data
    val filepath = "src/test/resources/part-00003-c8acc470-919e-4ea9-b274-11488238c85e-c000.csv"
    val data = spark.read.option("header", "true")
    .option("inferSchema", "true")
    .csv(filepath)

    //query null records from our restaurant dataset
    val nullRecords = data.filter(col("lat").isNull || col("lng").isNull)
    nullRecords.show()
    
    //mocking http call
    val mockFetchCoordinatesFromAPI = udf((city: String, country: String) => {
        city match {
    case "Paris"  => (48.861, 2.368)  // Coordinates for Paris
    case "London" => (51.495, -0.191) // Coordinates for London
    case _        => (0.0, 0.0)         // Default or API failure
  }
    })

    //apply udf
    val result = data
    .withColumn("lat",when(col("lat").isNull,mockFetchCoordinatesFromAPI(col("city"),col("country"))
    .getField("_1"))
    .otherwise(col("lat")))
    .withColumn("lng",when(col("lng").isNull,mockFetchCoordinatesFromAPI(col("city"),col("country"))
    .getField("_2"))
    .otherwise(col("lng")))

    //checking results
    val joined = nullRecords
    .join(result, Seq("id"), "inner")
    .select(
        nullRecords("id"),
        nullRecords("city").as("original_city"),
        nullRecords("country").as("original_country"),
        nullRecords("lat").as("original_lat"),
        nullRecords("lng").as("original_lng"),
        result("lat").as("updated_lat"),
        result("lng").as("updated_lng"),
    )
    joined.show()

    //assertions
    joined.collect().foreach(row => {
    assert(Option(row.getAs[Double]("updated_lat")).isDefined)
    assert(Option(row.getAs[Double]("updated_lng")).isDefined)
})


    test("Test geohashUDF using sample data") {
    // sample data
    val weather_data = Seq(
      (21.5478, -104.423),  
      (21.551, -104.374),  
      (21.5541, -104.325)    
    ).toDF("lat", "lng")

    // Define the geohash UDF
    val geohashUDF = udf((lat: Double, lng: Double) => {
      if (!lat.isNaN && !lng.isNaN && lat != 0.0 && lng != 0.0) {
        GeoHash.withCharacterPrecision(lat, lng, 4).toBase32
      } else {
        null
      }
    })

    //apply udf
    val result2 = weather_data.withColumn("geohash", geohashUDF(col("lat"), col("lng")))

    // Collect the results
    val geohashes = result2.select("geohash").as[String].collect()

    // Expected geohashes (calculated separately)
    val expectedGeohashes = Seq("9evd", "9evf", "9evf")

    // Assert that the geohashes match
    assert(geohashes sameElements expectedGeohashes)
  
}

}
