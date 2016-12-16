package com.demo.cpe.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.cpe.action.BusinessAction;
import com.demo.cpe.control.InstructParse;
import com.demo.cpe.control.impl.InstructHandleImpl;
import com.demo.cpe.pool.AbThreadPool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

/**
 * Created by ZJL on 2016/11/8.
 */
public class HttpClientInboundHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    public InstructParse informParse;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        StringBuilder builder = new StringBuilder();
        if (response instanceof LastHttpContent) {
            LastHttpContent content = response;
            ByteBuf buf = content.content();
            builder.append(buf.toString(CharsetUtil.UTF_8));
        }

        String sn = response.headers().get("sn");
        
        logger.info("收到ACS报文===========>{}", builder);

        if (response.status().code() == 200 && (builder == null || builder.length() == 0)) {
            System.out.println("结束====================>null 200");
            ctx.close();
            return;
        }

        if (informParse == null) {
            informParse = new InstructParse(new InstructHandleImpl(sn));
        }

        AbThreadPool.execute(new BusinessAction(ctx, builder, informParse, sn));
    }

}
