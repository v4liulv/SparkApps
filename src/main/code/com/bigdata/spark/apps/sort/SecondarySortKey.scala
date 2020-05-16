package com.bigdata.spark.apps.sort

/**
  * Created by liulv on 2017/5/16.
  *
  * 二次排序将其中的两个数据(first, second)作为key
  */
class SecondarySortKey(val first: Int, val second: Int) extends Ordered[SecondarySortKey] with Serializable {
  override def compare(other: SecondarySortKey): Int = {
      if((this.first - other.first)!=0){
        this.first - other.first
      }else{
        this.second - other.second
      }
  }
}
