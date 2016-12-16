package com.demo.cpe.code;

import java.io.File;

/**
 * Created by zjial on 2016/6/7.
 */
public class BaseUtil {


    public static String PATH = null;

    /**
     * 获取外部的资源文件
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    public static String getFilePath(String fileName) {
        if (PATH == null) {
            String myPath = BaseUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            PATH = new File(myPath).getParent() + File.separator;
        }
        return PATH + fileName;
    }
}
