

## Description
This Spark application processes restaurant and weather data, adds geospatial information using external APIs, and calculates geohash values for mapping. 

## Table of Contents
1. [Setup](#setup)
2. [Execution](#execution)
3. [Output](#output)
4. [Dependencies](#dependencies)
5. [Repository Link](#repository-link)

## Setup

### Prerequisites
- Apache Spark 3.5.3
- Java 8 or higher
- Scala 2.12
- sbt (Scala Build Tool)

### Clone the repository
Clone the GitHub repository containing this Spark application:
```bash
git clone https://github.com/illthinkofthenamelater/spark-application.git
cd spark-application
```

### Build the project
Use sbt to build the project:
```bash
sbt clean assembly
```
This will create a fat JAR file in the `target/scala-2.12/` directory.


### Running the application
Run the Spark application using the following command:
```bash
spark-submit \
  --class SparkJob \
  --master local[*] \
  target/scala-2.12/spark-application-assembly-1.0.jar
```

### Commands
- `--class SparkJob`: main class of the application
- `--master local[*]`: application runned locally using all available cores
- `target/scala-2.12/spark-application-assembly-1.0.jar`: jar file path

## Output

```
1. Restaurant Data with geohash
+------------+------------+----------------+-----------------------+-------+----------+------+-------+-------+
|          id|franchise_id|  franchise_name|restaurant_franchise_id|country|      city|   lat|    lng|geohash|
+------------+------------+----------------+-----------------------+-------+----------+------+-------+-------+
|197568495625|          10|The Golden Spoon|                  24784|     US|   Decatur|34.578|-87.021|   dn4h|
| 17179869242|          59|     Azalea Cafe|                  10902|     FR|     Paris|48.861|  2.368|   u09t|
|214748364826|          27| The Corner Cafe|                  92040|     US|Rapid City| 44.08|-103.25|   9xyd|
|154618822706|          51|    The Pizzeria|                  41484|     AT|    Vienna|48.213| 16.413|   u2ed|
|163208757312|          65|   Chef's Corner|                  96638|     GB|    London|51.495| -0.191|   gcpu|
+------------+------------+----------------+-----------------------+-------+----------+------+-------+-------+

2. Weather Data with geohash
+--------+-------+----------+----------+----------+---+
|     lng|    lat|avg_tmpr_f|avg_tmpr_c| wthr_date|day|
+--------+-------+----------+----------+----------+---+
|-104.423|21.5478|      70.3|      21.3|2017-09-24| 24|
|-104.374| 21.551|      69.4|      20.8|2017-09-24| 24|
|-104.325|21.5541|      68.6|      20.3|2017-09-24| 24|
|-104.276|21.5573|      67.8|      19.9|2017-09-24| 24|
|-104.227|21.5604|      67.2|      19.6|2017-09-24| 24|
+--------+-------+----------+----------+----------+---+


3. Joined tables on geohash column, saved in parquet format
+-------+------------+------------+----------------+-----------------------+-------+----------+------+-------+--------+-------+----------+----------+----------+---+
|geohash|          id|franchise_id|  franchise_name|restaurant_franchise_id|country|      city|   lat|    lng|     lng|    lat|avg_tmpr_f|avg_tmpr_c| wthr_date|day|
+-------+------------+------------+----------------+-----------------------+-------+----------+------+-------+--------+-------+----------+----------+----------+---+
|   dnqx|103079215121|          18|The Harvest Room|                   4340|     US|Mount Airy|36.481|-80.602|-80.8559|36.4223|      70.5|      21.4|2017-09-24| 24|
|   dnqx|103079215121|          18|The Harvest Room|                   4340|     US|Mount Airy|36.481|-80.602|-80.8006|36.4176|      69.6|      20.9|2017-09-24| 24|
|   dnqx|103079215121|          18|The Harvest Room|                   4340|     US|Mount Airy|36.481|-80.602|-80.7453|36.4129|      69.3|      20.7|2017-09-24| 24|
|   dnqx|103079215121|          18|The Harvest Room|                   4340|     US|Mount Airy|36.481|-80.602|  -80.69|36.4082|      70.2|      21.2|2017-09-24| 24|
|   dnqx|103079215121|          18|The Harvest Room|                   4340|     US|Mount Airy|36.481|-80.602|-80.6347|36.4035|      70.5|      21.4|2017-09-24| 24|
+-------+------------+------------+----------------+-----------------------+-------+----------+------+-------+--------+-------+----------+----------+----------+---+
```

**Unit tests for UDF functions are implemented in src/test/scala**


## Dependencies

The project uses the following dependencies:
- **Apache Spark (3.5.3)**: Core and SQL modules for data processing
- **Circe (0.14.10)**: JSON parsing for API responses
- **STTP Client (3.10.1)**: HTTP client for making API calls
- **GeoHash (1.4.0)**: Library for geohashing

These dependencies are specified in the `build.sbt` file

## Repository Link

[https://github.com/illthinkofthenamelater/spark-application](https://github.com/illthinkofthenamelater/spark-application)

