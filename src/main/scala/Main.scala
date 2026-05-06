import naka._
import org.apache.spark.sql.types._
import org.apache.spark.sql.SparkSession

object Main {
  def main(argv: Array[String]): Unit = {
    argv.headOption match {
      case Some("convert") => convert
      case Some(_) => println("Usage: Main [convert]")
      case _ => run
    }
  }

  private def convert = {
    val matchSchema = StructType(
      Array(
        StructField("Div", StringType, true),
        StructField("Date", StringType, true),
        StructField("Time", StringType, true),
        StructField("HomeTeam", StringType, true),
        StructField("AwayTeam", StringType, true),
        StructField("FTHG", IntegerType, true),
        StructField("FTAG", IntegerType, true),
      ),
    )

    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("SparkNaka")
      .getOrCreate()

    val rawMatches = spark.read
      .option("header", "true")
      .schema(matchSchema)
      .csv("./data/E0/*.csv", "./data/E1/*.csv")

    rawMatches.write
      .mode("overwrite")
      .parquet("data/matches.parquet")
  }

  private def run = {
    val spark = SparkSession
      .builder()
      .withExtensions(new SparkNaka)
      .master("local[*]")
      .appName("SparkNaka")
      .getOrCreate()

    val df1 = spark.read
//      .option("header", "true")
//      .csv("data/E0", "data/E1")
      .parquet("./data/matches.parquet")
      .createTempView("matches")

    val df2 = spark
      .sql(
        "select distinct epoch(Date) from matches limit 10",
      )

    df2.show()
    println(df2.queryExecution)
//    println(df.queryExecution)

  }
}
