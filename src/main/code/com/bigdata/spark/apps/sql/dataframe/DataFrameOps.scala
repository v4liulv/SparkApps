package com.bigdata.spark.apps.sql.dataframe

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by liulv on 2016/11/21 0021.
  *
  * 集群模式 Spark中DataFrame
  */
object DataFrameOps {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
    conf.setAppName("DataFrameOps")
    conf.setMaster("spark://hadoop01:7077")
    val dc = new SparkContext(conf)

    val sqlContext = new HiveContext(dc)
    /** 读取hive中的表创建dataframe **/
    val df = sqlContext.read.table("wyp1")

    /** select * from table **/
    df.show()
    /** desc table **/
    df.printSchema()
    /** select name from table **/
    df.select("name").show()
    /** select name , age + 10 from table**/
    df.select(df.col("name"), df.col("age") + 10).show()
    /** select * from table where age > 20 **/
    df.filter(df.col("age").gt(20)).show()
     /** select count(1) from table group by age **/
    df.groupBy(df.col("age")).count().show()

  }

}
