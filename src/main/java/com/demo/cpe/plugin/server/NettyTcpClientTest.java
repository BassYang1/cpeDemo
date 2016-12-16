package com.demo.cpe.plugin.server;

import java.io.UnsupportedEncodingException;

/**
 * Created by ZJL on 2016/11/28.
 */
public class NettyTcpClientTest {

    public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {
        NettyTcpClient.doConnect("1C25E1012345");
    }
}
