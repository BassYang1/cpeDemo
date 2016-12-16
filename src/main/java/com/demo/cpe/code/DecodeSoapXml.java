package com.demo.cpe.code;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.SoapMessageModel;

/**
 * Created by ZJL on 2016/8/25.
 */
public class DecodeSoapXml {

    public static StringBuilder methodToString(AbstractMethod abstractMethod) {
        StringBuilder builder = null;
        try {
            if (abstractMethod != null) {
                SoapMessageModel soapMessageModel = new SoapMessageModel();
                abstractMethod.addAllSoap(soapMessageModel);
                builder = new StringBuilder(abstractMethod.getMsgToString(soapMessageModel));
            }
        } catch (Exception e) {

        }
        return builder;
    }

    public final static AbstractMethod stringToMethod(StringBuilder stringBuilder) {
        AbstractMethod method = null;
        try {
            if (stringBuilder != null && stringBuilder.length() > 0) {
                SoapMessageModel soapMsg = AbstractMethod.getStringToMsg(stringBuilder.toString());
                String classPath = AbstractMethod.METHOD_CLASS_PATH + AbstractMethod.getRequestName(soapMsg);
                method = (AbstractMethod) Class.forName(classPath).newInstance();
                method.parse(soapMsg);
            }
        } catch (Exception e) {

        }
        return method;
    }
}
