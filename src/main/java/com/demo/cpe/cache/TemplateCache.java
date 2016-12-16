package com.demo.cpe.cache;

import com.demo.cpe.code.BaseUtil;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by ZJL on 2016/11/9.
 */
public class TemplateCache extends BaseUtil {



    public static ConcurrentMap<String, String[]> dataTemplateCache = new ConcurrentHashMap<>();

    public static void put(String key, String[] values) {
        if (values != null && values.length > 0) {
            dataTemplateCache.put(key, values);
        }
    }


    public static String[] get(String key) {
        return dataTemplateCache.get(key);
    }


    public static String getRandomValue(String name) {
        String value = null;
        try {
            String[] nameArr = name.split("\\.");
            String key = nameArr[nameArr.length - 1];
            if (dataTemplateCache.containsKey(key)) {
                String[] valueArr = get(key);
                if (valueArr != null && valueArr.length > 0) {
                    int index = (int) (Math.random() * valueArr.length);
                    value = valueArr[index];
                }
            }
        } catch (Exception e) {

        }
        return value;
    }


    public static void initAcs(String name) {
        try {
            Properties pro = new Properties();
            String path = getFilePath(name);
            FileInputStream file = new FileInputStream(path);
            pro.load(file);
            file.close();

            Iterator it = pro.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = String.valueOf(entry.getKey());
                String value = String.valueOf(entry.getValue());
                if (value != null && value.trim().length() > 0) {
                    String[] valueArr = value.split(",");
                    put(key, valueArr);
                }
            }
        } catch (Exception e) {

        } finally {

        }

    }


}
