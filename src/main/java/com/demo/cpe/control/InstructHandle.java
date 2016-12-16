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
public interface InstructHandle {

    public AbstractMethod download(Download download);

    public AbstractMethod getParameterValues(GetParameterValues values);
    
    public AbstractMethod getParameterNames(GetParameterNames values);

    public AbstractMethod setParameterValues(SetParameterValues values);
    
    public AbstractMethod addObject(AddObject values);
    
    public AbstractMethod deleteObject(DeleteObject values);

}
