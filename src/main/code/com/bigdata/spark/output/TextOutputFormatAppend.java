package com.bigdata.spark.output;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.ipc.RemoteException;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author liulv
 * @date 2020/2/7
 * @time 1:33
 * <p>
 * 说明： 定制版TextOutPutFormat,用于多次输出进行追加写
 * 并打印文件输出目录，文件输出前大小和输出后大小
 *
 * 可能BUG：多并发多线程同时追加写同一个文件问题
 *
 */
public class TextOutputFormatAppend<K, V> extends TextOutputFormat<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(TextOutputFormatAppend.class.getSimpleName());

    /**
     * 仅仅只是用于全局在写入后获取文件大小打印
     */
    private static Path filePath;
    /**
     * 仅仅只是用于全局在写入后获取文件大小打印
     */
    private static FileSystem fss;

    /**
     * 定制RecordWriter内部类, 在重写getRecordWriter方法时调用
     *
     * @param <K> 输出K
     * @param <V> 输出V
     */
    protected static class LineRecordWriterAppend<K, V> implements RecordWriter<K, V> {
        private static final String utf8 = "UTF-8";
        private static final byte[] newline;

        static {
            try {
                newline = "\n".getBytes(utf8);
            } catch (UnsupportedEncodingException uee) {
                throw new IllegalArgumentException("can't find " + utf8 + " encoding");
            }
        }

        protected DataOutputStream out;
        private final byte[] keyValueSeparator;

        public LineRecordWriterAppend(DataOutputStream out, String keyValueSeparator) {
            this.out = out;
            try {
                this.keyValueSeparator = keyValueSeparator.getBytes(utf8);
            } catch (UnsupportedEncodingException uee) {
                throw new IllegalArgumentException("can't find " + utf8 + " encoding");
            }
        }

        public LineRecordWriterAppend(DataOutputStream out) {
            this(out, "\t");
        }

        /**
         * Write the object to the byte stream, handling Text as a special
         * case.
         *
         * @param o the object to print
         * @throws IOException if the write throws, we pass it on
         */
        private void writeObject(Object o) throws IOException {
            if (o instanceof Text) {
                Text to = (Text) o;
                out.write(to.getBytes(), 0, to.getLength());
            } else {
                out.write(o.toString().getBytes(utf8));
            }
        }

        public synchronized void write(K key, V value) throws IOException {
            boolean nullKey = key == null || key instanceof NullWritable;
            boolean nullValue = value == null || value instanceof NullWritable;
            if (nullKey && nullValue) {
                return;
            }
            if (!nullKey) {
                writeObject(key);
            }
            if (!(nullKey || nullValue)) {
                out.write(keyValueSeparator);
            }
            if (!nullValue) {
                writeObject(value);
            }
            out.write(newline);
        }

        @Override
        public synchronized void close(Reporter reporter) throws IOException {
            out.flush();
            out.close();
            //必须在close后才能获取写入后文件大小
            long fileSize = fss.listFiles(filePath, false).next().getLen();
            System.out.println("[" + Thread.currentThread().getId() + "] 写入后文件大小:" + FileUtil.convertFileSize(fileSize));
        }
    }

    /**
     * 从写getRecordWriter方法
     *
     * @param ignored 文件系统FileSystem
     * @param job Hadoop JobConf
     * @param name 文件名
     * @param progress 文件系统创建输出流报告进展
     * @return RecordWriter 写入作业的输出
     * @throws IOException 抛出IO异常
     */
    @Override
    public RecordWriter<K, V> getRecordWriter(FileSystem ignored,
                                              JobConf job,
                                              String name,
                                              Progressable progress)
            throws IOException {
        boolean isCompressed = getCompressOutput(job);
        String keyValueSeparator = job.get("mapreduce.output.textoutputformat.separator",
                "\t");
        if (!isCompressed) {
            Path taskFile = FileOutputFormat.getTaskOutputPath(job, name);
            //FileSystem fs = file.getFileSystem(job);
            Path outputPath = getOutputPath(job);
            FileSystem fs = taskFile.getFileSystem(job);
            fss = fs;
            Path file = new Path(outputPath, name);
            filePath = file;
            DistributedFileSystem dfs = (DistributedFileSystem) DistributedFileSystem.get(fs.getConf());
            FSDataOutputStream fileOut = null;

            if(!fs.exists(file)){
                fileOut = fs.create(file);
                System.out.println("[" + Thread.currentThread().getId() + "] 文件名 : " + filePath + ", 写入前文件大小:"
                        + FileUtil.convertFileSize(0));
            }else {
                long fileSize = fs.listFiles(file, false).next().getLen();
                System.out.println("[" + Thread.currentThread().getId() + "] 文件名 : " + filePath + ", 写入前文件大小:"
                        + FileUtil.convertFileSize(fileSize));
                //HDFS租约处理 ,如果写入的文件租约未释放，无限循环等待睡眠500毫秒在写入和追加写
                boolean isLease = true;
                int i = 0;
                while(fileOut == null || isLease){
                    i++;
                    try {
                        fileOut = fs.append(file);
                        isLease = false;
                    }catch (RemoteException e ){
                        //LOG.warn(e.getMessage());
                        try {
                            Thread.sleep(500);
                            if(i == 10)
                                dfs.recoverLease(file);
                        } catch (Exception ex) {
                            LOG.warn(ex.getMessage());
                        }
                        isLease = true;
                    }
                }
            }


            return new LineRecordWriterAppend<K, V>(fileOut, keyValueSeparator);
        } else {
            Class<? extends CompressionCodec> codecClass =
                    getOutputCompressorClass(job, GzipCodec.class);
            // create the named codec
            CompressionCodec codec = ReflectionUtils.newInstance(codecClass, job);
            // build the filename including the extension
            //Path file = FileOutputFormat.getTaskOutputPath(job, name + codec.getDefaultExtension());
            //FileSystem fs = file.getFileSystem(job);
            if(!name.contains(codec.getDefaultExtension())) name =  name + codec.getDefaultExtension();
            Path taskFile = FileOutputFormat.getTaskOutputPath(job, name);
            FileSystem fs = taskFile.getFileSystem(job);
            fss = fs;
            Path outputPath = getOutputPath(job);
            Path file = new Path(outputPath, name);
            if(!fs.exists(file)) fs.create(file);
            long fileSize = fs.listFiles(file, false).next().getLen();
            System.out.println("============================================");
            filePath = file;
            System.out.println("文件名[压缩] : " + file.toString() + ", 写入前文件大小:" + FileUtil.convertFileSize(fileSize));
            DistributedFileSystem dfs = (DistributedFileSystem) DistributedFileSystem.get(fs.getConf());
            dfs.recoverLease(file);
            FSDataOutputStream fileOut = fs.append(file);
            //FSDataOutputStream fileOut = fs.create(file);
            return new LineRecordWriterAppend<K, V>(new DataOutputStream
                    (codec.createOutputStream(fileOut)),
                    keyValueSeparator);
        }
    }
}
