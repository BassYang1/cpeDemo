package com.demo.cpe.action;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.DownloadResponse;
import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.InformResponse;
import com.cmiot.acs.model.SetParameterValues;
import com.cmiot.acs.model.struct.Event;
import com.cmiot.acs.model.struct.EventStruct;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.demo.cpe.ServerSetting;
import com.demo.cpe.cache.CpeCache;
import com.demo.cpe.code.DecodeSoapXml;
import com.demo.cpe.control.InstructParse;
import com.demo.cpe.server.NettyHttpClient;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by ZJL on 2016/11/8.
 */
public class BusinessAction implements Runnable {
    public String sn;
    public ChannelHandlerContext ctx;
    public StringBuilder builder;
    public InstructParse instructParse;

    public BusinessAction(ChannelHandlerContext ctx, StringBuilder builder, InstructParse instructParse, String sn) {
        this.ctx = ctx;
        this.builder = builder;
        this.instructParse = instructParse;
        this.sn = sn;
    }

    @Override
    public void run() {
        AbstractMethod requestMethod = DecodeSoapXml.stringToMethod(builder);
        if (requestMethod instanceof InformResponse) {
            NettyHttpClient.sendMsg(ctx, null);
            return;
        } else {
            AbstractMethod responseMethod = instructParse.parseAcsRequestMethod(requestMethod);
            NettyHttpClient.sendMsg(ctx, responseMethod);
            // 判断是否是诊断
            boolean b = false;
            if (requestMethod instanceof SetParameterValues) {
                SetParameterValues setParameterValues = (SetParameterValues) requestMethod;
                for (ParameterValueStruct pvs : setParameterValues.getParameterList().getParameterValueStructs()) {
                    String name = pvs.getName();
                    // 如果是诊断
                    if ("InternetGatewayDevice.X_CMCC_PPPOE_EMULATOR.DiagnosticsState".equals(name)
                            || name.matches("InternetGatewayDevice.Services.VoiceService.[0-9]+.PhyInterface.\\d+.Tests.TestState")) {
                        b = true;
                        break;
                    }
                }
            }else if (responseMethod instanceof DownloadResponse) {
                try {
                    Thread.sleep(2000);
                    int http_port = ServerSetting.HTTP_PORT;
                    InetAddress addr = InetAddress.getLocalHost();
                    String ip = addr.getHostAddress();
                    ip = "http://" + ip + ":" + http_port +"/";
                    Inform laInform = CpeCache.getInform();
                    List<EventStruct> eventCodes = new ArrayList<>();
                    eventCodes.add(new EventStruct(Event.TRANSFER_COMPLETE));
                    eventCodes.add(new EventStruct("M Download"));
                    laInform.getEvent().setEventCodes(eventCodes);
                    laInform.getDeviceId().setSerialNubmer(sn);
                    List<ParameterValueStruct> list = laInform.getParameterList().getParameterValueStructs();
                    for (ParameterValueStruct pvs : list) {
                        if ("InternetGatewayDevice.ManagementServer.ConnectionRequestURL".equals(pvs.getName())) {
                            pvs.setValue(ip + sn);
                        }
                    }
                    NettyHttpClient.sendMsg(ctx, laInform);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            if (b) {
                try {
                    Thread.sleep(2000);
                    int http_port = ServerSetting.HTTP_PORT;
                    InetAddress addr = InetAddress.getLocalHost();
                    String ip = addr.getHostAddress();
                    ip = "http://" + ip + ":" + http_port +"/";
                    Inform laInform = CpeCache.getInform();
                    List<EventStruct> eventCodes = new ArrayList<>();
                    eventCodes.add(new EventStruct(Event.DIAGNOSTICS_COMPLETE));
                    laInform.getEvent().setEventCodes(eventCodes);
                    laInform.getDeviceId().setSerialNubmer(sn);
                    List<ParameterValueStruct> list = laInform.getParameterList().getParameterValueStructs();
                    for (ParameterValueStruct pvs : list) {
                        if ("InternetGatewayDevice.ManagementServer.ConnectionRequestURL".equals(pvs.getName())) {
                            pvs.setValue(ip + sn);
                        }
                    }
                    NettyHttpClient.sendMsg(ctx, laInform);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    }
}
