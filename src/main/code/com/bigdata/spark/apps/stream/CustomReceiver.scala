package com.bigdata.spark.apps.stream

import java.io.{BufferedReader, InputStreamReader}
import java.net.{ConnectException, Socket}
import java.nio.charset.StandardCharsets

import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.receiver.Receiver

/**
  * 通过套接字接收数据的自定义接收器。接收到的字节被解释为
  * 文本和\n分隔的行被视为记录。然后他们被计数和打印。
  *
  * 运行前首先运行一个Netcat服务器
  *   Linux : ` nc -lk 9999`
  *   windows先下载安装，将E:\Tools\netcat\nc11nt.rar中的nc.ext放到c:\windows下，然后在运行
  *   nc -l -p 9999
  *
  * @author liulv 
  * @date 2020/2/8 5:33
  */
class CustomReceiver(host:String, port: Int) extends Receiver[String](StorageLevel.MEMORY_AND_DISK_2){
  override def onStart(): Unit ={
    //Start the thread that receiver date over a connection
    new Thread("Socket Receiver"){
      override def run(): Unit ={
        receive()
      }
    }.start()
  }

  override def onStop(): Unit = {

  }

  /**
    * Create a socket connection and receive data until receiver is stoped
    */
  private def receive(): Unit ={
      var socket: Socket = null
    var userInput: String = null
    println(s"Connecting to $host : $port")
    try{
      socket = new Socket(host, port)
      println(s"Connected to $host : $port")

      val reader = new BufferedReader(new InputStreamReader(socket.getInputStream,  StandardCharsets.UTF_8))
      userInput = reader.readLine()
      while (!isStopped() && userInput != null){
        store(userInput)
        userInput = reader.readLine()
      }
      reader.close()
      socket.close()
      println("Stopped receiving")
      println("Trying to connect again")
    } catch {
      case e: ConnectException =>
        restart(s"Error Connecting to $host : $port")
      case t: Throwable =>
        restart("Error receiving data", t)
    }
  }
}

object CustomReceiver{
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Custom Receiver").setMaster("local[2]")
    val ssc = new StreamingContext(conf, Seconds(2))

    val host = "localhost"
    val port = 9999
    val lines = ssc.receiverStream(new CustomReceiver(host, port))
    val linesStr = lines.print()
    lines.flatMap(_.split(" ")).map(x => (x, 1)).reduceByKey(_ + _).print()

    ssc.start()
    ssc.awaitTermination()
  }
}
