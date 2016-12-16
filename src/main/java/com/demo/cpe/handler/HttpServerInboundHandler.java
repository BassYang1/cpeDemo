package com.demo.cpe.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.struct.Event;
import com.cmiot.acs.model.struct.EventStruct;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.demo.cpe.ServerSetting;
import com.demo.cpe.cache.CpeCache;
import com.demo.cpe.cache.TemplateCache2;
import com.demo.cpe.code.DecodeSoapXml;
import com.demo.cpe.server.NettyHttpClient;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ZJL on 2016/11/18.
 */
public class HttpServerInboundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ExecutorService executorService;

    public HttpServerInboundHandler(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest msg) throws Exception {

        String uri = msg.uri();

        String sn = uri.substring(1, uri.length());
        logger.info("CPE反向连接SN:{}", sn);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!TemplateCache2.cpeMap.containsKey(sn)){
                        TemplateCache2.load("cpe.xml", sn);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int http_port = ServerSetting.HTTP_PORT;
                InetAddress addr = null;
                try {
                    addr = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                String ip = addr.getHostAddress();
                ip = "http://" + ip + ":" + http_port +"/";
                Inform laInform = CpeCache.getInform();
                List<EventStruct> eventCodes = new ArrayList<>();
                eventCodes.add(new EventStruct(Event.CONNECTION_REQUEST));
                laInform.getEvent().setEventCodes(eventCodes);
                laInform.getDeviceId().setSerialNubmer(sn);
                List<ParameterValueStruct> list = laInform.getParameterList().getParameterValueStructs();
                for (ParameterValueStruct pvs : list) {
                    if ("InternetGatewayDevice.ManagementServer.ConnectionRequestURL".equals(pvs.getName())) {
                        pvs.setValue(ip + sn);
                        break;
                    }
                }
                StringBuilder builder = DecodeSoapXml.methodToString(laInform);
                FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/",
                        Unpooled.wrappedBuffer(builder.toString().getBytes()));
                NettyHttpClient.sendMsg(request);
            }
        });
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    // 出现异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    // 断开链接
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }
}
