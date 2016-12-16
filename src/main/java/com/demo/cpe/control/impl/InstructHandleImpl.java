package com.demo.cpe.control.impl;

import com.cmiot.acs.model.*;
import com.cmiot.acs.model.struct.*;
import com.demo.cpe.cache.TemplateCache;
import com.demo.cpe.cache.TemplateCache2;
import com.demo.cpe.control.InstructHandle;

import org.dom4j.Element;

import java.util.List;

/**
 * Created by ZJL on 2016/11/9.
 */
public class InstructHandleImpl extends TemplateCache implements InstructHandle {
    private String sn;

    public InstructHandleImpl(String sn) {
        this.sn = sn;
    }

    @Override
    public AbstractMethod getParameterNames(GetParameterNames values) {
        GetParameterNamesResponse response = new GetParameterNamesResponse();
        response.setRequestId(values.getRequestId());
        List<ParameterInfoStruct> parameterList = TemplateCache2.getPathByName(TemplateCache2.cpeMap.get(sn),
                values.getParameterPath(), values.isNextLevel());
        response.setParameterList(parameterList);
        return response;
    }

    @Override
    public AbstractMethod addObject(AddObject values) {
        AddObjectResponse response = new AddObjectResponse();
        int num = TemplateCache2.addObject(TemplateCache2.cpeMap.get(sn), values.getObjectName());
        response.setRequestId(values.getRequestId());
        response.setInstanceNumber(num);
        response.setStatus(0);
        return response;
    }

    @Override
    public AbstractMethod deleteObject(DeleteObject values) {
        DeleteObjectResponse response = new DeleteObjectResponse();
        response.setRequestId(values.getRequestId());
        boolean b = TemplateCache2.delObject(TemplateCache2.cpeMap.get(sn), values.getObjectName());
        System.out.println(b);
        response.setStatus(1);
        return response;
    }

    @Override
    public AbstractMethod download(Download download) {
        DownloadResponse response = new DownloadResponse();
        response.setRequestId(download.getRequestId());
        response.setStatus(1);
        return response;
    }

    @Override
    public AbstractMethod getParameterValues(GetParameterValues values) {
        GetParameterValuesResponse response = new GetParameterValuesResponse();
        response.setRequestId(values.getRequestId());
        ParameterList parameterList = new ParameterList();
        for (String name : values.getParameterNames()) {
        	List<Element> list = TemplateCache2.getElementListByName(TemplateCache2.cpeMap.get(sn), name);
            String value = list.get(0).getText();
            try {
                int valueInt = Integer.parseInt(value);
                ParameterValueStructInt valueStructInt = new ParameterValueStructInt(name, valueInt);
                parameterList.addParamValues(valueStructInt);
            } catch (Exception e) {
                ParameterValueStructStr valueStructStr = new ParameterValueStructStr(name, value);
                parameterList.addParamValues(valueStructStr);
            }
        }
        response.setParameterList(parameterList);
        return response;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public AbstractMethod setParameterValues(SetParameterValues values) {
        ParameterList parameterList = values.getParameterList();
        List<ParameterValueStruct> pvsList = parameterList.getParameterValueStructs();
        for (ParameterValueStruct pvs : pvsList) {
            String name = pvs.getName();
            String value = pvs.getValue().toString();
            // 如果是诊断
            if ("InternetGatewayDevice.X_CMCC_PPPOE_EMULATOR.DiagnosticsState".equals(name) || name
                    .matches("InternetGatewayDevice.Services.VoiceService.[0-9]+.PhyInterface.\\d+.Tests.TestState")) {
                value = "Complete";
            }
            List<Element> list = TemplateCache2.getElementListByName(TemplateCache2.cpeMap.get(sn), name);
            list.get(0).setText(value);
        }

        SetParameterValuesResponse response = new SetParameterValuesResponse();
        response.setRequestId(values.getRequestId());
        response.setStatus(0);
        return response;
    }

}
