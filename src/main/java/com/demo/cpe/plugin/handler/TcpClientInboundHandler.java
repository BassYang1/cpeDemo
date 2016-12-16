package com.demo.cpe.plugin.handler;

import com.alibaba.fastjson.JSONObject;
import com.demo.cpe.plugin.control.PlugInControl;
import com.demo.cpe.plugin.server.NettyTcpClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TCP 报文解析Handler
 * Created by ZJL on 2016/11/8.
 */
public class TcpClientInboundHandler extends SimpleChannelInboundHandler<String> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String SN = null;
    private boolean certification = false;
    private ExecutorService executorService;

    public TcpClientInboundHandler(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        logger.info("收到vertx报文<<==={}", s);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(s.substring(4, s.length()));
        } catch (Exception e) {
            return;
        }

        if (StringUtils.isNotBlank(jsonObject.getString("PONG"))) {
            return;
        }

        if (Integer.parseInt(jsonObject.getString("ID")) == 1001) {
            if (Integer.parseInt(jsonObject.getString("Result")) == 0) {
                JSONObject registerObject = new JSONObject();
                if (StringUtils.isBlank(SN)) {
                    SN = jsonObject.getString("MAC");
                }
                registerObject.put("RPCMethod", "Register");
                registerObject.put("ID", 1002);
                registerObject.put("MAC", SN);
                registerObject.put("CheckGateway", jsonObject.getString("ChallengeCode"));
                registerObject.put("DevRND", RandomStringUtils.randomNumeric(16));
                NettyTcpClient.writeAndFlush(ctx, registerObject);
                return;
            }
        } else if (Integer.parseInt(jsonObject.getString("ID")) == 1002) {
            if (Integer.parseInt(jsonObject.getString("Result")) == 0) {
                certification = true;
                return;
            }
        }

        if (certification) {
            final PlugInControl plugInControl = new PlugInControl(SN, jsonObject, ctx);
            executorService.execute(plugInControl);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("---服务器是活跃的 ---");
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 10s 之后尝试重新连接服务器
        System.out.println("---服务器是不活跃的---10s 之后尝试重新连接服务器...");
        if (StringUtils.isNotBlank(SN)) {
            final EventLoop eventLoop = ctx.channel().eventLoop();
            eventLoop.schedule(() -> {
                try {
                    NettyTcpClient.doConnect(SN);
                } catch (InterruptedException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }, 3L, TimeUnit.SECONDS);
            super.channelInactive(ctx);
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 不管是读事件空闲还是写事件空闲都向服务器发送心跳包
            if (certification) {
                sendHeartbeatPacket(ctx);
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("连接出现异常");
    }


    /**
     * 发送心跳包
     *
     * @param ctx
     */

    private void sendHeartbeatPacket(ChannelHandlerContext ctx) throws Exception {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("RPCMethod", "Hb");
        jsonObject.put("PING", "PING");
        NettyTcpClient.writeAndFlush(ctx, jsonObject);
    }


}
