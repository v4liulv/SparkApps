package com.bigdata.spark.apps.sql.hive

import java.net.URLDecoder

import com.bigdata.spark.apps.SparkUtil
import com.bigdata.spark.apps.sjzl.SjzljcGz
import com.bigdata.spark.apps.submit.SparkSubmitUtil
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * @author liulv 
  * @date 2019/8/20
  * @time 21:33
  * @description 数据质量-通过sparkSQL实现，本地模式运行，前提SjzlTestTable.mian()创建测试表
  */
//noinspection ScalaDocUnknownTag
object SjzlSqlFromSpark {

  def main(args: Array[String]): Unit = {

    val spark = SparkUtil.sparkInitLocal("SjzlSparkSqlTest")

    SparkSubmitUtil.setJobHistory(spark)

    val sqlDF = sjzlDf(spark)

    //空值验证
    sjzlIsNull(sqlDF)

    spark.stop()
  }

  //获取数据质量DF
  def sjzlDf(spark: SparkSession) : DataFrame = {
    import spark.implicits._
    import spark.sql
    sql("SELECT rksj,wybs,xm,zjhm,rylbdm,gjdqdm FROM ODS_CRJ_CRJJL_TEXT")
  }

  //根据DF进行数据质量-空值验证
  def sjzlIsNull(sqlDF : DataFrame): Unit = {
    //检测字段
    val filedName = "rksj"
    //数据质量类别：1代表空值检测，正式环境通过配置获取
    val sjzlType = "1"
    //数据质量-检测值, 正式环境通过配置获取
    val sjzlGzz = "1"
    val sjzljcXM = sqlDF.filter(lines =>
      lines.getAs(filedName) == null
        ||
        SjzljcGz.sjjc(sjzlType, sjzlGzz, lines.getAs(filedName).toString)
    ).count()

    println("空值验证：")
    println(sjzljcXM)
  }

}
