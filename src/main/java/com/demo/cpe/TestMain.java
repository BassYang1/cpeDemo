package com.demo.cpe;

import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.struct.EventStruct;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.demo.cpe.cache.CpeCache;
import com.demo.cpe.cache.TemplateCache2;
import com.demo.cpe.code.BaseUtil;
import com.demo.cpe.code.DecodeSoapXml;
import com.demo.cpe.code.LogBackConfigLoader;
import com.demo.cpe.plugin.server.NettyTcpClient;
import com.demo.cpe.server.NettyHttpClient;
import com.demo.cpe.server.NettyHttpServer;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ZJL on 2016/11/23.
 */
public class TestMain {
    private static Logger logger = LoggerFactory.getLogger(NettyHttpClient.class);
    private static ExecutorService executorService = new ThreadPoolExecutor(256, 256, 256, TimeUnit.SECONDS,
            new LinkedTransferQueue<Runnable>());

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void main(String[] args) throws Exception {
        LogBackConfigLoader.load(BaseUtil.getFilePath("logback.xml"));

        int cpe_num = ServerSetting.CPE_NUM;
        String cpe_sn = ServerSetting.CPE_SN;//ODJI8202LYN
        String cpe_password = ServerSetting.CPE_PASSWORD;
        int http_port = ServerSetting.HTTP_PORT;
        InetAddress addr = InetAddress.getLocalHost();
        String ip = addr.getHostAddress();
        ip = "http://" + ip + ":" + http_port + "/";
        for (int i = 0; i < cpe_num; i++) {
            String newSN = cpe_sn + i;
            TemplateCache2.load("cpe.xml", newSN);
            Inform inform = CpeCache.getInform();
            inform.getDeviceId().setSerialNubmer(newSN);
            List<EventStruct> eventCodes = new ArrayList<EventStruct>();
            eventCodes.add(new EventStruct("0 BOOTSTRAP"));
            eventCodes.add(new EventStruct("X CMCC BIND"));
            inform.getEvent().setEventCodes(eventCodes);
            List<ParameterValueStruct> list = inform.getParameterList().getParameterValueStructs();
            for (ParameterValueStruct pvs : list) {
                if ("InternetGatewayDevice.ManagementServer.ConnectionRequestURL".equals(pvs.getName())) {
                    pvs.setValue(ip + cpe_sn + i);
                }
                if ("InternetGatewayDevice.X_CMCC_UserInfo.Password".equals(pvs.getName())) {
                    pvs.setValue(cpe_password + i);
                }
            }
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    StringBuilder builder = DecodeSoapXml.methodToString(inform);
                    FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/",
                            Unpooled.wrappedBuffer(builder.toString().getBytes()));
                    logger.info("发到ACS报文========>{}", builder);
                    NettyHttpClient.sendMsg(request);
                    try {
                        NettyTcpClient.doConnect(newSN);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        NettyHttpServer httpServer = new NettyHttpServer(executorService);
        httpServer.start(ServerSetting.HTTP_PORT);

    }

}
