package com.demo.cpe.plugin.cache;

import com.demo.cpe.plugin.model.PluginModel;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by ZJL on 2016/11/29.
 */
public class PluginManage {
    public static ConcurrentMap<String, List<PluginModel>> PLUGIN_DB = new ConcurrentHashMap<>();
}
