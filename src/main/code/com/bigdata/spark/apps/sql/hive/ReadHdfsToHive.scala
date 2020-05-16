package com.bigdata.spark.apps.sql.hive

import com.bigdata.spark.apps.SparkUtil
import org.apache.spark.sql.{Row, SaveMode}
import org.apache.spark.sql.types.{DataType, DataTypes, DateType, StructField, StructType}

import scala.collection.mutable.ArrayBuffer



class ReadHdfsToHive {

}

/**
 * @author liulv
 */
object ReadHdfsToHive {
  //val fieldNames = Array.apply("id", "name", "age", "rksj")

  private val oracleSchema = Map("id" -> "varchar2", "name" -> "varchar2", "age" -> "number", "rksj" -> "varchar")

  def main(args: Array[String]): Unit = {
    val spark = SparkUtil.getSpark("ReadHdfsToHive", true)

    // 设置RDD的partition的数量一般以集群分配给应用的CPU核数的整数倍为宜。
    val minPartitions = 2;
    val personRDD = spark.sparkContext.textFile("/apps/tmp/", minPartitions)
    personRDD.collect().foreach(println)

    val person = personRDD
      //是否去掉首行
      //.mapPartitionsWithIndex { (idx, iter)=> if (idx == 0) iter.drop(1) else iter }
      .map(_.split(","))
      .map(row => {
        val listBuffer = new ArrayBuffer[Any]()
        var i = 0;
        for(filedType <- oracleSchema.values){
          listBuffer.append(getDate(filedType, row(i)))
          i = i + 1;
        }
        Row.fromSeq(listBuffer)
      })
    person.collect().foreach(println)

    spark.sql("USE tencent")
    val personDF = spark.createDataFrame(person, createStructType()).toDF(oracleSchema.keys.toArray: _*)
    personDF.show()

    personDF.createOrReplaceTempView("personV")

    val results = spark.sql("SELECT * FROM personV")

    results.write.format("orc").mode(SaveMode.Append).saveAsTable("person")
  }

  //	root
  //    |-- id: Integer (nullable	=	true)
  //		|--	age:	integer	(nullable	=	true)
  //		|--	name:	string	(nullable	=	true)
  def createStructType(): StructType = {
    val fields = new ArrayBuffer[StructField]()
    for ((filedName, filedType) <- oracleSchema){
      fields += DataTypes.createStructField(filedName, filedTypeTS(filedType), true)
    }
    DataTypes.createStructType(fields.toArray)
  }

  /**
   * RDBMS数据库数据类型转换为Hive需要的DataType
   *
   * @param dataTypeStr RDBMS数据库数据类型
   * @return
   */
  def filedTypeTS(dataTypeStr: String): DataType = {
    getDataType(dataTypeStr)
  }

  /**
   * 通过RDBMS数据库数据类型值转换为Hive的DataType
   *
   * @param dateType RDBMS数据库数据类型
   * @return Hive需要构建的数据类型DataType，如DataTypes.IntegerType、DataTypes.StringType
   */
  def getDataType(dateType: String): DataType = {
    dateType match {
      case  "number" => DataTypes.IntegerType
      case  "number(9)" => DataTypes.IntegerType
      case "String" => DataTypes.StringType
      case _ => DataTypes.StringType
    }
  }

  /**
   * 将读取到的数据转换为Hive需要数据,比如类型为Int或String、Double、Date等..
   *
   * @param dateType RDBMS数据库数据类型
   * @param data 读取到数据
   * @return data hive的数据
   */
  def getDate(dateType: String, data: String): Any = {
    dateType match {
      case  "number" => data.mkString.toInt
      case  "number(9)" => data.mkString.toInt
      case "varchar" => data.mkString
      case "varchar2" => data.mkString
      case _ => data.mkString
    }
  }

}
