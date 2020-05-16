package com.bigdata.spark.apps.cores

import java.net.URLDecoder

/**
  * @author liulv 
  * @date 2019/8/21
  * @time 4:48
  *
  *       公共的工具类:
  *       提供getClassPath和getClassPathFile两个静态方法
  */
class ClassPathUtil {

}

/**
  * 类的伴生对象：
  * 实现静态类和静态方法
  */
object ClassPathUtil {
  /**
    * 获取classpath路径
    *
    * @return 返回classpath路径
    */
  def getClassPath: String = {
    var classPath = Thread.currentThread.getContextClassLoader.getResource("").getPath
    //classPath = classPath.substring(0, classPath.length - 8)
    classPath = URLDecoder.decode(classPath, "UTF-8")

    classPath
  }

  /**
    * 获取classpath路径下classes目录
    *
    * @return
    */
  def getClassesPath: String = {
    getClassPath + "classes/"
  }

  /**
    * file: 模式下的classpath路径
    *
    * @return 返回 file: classpath
    */
  def getClassPathFile: String = {
    "file://" + getClassPath
  }

  /**
    * 获取classpath路径下classes目录
    *
    * @return
    */
  def getClassesPathFile: String = {
    getClassPathFile + "classes/"
  }

  def main(args: Array[String]): Unit = {
    println("getClassPath : \n" + getClassPath)
    println("getClassesPath : \n" + getClassesPath)
    println("getClassPathFile : \n" + getClassPathFile)
    println("getClassesPathFile : \n" + getClassesPathFile)
  }

}
