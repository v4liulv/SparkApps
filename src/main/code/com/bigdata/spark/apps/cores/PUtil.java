package com.bigdata.spark.apps.cores;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;

/**
 * @author liulv
 * @date 2020/3/15 22:37
 */
public class PUtil {
    public static String CLASS_PATH = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();

    public static String getDataPath() throws UnsupportedEncodingException {
        CLASS_PATH = URLDecoder.decode(CLASS_PATH, "utf-8");
        CLASS_PATH = CLASS_PATH.substring(CLASS_PATH.indexOf("/") + 1);
        int i = 0;
        while (CLASS_PATH.contains("/")) {
            CLASS_PATH = CLASS_PATH.substring(0, CLASS_PATH.lastIndexOf("/"));
            if (i == 2) {
                break;
            }
            i++;
        }
        return "file:///" + CLASS_PATH + "/data";
    }

}
