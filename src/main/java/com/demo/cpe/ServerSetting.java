package com.demo.cpe;

import com.demo.cpe.code.PropertiesUtil;

/**
 * Created by ZJL on 2016/11/8.
 */
public class ServerSetting {
    public static final String ACS_IP = PropertiesUtil.getPropertiesValue("acs.ip");
    public static final int ACS_PORT = PropertiesUtil.getPropertiesValueInt("acs.port");
    public static final int BUSINESS_POOL_NUM = PropertiesUtil.getPropertiesValueInt("business.pool.num");

    public static final int HTTP_PORT = PropertiesUtil.getPropertiesValueInt("http.port");
    
    //模拟cpe数量
    public static final int CPE_NUM = PropertiesUtil.getPropertiesValueInt("cpe.num");
    public static final String CPE_SN = PropertiesUtil.getPropertiesValue("cpe.sn");
    public static final String CPE_PASSWORD = PropertiesUtil.getPropertiesValue("cpe.password");


    public static final String VERTX_IP = PropertiesUtil.getPropertiesValue("vertx.ip");

    public static final int VERTX_PORT = PropertiesUtil.getPropertiesValueInt("vertx.port");
}
