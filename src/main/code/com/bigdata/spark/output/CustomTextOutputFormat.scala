package com.bigdata.spark.output

import java.io.IOException

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.compress.{CompressionCodec, GzipCodec}
import org.apache.hadoop.mapred.lib.MultipleTextOutputFormat
import org.apache.hadoop.mapred.{FileOutputFormat, JobConf, RecordWriter}
import org.apache.hadoop.util.{Progressable, ReflectionUtils}

/**
  * @author liulv
  * @date 2020/2/7
  * @time 1:33
  *       <p>
  *       说明：定制的outputFormat，使用方式rdd.saveAsHadoopFile(.., .., classOf[CustomOutputFormat], ... )
  *
  *       实现功能：
  * 1. 通过自定义输出文件名，通过当前时间搓命名文件名
  * 2. 去掉原始的输出目录存在报错
  * 3. 多次流输入在多个RDD输出情况，追加写数据，结合定制的TextOutPutFormatAppend使用
  * 4. 输出文件大小控制，大于最大大小值新文件输出
  *
  */
class CustomTextOutputFormat extends MultipleTextOutputFormat[Any, Any] {

  /**
    * 使用定制的TextOutputFormatAppend
    * 这里为什么不自己定制此方法呢，因为在TextOutputFormatAppend定制了，那么这里共用就不可以了
    * 代码共用
    */
  private var theTextOutputFormat: TextOutputFormatAppend[Any, Any] = _

  /**
    * 使用定制的TextOutputFormatAppend.getRecordWriter方法
    * 这里为什么不自己定制此方法呢，因为在TextOutputFormatAppend定制了，那么这里共用就可以了
    * 代码共用
    */
  @throws[IOException]
  override protected def getBaseRecordWriter(fs: FileSystem, job: JobConf, name: String, progressbar: Progressable): RecordWriter[Any, Any] = {
    //使用定制的TextOutputFormatAppend的getRecordWriter
    if (theTextOutputFormat == null) theTextOutputFormat = new TextOutputFormatAppend[Any, Any]
    theTextOutputFormat.getRecordWriter(fs, job, name, progressbar)
  }

  //重写generateFileNameForKeyValue方法，该方法是负责自定义生成文件的文件名
  override def generateFileNameForKeyValue(key: Any, value: Any, xtname: String): String = {
    //这里的key和value指的就是要写入文件的rdd对，再此，我定义文件名以key.txt来命名，当然也可以根据其他的需求来进行生成文件名
    //val fileName = key.asInstanceOf[String] + ".txt"

    var nameSb: StringBuffer = new StringBuffer()

    //启动压缩处理
    val isCompressed = FileOutputFormat.getCompressOutput(CustomTextOutputFormat.jobConf)
    var codecClass: Class[_ <: CompressionCodec] = null
    var compressFileDefaultExtension: String = null
    if (isCompressed) {
      codecClass = FileOutputFormat.getOutputCompressorClass(CustomTextOutputFormat.jobConf, classOf[GzipCodec])
      compressFileDefaultExtension = ReflectionUtils.newInstance(codecClass, CustomTextOutputFormat.jobConf).getDefaultExtension
    }

    //@throws java.lang.NullPointerException
    val outputPath = FileOutputFormat.getOutputPath(CustomTextOutputFormat.jobConf)
    val fs = outputPath.getFileSystem(CustomTextOutputFormat.jobConf)

    val outputPathListFiles = fs.listFiles(outputPath, true)
    import util.control.Breaks._
    breakable {
      while (outputPathListFiles.hasNext) {
        val ffl = outputPathListFiles.next()
        //如果输出目录下存在文件小于maxSize，则使用此文件作为输出文件
        if (ffl.isFile && ffl.getLen < CustomTextOutputFormat.maxFileSize) {
          val oldFile = ffl.getPath.getName
          if (!isCompressed) {
            if (!oldFile.endsWith(".bz2")
              && !oldFile.endsWith(".gz")
              && !oldFile.endsWith(".snappy")
              && !oldFile.endsWith(".lz4")
              && !oldFile.endsWith(".lzo")
              && oldFile.endsWith(CustomTextOutputFormat.fileSuffix)) {
              nameSb.append(oldFile)
              //println("写入历史文件： " + nameSb.toString)
              break
            }
          } else if (oldFile.endsWith(CustomTextOutputFormat.fileSuffix + compressFileDefaultExtension)) {
            nameSb.append(oldFile)
            //println("写入历史文件： " + nameSb.toString)
            break
          }
        }
      }
    }

    //新增文件情况
    if (nameSb.length() == 0) {
      nameSb.append(System.currentTimeMillis().toString).append(CustomTextOutputFormat.fileSuffix)
      if (isCompressed) {
        nameSb.append(compressFileDefaultExtension)
      }
    }

    val file = new Path(outputPath, nameSb.toString)
    if (!fs.exists(file)) fs.create(file)
    val fileSize = fs.listFiles(file, false).next.getLen

    //文件超过一定大小，写入新文件
    if (fileSize > CustomTextOutputFormat.maxFileSize){
      nameSb.setLength(0)
      nameSb.append(System.currentTimeMillis().toString).append(CustomTextOutputFormat.fileSuffix)
    }
    nameSb.toString
  }

  /** 因为saveAsHadoopFile是以key,value的形式保存文件，写入文件之后的内容也是，
    * 按照key value的形式写入，k,v之间用空格隔开，这里我只需要写入value的值，不需要将key的值写入到文件中，
    * 所以我需要重写 该方法，让输入到文件中的key为空即可，当然也可以进行领过的变通，
    * 也可以重写generateActuralValue(key:Any,value:Any),根据自己的需求来实现
    * */
  override def generateActualKey(key: Any, value: Any): Null = null

  //对生成的value进行转换为字符串，当然源码中默认也是直接返回value值，如果对value没有特殊处理的话，不需要重写该方法
  // override def generateAcutalValue(key: Any, value: Any): String = {
  // return value.asInstance[String]
  // }

  //该方法使用来检查我们输出的文件目录是否存在，源码中，是这样判断的，如果写入的父目录已经存在的话，则抛出异常
  // 在这里我们冲写这个方法，修改文件目录的判断方式，如果传入的文件写入目录已存在的话，直接将其设置为输出目录即可不会抛出异常   */
  override def checkOutputSpecs(ignored: FileSystem, job: JobConf): Unit = {
    val outDir = FileOutputFormat.getOutputPath(job)
    if (outDir != null) {
      //注意下面的这两句，如果说你要是写入文件的路径是hdfs的话，下面的两句不要写，或是注释掉，
      // 它俩的作用是标准化文件输出目录，根据我的理解是，他们是标准化本地路径，写入本地的话，可以加上，
      // 本地路径记得要用file:///开头，比如file:///E:/a.txt
      //val fs: FileSystem = ignored
      //outDir = fs.makeQualified(outDir)
      FileOutputFormat.setOutputPath(job, outDir)
    }
  }
}

object CustomTextOutputFormat {
  /**
    * Hadoop JobConf用于获取HDFS环境相关配置
    */
  private var jobConf: JobConf = _

  /**
    * 最大文件大小单位为byte用于分文件存储，如果输出目录下存在文件大小小于此值，那么写入时候会追加写入这些文件下，如果都大于此值，
    * 那么会新建一个通过时间搓命名的文件进行写入，默认为510M
    */
  private var maxFileSize: Long = 1024*1024*510

  private var fileSuffix: String = ".txt"

  /**
    * Spark环境中通过hadoopConfiguration创建Jobconf进行设置，
    * 在saveAsHadoopFile前调用设置，并且在saveAsHadoop中传递参数也用同一个JobConf，例如:
    *
    * val hadoopConf = sc.hadoopConfiguration
    * val jobConf = new JobConf(hadoopConf)
    * CustomOutputFormat.setJobConf(jobConf)
    * ..
    * RDD.saveAsHadoopFile(...., jobConf)
    *
    * @param conf JobConf
    */
  def setJobConf(conf: JobConf): Unit = {
    this.jobConf = conf
  }

  /**
    * 设置最大文件大小，用于控制输出文件大小控制
    * 在rdd.saveAsHadoopFile方法前调用
    *
    * @param maxFileSize Long:最大文件大小单位为byte
    */
  def setMaxFileSize(maxFileSize: Long): Unit = {
    this.maxFileSize = maxFileSize
  }

  def setFileSuffix(fileSuffix: String): Unit = {
    this.fileSuffix = fileSuffix
  }
}