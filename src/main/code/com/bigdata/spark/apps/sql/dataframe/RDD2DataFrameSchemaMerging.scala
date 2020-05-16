package com.bigdata.spark.apps.sql.dataframe

/**
  * Created by Administrator on 2016/11/22 0022.
  * Schema Merging
  * Since schema merging is a relatively expensive operation, and is not a necessity in most cases,
  * we turned it off by default starting from 1.5.0. You may enable it by
  *
  */
object RDD2DataFrameSchemaMerging {

  def main(args: Array[String]): Unit = {

   /* val conf = new SparkConf().setMaster("local").setAppName("RDD2DataFrameSchemaMerging");
    val sc = new SparkContext(conf)
    // Create a simple DataFrame, store into a partition directory
    val squaresDF = sc.makeRDD(1 to 5).map(i => (i,i * i)).toDF("value", "square")
    squaresDF.write.parquet("data/test_table/key=1")

    // Create another DataFrame in a new partition directory,
    // adding a new column and dropping an existing column
    val cubesDF = sc.makeRDD(6 to 10).map(i => (i, i * i * i)).toDF("value", "cube")
    cubesDF.write.parquet("data/test_table/key=2")

    / Read the partitioned table
    val mergedDF = sc.option("mergeSchema", "true").parquet("data/test_table")
    mergedDF.printSchema()*/

  }

}
