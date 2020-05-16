package com.bigdata.spark.apps;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * xml配置文件工具类
 *
 * @author liulv
 * @date 2020/2/8 1:43
 */
public class PropertiesSiteUtil {
    /**
     * CONFIG_PATH 配置文件目录
     */
    private static String CONFIG_PATH = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().
            getResource("")).getPath() + "/";

    /**
     * 获取相关配置文件的参数
     * @param fileName
     * @param taskName 标识id
     *
     * @return Map<Key, Value>
     */
    public static Map<String, String> getValues(String fileName, String taskName){
        if( "".equals(fileName) || "".equals(taskName) )return null;
        System.out.println(">>>>>>>>>>>>>>>>>confFileName:"+ fileName);
        InputStream is;
        String configFilePath = CONFIG_PATH + fileName;
        SAXReader reader = new SAXReader();
        Map<String, String> resMap = new HashMap<>();
        try {
            configFilePath = URLDecoder.decode(configFilePath, "UTF-8");
            is = new BufferedInputStream(new FileInputStream(new File(configFilePath)));
            Document doc = reader.read(is);
            //common element
            Element commonElement = (Element)doc.selectSingleNode("configuration/commonConfig");
            if(commonElement!=null){
                List<Element> propertyElements = commonElement.elements("property");
                if(propertyElements!=null && propertyElements.size()>0){
                    for(Element property : propertyElements){
                        String key = property.element("name").getText();
                        String value = property.element("value").getText();
                        resMap.put(key, value);
                    }
                }
            }
            //task element
            Element taskElement = (Element)doc.selectSingleNode("configuration/task[@name=\""+taskName+"\"]");
            if(taskElement==null)return null;

            resMap.put("TASK_NAME", taskName);
            List<Element> propertyElements = taskElement.elements("property");
            if(propertyElements==null || propertyElements.size()< 1)return null;
            for(Element property : propertyElements){
                String key = property.element("name").getText();
                String value = property.element("value").getText();
                resMap.put(key, value);
            }
        } catch (DocumentException | FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return resMap;
    }

}
