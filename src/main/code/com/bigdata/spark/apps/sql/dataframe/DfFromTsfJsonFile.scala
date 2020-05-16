package com.bigdata.spark.apps.sql.dataframe

import com.bigdata.spark.apps.SparkUtil
import com.bigdata.spark.apps.cores.ClassPathUtil
import org.apache.spark.sql.Row

import scala.collection.mutable.ArrayBuffer

/**
 *
 * @author liulv 
 * @date 2020/4/8 17:22
 */
class DfFromTsfJsonFile {

}

//case class Person(id: Int, name: String, age: Int)

object DfFromTsfJsonFile {

  def main(args: Array[String]): Unit = {
    val spark = SparkUtil.getSpark("DataFrameJFormJsonFileTest")
    val peopleJsonPath = ClassPathUtil.getClassPathFile + "people.json"
    println("peopleJsonPath: " + peopleJsonPath)
    val people = spark.read.json(peopleJsonPath)
    import spark.implicits._
    people.printSchema()
    //	root
    //    |-- id: Integer (nullable	=	true)
    //		|--	age:	integer	(nullable	=	true)
    //		|--	name:	string	(nullable	=	true)
    val fields = people.schema.names

    val listBuffer = new ArrayBuffer[AnyRef]()
    val peopleRDD = people.rdd.map(row => {
      for (field <- fields) {
        val colv = row.getAs[AnyRef](field)

        //需要脱敏字段
        if(field.equals("name")){
          listBuffer.append(colv  + "**************")
        }
        listBuffer.append(colv)
      }
      Row.fromSeq(listBuffer)
    })

    spark.createDataFrame(peopleRDD, people.schema).toDF(fields: _*).show()
  }


}
