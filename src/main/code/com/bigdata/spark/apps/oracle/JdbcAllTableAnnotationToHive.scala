package com.bigdata.spark.apps.oracle

import java.util
import java.util.Properties

import com.bigdata.spark.apps.submit.SparkSubmitUtil
import com.sinobest.framework.util.PropertiesUtil
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.types.{DecimalType, IntegerType, StructField, StructType}

import scala.collection.immutable
import scala.collection.mutable.ArrayBuffer

/**
  * 全量表抽取过程，库必须为空库，Oracle某用户下的全部的表和注释以及数据写入到Hive对应表注释上
  *
  * @author liulv 
  * @date 2020/2/9 19:27
  **/
class JdbcAllTableAnnotationToHive(spark: SparkSession, jdbcOptions: Properties, hiveDB: String) {

  private val extraOptions = new scala.collection.mutable.HashMap[String, String]

  val sql_in_owner = s"('${jdbcOptions.getProperty("user")}')"

  def getDataFrameByJdbc(options: java.util.Map[String, String]): DataFrame ={
    import scala.collection.JavaConversions._
    spark.read.format("jdbc").options(options).load()
  }

  def getAllTableName(): DataFrame ={
    import scala.collection.JavaConversions._
    if(extraOptions.size < 2)
    extraOptions ++= jdbcOptions
    val queryAllTable = s"(select table_name from all_tables where owner in ${sql_in_owner})"
    println(queryAllTable)
    extraOptions.put("dbtable", queryAllTable)
    getDataFrameByJdbc(extraOptions)
  }

  def getAllTableAnnotation(): DataFrame ={
    import scala.collection.JavaConversions._
    if(extraOptions.size < 2)
    extraOptions ++= jdbcOptions
    val queryAllTableAnnotation = s"(select * from all_col_comments where owner in ${sql_in_owner})a"
    println(queryAllTableAnnotation)
    extraOptions.put("dbtable", queryAllTableAnnotation)
    //所有的表字段对应的注释
    getDataFrameByJdbc(extraOptions).repartition(160).cache
  }

  def getFiledAnnotationMap(allColComments : DataFrame, tableName : String) : Map[String,String] ={
    //字段名 和注 对应的map
    allColComments.where(s"TABLE_NAME='${tableName}'")
      .select("COLUMN_NAME", "COMMENTS")
      .na.fill("", Array("COMMENTS"))
      .rdd.map(row => (row.getAs[String]("COLUMN_NAME"), row.getAs[String]("COMMENTS")))
      .collect()
      .toMap
  }

  def writerToHive(saveMode: SaveMode): Unit ={
    import scala.collection.JavaConversions._
    if(extraOptions.size < 2)
      extraOptions ++= jdbcOptions

    val allColComments = getAllTableAnnotation()

    spark.sql(s"use $hiveDB")

    getAllTableName().select("table_name").collect().foreach(row => {
      //表名
      val table_name = row.getAs[String]("table_name")
      extraOptions.put("dbtable", table_name)
      //根据表名从Oracle取数
      val df = getDataFrameByJdbc(extraOptions)
      //字段名 和注 对应的map
      val colName_comments_map = getFiledAnnotationMap(allColComments, table_name)

      //需要调整字段类型的字段
      val colName = ArrayBuffer[String]()
      //为schema添加注释信息
      val schema = df.schema.map(s => {
        if (s.dataType.equals(DecimalType(38, 0)) || s.dataType.equals(DecimalType(2, 0))) {
          colName += s.name
          new StructField(s.name, IntegerType, s.nullable, s.metadata).withComment(colName_comments_map(s.name))
        } else {
          s.withComment(colName_comments_map(s.name))
        }
      })

      import org.apache.spark.sql.functions._
      var df_int = df
      colName.foreach(name => {
        df_int = df_int.withColumn(name, col(name).cast(IntegerType))
      })

      //根据添加了注释的schema，新建DataFrame
      val new_df = spark.createDataFrame(df_int.rdd, StructType(schema))
      new_df.write.mode(saveMode).saveAsTable(table_name)
    })

  }
}

object JdbcAllTableAnnotationToHive{
  def apply(spark: SparkSession, jdbcOptions: Properties, hiveDB: String): JdbcAllTableAnnotationToHive =
    new JdbcAllTableAnnotationToHive(spark, jdbcOptions, hiveDB)

  def main(args: Array[String]): Unit = {
    val build = SparkSession
      .builder()
      .master("local[2]")
      .appName("ReadOracle")
      .config("spark.sql.parquet.writeLegacyFormat", true)

    SparkSubmitUtil.setJobHistory(build)

    val spark = build.enableHiveSupport().getOrCreate()

    val property = PropertiesUtil.getProperties("spark-oracle.properties")

    val hiveDB = "bzk";

    apply(spark, property, hiveDB).writerToHive(SaveMode.Append)
  }
}
