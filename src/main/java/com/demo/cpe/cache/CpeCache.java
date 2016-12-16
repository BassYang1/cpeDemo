package com.demo.cpe.cache;

import com.cmiot.acs.model.Inform;
import com.demo.cpe.code.BaseUtil;
import com.demo.cpe.code.DecodeSoapXml;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by ZJL on 2016/11/8.
 */
public class CpeCache extends BaseUtil {
    private static final Inform inform = (Inform) DecodeSoapXml.stringToMethod(fileToStringBuilder(getFilePath("Inform.xml")));

    public static Inform getInform() {
        //Inform lastInform = (Inform) DecodeSoapXml.stringToMethod(fileToStringBuilder(getFilePath("Inform.xml")));
        return inform;
    }
    
    /**
     * 将File转StringBuilder
     *
     * @param filePath
     * @return
     */
    private static StringBuilder fileToStringBuilder(String filePath) {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            String tempString = null;
            File file = new File(filePath);
            reader = new BufferedReader(new FileReader(file));
            while (StringUtils.isNotBlank(tempString = reader.readLine())) {
                builder.append(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return builder;
    }

}
