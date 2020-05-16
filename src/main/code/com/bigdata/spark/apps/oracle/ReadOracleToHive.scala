package com.bigdata.spark.apps.oracle

import com.bigdata.spark.apps.submit.SparkSubmitUtil
import org.apache.hadoop.conf.Configuration
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.internal.SQLConf

/**
  * 读取Oracle表数据并写入到Hive表
  *
  * @author liulv 
  * @date 2020/1/31 5:09
  */
class ReadOracleToHive{
  def run(): Unit ={
    val sparkBuilder = SparkSession
      .builder
      .master("local[2]")
      .appName("ReadOracleToHive")

    SparkSubmitUtil.setJobHistory(sparkBuilder)

    //解决java.lang.ClassNotFoundException: com.bigdata.spark.apps.hive.ReadOracleToHive$$anonfun$run$2
    sparkBuilder.config("spark.executor.extraClassPath", SparkSubmitUtil.sparkAppJar)
    sparkBuilder.config("spark.jars", SparkSubmitUtil.sparkAppJar)

    val conf = new Configuration()
    import scala.collection.JavaConverters._
    for (c <- conf.iterator().asScala){
      sparkBuilder.config(c.getKey, c.getValue)
    }
    val spark = sparkBuilder.config(SQLConf.PARQUET_WRITE_LEGACY_FORMAT.key, value = true)
      .enableHiveSupport().getOrCreate()

    val ct = 58475050
    val sql_str = s"(select a.*, ROWNUM rownum__rn from pcs_cs_bzk.B_BZ_LY_LGZSXX a) b"
    val table = spark.read
      .format("jdbc")
      .option(JDBCOptions.JDBC_DRIVER_CLASS, "oracle.jdbc.driver.OracleDriver")
      .option(JDBCOptions.JDBC_URL, "jdbc:oracle:thin:@192.168.19.1:1521:orcl")
      .option("user", "pcs_cs_bzk")
      .option("password", "pcs_cs_bzk")
      .option(JDBCOptions.JDBC_TABLE_NAME, sql_str)
      .option(JDBCOptions.JDBC_BATCH_FETCH_SIZE, 100000)
      .option(JDBCOptions.JDBC_PARTITION_COLUMN, "rownum__rn")
      .option(JDBCOptions.JDBC_LOWER_BOUND, 0)
      .option(JDBCOptions.JDBC_UPPER_BOUND, ct)
      .option(JDBCOptions.JDBC_NUM_PARTITIONS, 1)
      .load()
      .drop("rownum__rn")

    table.foreach(x => println(x))

    //通过savaAsTable写入hive表
    table
      .repartition(1)
      .write
      .mode(SaveMode.Overwrite)
      .saveAsTable("bzk.B_BZ_LY_LGZSXX")
  }
}

object ReadOracleToHive {

  def apply(): ReadOracleToHive = new ReadOracleToHive()

  def main(args: Array[String]): Unit =
    apply().run()

}
