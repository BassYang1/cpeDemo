package com.demo.cpe.control;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.AddObject;
import com.cmiot.acs.model.DeleteObject;
import com.cmiot.acs.model.Download;
import com.cmiot.acs.model.GetParameterNames;
import com.cmiot.acs.model.GetParameterValues;
import com.cmiot.acs.model.SetParameterValues;

/**
 * Created by ZJL on 2016/11/9.
 */
public class InstructParse {

    private InstructHandle instructHandle;

    public InstructParse(InstructHandle instructHandle) {
        this.instructHandle = instructHandle;
    }

    /**
     * 分析ACS下发的指令
     *
     * @param requestMethod
     * @return
     */
    public AbstractMethod parseAcsRequestMethod(AbstractMethod requestMethod) {
        AbstractMethod responseMethod = null;
        if (requestMethod instanceof SetParameterValues) {
            responseMethod = instructHandle.setParameterValues((SetParameterValues) requestMethod);
        } else if (requestMethod instanceof GetParameterValues) {
            responseMethod = instructHandle.getParameterValues((GetParameterValues) requestMethod);
        } else if (requestMethod instanceof Download) {
            responseMethod = instructHandle.download((Download) requestMethod);
        } else if (requestMethod instanceof GetParameterNames) {
            responseMethod = instructHandle.getParameterNames((GetParameterNames) requestMethod);
        }else if (requestMethod instanceof DeleteObject) {
            responseMethod = instructHandle.deleteObject((DeleteObject) requestMethod);
        }else if (requestMethod instanceof AddObject) {
            responseMethod = instructHandle.addObject((AddObject) requestMethod);
        }
        
        return responseMethod;
    }

}
