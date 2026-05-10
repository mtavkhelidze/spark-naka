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
        "select Date, HomeTeam, epoch(Date) as Unix, epoch(Date) as TimeStamp from matches sort by unix",
      )
    df2.show()
    println(df2.queryExecution)
//
//    (1 to 5).foreach { i =>
//      val start = System.currentTimeMillis()
//      df2.collect()
//      println(s"Run $i: ${System.currentTimeMillis() - start} ms")
//    }
//    val executedPlan = df2.queryExecution.executedPlan
//    executedPlan.foreach { plan =>
//      println(s"Operator: ${plan.nodeName}")
//      plan.metrics.foreach { case (name, metric) =>
//        println(s"  $name: ${metric.value}")
//      }
//    }
  }
}
