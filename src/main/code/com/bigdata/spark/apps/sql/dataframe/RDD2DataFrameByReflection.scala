package com.bigdata.spark.apps.sql.dataframe

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  *  Created by Administrator on 2016/11/21 0021.
  *
  * 使用反射的机制将RDD转换为DataFrame
  * 使用JavaBean和Scala中的CaseClass,这是我们知道RDD的元数据结构下
  * 但是如果不知道RDD的元数据的话，就无法使用使用反射机制转换DataFrame
  */
object RDD2DataFrameByReflection {

  /**
    * case class相当于Java中的JavaBean
    *
    * @param id  Entity Person attributes person id
    * @param name  Entity Person attributes person name
    * @param age  Entity Person attributes person age
    */
  case class Person(id: Int, name: String, age: Int)

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("RDDRelationTest").setMaster("local")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    // Importing the SQL context gives access to all the SQL functions and implicit conversions.
    import sqlContext.implicits._

    // Reflection进行相关的转换DataFrame
    val df = sc.textFile("E://persions.txt")
      .map(_.split(","))
      .map(attributes => Person(attributes(0).trim.toInt, attributes(1), attributes(2).trim.toInt))
      .toDF()
    // Any RDD containing case classes can be registered as a table.  The schema of the table is
    // automatically inferred using scala reflection.
    df.createOrReplaceTempView("persons")

    // Once tables have been registered, you can run SQL queries over them.
    println("Result of SELECT *:")
    sqlContext.sql("SELECT * FROM persons").collect().foreach(println)

    // Aggregation queries are also supported.
    val count = sqlContext.sql("SELECT COUNT(*) FROM persons").collect().head.getLong(0)
    println(s"COUNT(*): $count")

    // The results of SQL queries are themselves RDDs and support all normal RDD functions.  The
    // items in the RDD are of type Row, which allows you to access each column by ordinal.
    val rddFromSql = sqlContext.sql("SELECT id, name, age  FROM persons WHERE age < 10")

    println("Result of RDD.map:")
    rddFromSql.map(row => s"name: ${row(1)}, age: ${row(2)}").collect().foreach(println)

    // Queries can also be written using a LINQ-like Scala DSL.
    df.where($"id" === 1).orderBy($"age".asc).select($"id").collect().foreach(println)

    // Write out an RDD as a parquet file.
    //df.write.parquet("pair.parquet")

    // Read in parquet file.  Parquet files are self-describing so the schmema is preserved.
    val parquetFile = sqlContext.read.parquet("pair.parquet")

    println("key === 1:  ")
    // Queries can be run using the DSL on parequet files just like the original RDD.
    parquetFile.where($"id" === 1).select($"name".as("a")).collect().foreach(println)

    // These files can also be registered as tables.
    println("parquetFile select:    ")
    parquetFile.createOrReplaceTempView("parquetFile")
    sqlContext.sql("SELECT * FROM parquetFile").collect().foreach(println)

    sc.stop()
  }

}
