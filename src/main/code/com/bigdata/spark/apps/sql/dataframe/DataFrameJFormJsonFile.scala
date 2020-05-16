package com.bigdata.spark.apps.sql.dataframe

import com.bigdata.spark.apps.SparkUtil
import com.bigdata.spark.apps.cores.ClassPathUtil


/**
  * Created by Administrator on 2016/11/22 0022.
  *
  * Spark	SQL能够自动推断JSON数据集的模式，加载它为一个Schema RDD
  * json File	：从一个包含JSON文件的目录中加载。文件中的每一行是一个JSON对象
  */
object DataFrameJFormJsonFile {

  def main(args: Array[String]): Unit = {

    val spark = SparkUtil.getSpark("DataFrameJFormJsonFileTest")

    val peopleJsonPath = ClassPathUtil.getClassPathFile + "people.json"

    println("peopleJsonPath: " + peopleJsonPath)
    val people = spark.read.json(peopleJsonPath)
    people.printSchema()
    //	root
    //		|--	age:	integer	(nullable	=	true)
    //		|--	name:	string	(nullable	=	true)
    people.show()

    people.createOrReplaceTempView("people")

    import spark.sql
    import spark.implicits._
    sql("SELECT * FROM people WHERE age >=13 AND age <=19").show()

    //json RDD
    //	Alternatively,	a	Schema RDD	can	be	created	for	a	JSON	dataset	represented	by
    //	an	RDD[String]	storing	one	JSON	object	per	string.
    val anotherPeopleRDD = spark.sparkContext.parallelize(
      """{"name":"Yin","address":{"city":"Columbus","state":"Ohio"}}""" :: Nil)
    val anotherPeople = spark.read.json(anotherPeopleRDD)
    anotherPeople.createOrReplaceTempView("autoPeople")
    sql("select * from autoPeople")

    spark.stop()
  }

}
