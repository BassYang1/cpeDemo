package com.demo.cpe.plugin.control;

import com.alibaba.fastjson.JSONObject;
import com.demo.cpe.plugin.server.NettyTcpClient;
import io.netty.channel.ChannelHandlerContext;

/**
 * 方法控制器
 *
 * Created by ZJL on 2016/11/29.
 */
public class PlugInControl implements Runnable {
    private String sn;

    private JSONObject request;

    private ChannelHandlerContext ctx;

    public PlugInControl(String sn, JSONObject request, ChannelHandlerContext ctx) {
        this.sn = sn;
        this.request = request;
        this.ctx = ctx;
    }

    @Override
    public void run() {
        final PlugInAction plugInAction = new PlugInAction(sn, request);
        JSONObject jsonObject = plugInAction.methodControl();
        try {
            NettyTcpClient.writeAndFlush(ctx, jsonObject);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
