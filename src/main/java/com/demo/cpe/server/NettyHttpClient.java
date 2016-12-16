package com.demo.cpe.server;

import com.cmiot.acs.model.AbstractMethod;
import com.demo.cpe.ServerSetting;
import com.demo.cpe.code.DecodeSoapXml;
import com.demo.cpe.handler.HttpClientInboundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NettyHttpClient
 * Created by ZJL on 2016/11/8.
 */
public class NettyHttpClient {
    private static Logger logger = LoggerFactory.getLogger(NettyHttpClient.class);

    private static Bootstrap bootstrap = getBootstrap();

    private static synchronized Bootstrap getBootstrap() {
        if (bootstrap == null) {
            bootstrap = new Bootstrap();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new HttpResponseDecoder());
                    ch.pipeline().addLast(new HttpRequestEncoder());
                    ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));
                    ch.pipeline().addLast(new HttpClientInboundHandler());
                }
            });
        }
        return bootstrap;
    }


    public static void sendMsg(FullHttpRequest httpRequest) {
        try {
            httpRequest.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/xml");
            httpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            httpRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpRequest.content().readableBytes());
            httpRequest.headers().set(HttpHeaderNames.HOST, ServerSetting.ACS_IP+":"+ServerSetting.ACS_PORT);
            Channel channel = getBootstrap().connect(ServerSetting.ACS_IP, ServerSetting.ACS_PORT).sync().channel();
            channel.writeAndFlush(httpRequest).sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMsg(ChannelHandlerContext context, AbstractMethod method) {
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        if (method == null) {
            httpRequest.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/xml");
            httpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        } else {
            StringBuilder builder = DecodeSoapXml.methodToString(method);
            httpRequest.content().writeBytes(builder.toString().getBytes());
            logger.info("发到ACS报文========>{}", builder);
        }
        httpRequest.headers().set(HttpHeaderNames.HOST, ServerSetting.ACS_IP+":"+ServerSetting.ACS_PORT);
        httpRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpRequest.content().readableBytes());

        context.writeAndFlush(httpRequest).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }


}
