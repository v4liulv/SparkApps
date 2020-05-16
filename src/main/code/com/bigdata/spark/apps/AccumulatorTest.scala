package com.bigdata.spark.apps

import java.lang

import com.bigdata.spark.apps.submit.SparkSubmitUtil
import org.apache.spark.util.{AccumulatorV2, LongAccumulator}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author liulv 
  * @date 2020/2/6
  * @time 14:59
  *
  *       说明：广播器-累加器的使用，通过累加器实现行数统计
  *
  *  代码示例使用的是 Spark 内置的 Long 类型的累加器，程序员可以通过继承 AccumulatorV2 类创建新的累加器类型。
  *  AccumulatorV2抽象类有几个需要 override（重写）的方法 : reset 方法可将累加器重置为 0，add 方法可将其它值添加到累加器中，
  *  merge 方法可将其他同样类型的累加器合并为一个。
  */
class AccumulatorTest {

}

/**
 *
 * @author liulv
 * @date 2020/2/8 1:20
 */
object AccumulatorTest {

  def accumLineCount(): Unit ={
    val sparkConf = new SparkConf().setMaster("local").setAppName("Accumulator Line Count")
    SparkSubmitUtil.setJobHistory(sparkConf)
    val sc = new SparkContext(sparkConf)

    val accumLineCount: LongAccumulator = sc.longAccumulator("My LongAccumulator")

    // 创建自定义累加器
    val myVectorAcc = new IntAccumulator
    myVectorAcc.setValue(10)
    // 自定义累加器注册到SparkContext
    sc.register(myVectorAcc, "MyVectorAcc1")

    val parallelCollectionRDD = sc.parallelize(Array(1,2,3,4))

    parallelCollectionRDD.foreach(_ => accumLineCount.add(1))
    parallelCollectionRDD.foreach(_ => myVectorAcc.add(1))

    println(accumLineCount.value)
    println(myVectorAcc.value)
  }

  def main(args: Array[String]): Unit = {
    accumLineCount()
  }

}

import java.{lang => jl}
class IntAccumulator extends AccumulatorV2[jl.Integer, jl.Integer] {
  private var _sum = 0
  private var _count = 0

  def count: Int = _count

  def sum: Int = _sum

  def avg: Double = _sum.toDouble / _count

  override def isZero: Boolean = _sum == 0L && _count == 0

  override def copy(): IntAccumulator = {
    val newAcc = new IntAccumulator
    newAcc._count = this._count
    newAcc._sum = this._sum
    newAcc
  }

  override def reset(): Unit = {
    _sum = 0
    _count = 0
  }

  override def add(v: jl.Integer): Unit = {
    _sum += v
    _count += 1
  }

  def add(v: Int): Unit = {
    _sum += v
    _count += 1
  }

  override def merge(other: AccumulatorV2[jl.Integer, jl.Integer]): Unit = other match {
    case o: IntAccumulator =>
      _sum += o.sum
      _count += o.count
    case _ =>

  }

  def setValue(newValue: Int): Unit = _sum = newValue

  override def value: jl.Integer = _sum
}
