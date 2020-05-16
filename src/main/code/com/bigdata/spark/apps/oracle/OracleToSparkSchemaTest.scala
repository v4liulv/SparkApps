package com.bigdata.spark.apps.oracle

import com.sinobest.framework.util.PropertiesUtil
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.types.{DecimalType, IntegerType}

import scala.collection.mutable.ArrayBuffer

/**
  * 打印Oracle常用字段类型读取到Spark后对应字段类型,对应表如下
  * |-- VARCHAR2: string (nullable = true)
  * |-- NVARCHAR2: string (nullable = true)
  * |-- INTEGER: decimal(38,0) (nullable = true)
  * |-- NUMBER(10,4): decimal(10,4) (nullable = true)
  * |--  NUMBER(30,7): decimal(30,7) (nullable = true)
  * |-- NUMBER: decimal(38,10) (nullable = true)
  * |-- DATE: timestamp (nullable = true)
  * |-- TIMESTAMP: timestamp (nullable = true)
  * |-- CHAR: string (nullable = true)
  * |-- CLOB: string (nullable = true)
  * |-- BLOB: binary (nullable = true)
  * |-- RAW: binary (nullable = true)
  *
  * 描述问题：不完美的是没有将NUMBER标度为零的转换为Int，还是以DECIMAL(38,0)的形式表示虽然都是表示的整数，
  * 但是在后面Spark读取hive或者其他数据库的时候，还需要将DECIMAL转为Int
  *
  * @author liulv 
  * @date 2020/2/9 1:39
  */
class OracleToSparkSchemaTest {
  def printOracleSchema(): Unit ={
    val spark = SparkSession.builder().appName("OracleSchema").master("local[2]").getOrCreate()

    val property = PropertiesUtil.getProperties("spark-oracle.properties")
    property.setProperty(JDBCOptions.JDBC_TABLE_NAME, "SPARK_SCHEMA_TEST")
    import scala.collection.JavaConversions._
    val jdbcDF = spark.read.format("jdbc").options(property).load()

    jdbcDF.printSchema()

    //下面实现将DecimalType(38, 0)列名转换为int
    val colName = ArrayBuffer[String]()
    val schema = jdbcDF.schema.foreach(s => {
      //需要转换的列名
      if (s.dataType.equals(DecimalType(38, 0))) {
        colName += s.name
      }
    })
    var df_int = jdbcDF
    import org.apache.spark.sql.functions._
    colName.foreach(name => {
      df_int = df_int.withColumn(name, col(name).cast(IntegerType))
    })
    df_int.printSchema()
    df_int.show

    spark.stop()
  }
}

object OracleToSparkSchemaTest {
  def apply(): OracleToSparkSchemaTest = new OracleToSparkSchemaTest()

  def main(args: Array[String]): Unit = {
    apply.printOracleSchema()
  }
}
