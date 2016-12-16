package com.demo.cpe.plugin.server;

import com.alibaba.fastjson.JSONObject;
import com.demo.cpe.ServerSetting;
import com.demo.cpe.plugin.handler.TcpClientInboundHandler;
import com.demo.cpe.pool.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ZJL on 2016/11/28.
 */
public class NettyTcpClient {
    private static Logger logger = LoggerFactory.getLogger(NettyTcpClient.class);
    private static Bootstrap bootstrap = getBootstrap();

    public static ExecutorService PLUG_IN_THREAD_POOL = new ThreadPoolExecutor(256, 256, 256, TimeUnit.SECONDS,
            new LinkedTransferQueue<>(), new NamedThreadFactory("PLUG_IN-CONSUMER"));


    private static synchronized Bootstrap getBootstrap() {
        if (bootstrap == null) {
            bootstrap = new Bootstrap();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                    ch.pipeline().addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                    ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));
                    ch.pipeline().addLast(new IdleStateHandler(0, 600, 0));
                    ch.pipeline().addLast(new TcpClientInboundHandler(PLUG_IN_THREAD_POOL));
                }
            });
        }
        return bootstrap;
    }


    /**
     * 抽取出该方法 (断线重连时使用)
     *
     * @throws InterruptedException
     */
    public static void doConnect(String mac) throws InterruptedException, UnsupportedEncodingException {
        Channel channel = getBootstrap().connect(ServerSetting.VERTX_IP, ServerSetting.VERTX_PORT).sync().channel();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("RPCMethod", "BootInitiation");
        jsonObject.put("ID", 1001);
        jsonObject.put("PROTVersion", "BootInitiation");
        jsonObject.put("MAC", mac);
        String data = jsonObject.toJSONString();
        String length = new String(int2Byte(data.length()));
        StringBuilder builder = new StringBuilder(length).append(data);
        logger.info("发到vertx报文===>>{}", builder);
        channel.writeAndFlush(builder.toString()).sync();
    }


    /**
     * 发送数据
     *
     * @param jsonObject
     * @throws InterruptedException
     */
    public static void writeAndFlush(ChannelHandlerContext ctx, JSONObject jsonObject) throws InterruptedException {
        String data = jsonObject.toJSONString();
        String length = new String(int2Byte(data.length()));
        StringBuilder builder = new StringBuilder(length).append(data);
        logger.info("发到vertx报文===>>{}", builder);
        ctx.writeAndFlush(builder.toString());
    }


    private static byte[] int2Byte(int intValue) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (intValue >> 8 * (3 - i) & 0xFF);
        }
        return b;
    }
}
