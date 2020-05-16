package com.bigdata.spark.output;

/**
 * @author liulv
 * @date 2020/2/7
 * @time 14:25
 *
 * 文件操作相关工具类，继承org.org.apache.hadoop.fs.FileUtil全部的静态方法
 */
public class FileUtil extends org.apache.hadoop.fs.FileUtil {

    /**
     * 转换文件大小，转换为带单位大小格式
     *
     * @author liulv 
     * @date 2020/2/8 1:04
     * @param size File Size 单位Byte
     * @return java.lang.String 返回格式化后大小格式字符串
     */
    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else return String.format("%d B", size);
    }

    public static void main(String[] args) {
        long fileSize = 1024;
        System.out.println(FileUtil.convertFileSize(fileSize));
    }
}
