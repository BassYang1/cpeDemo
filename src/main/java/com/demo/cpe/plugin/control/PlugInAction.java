package com.demo.cpe.plugin.control;

import com.alibaba.fastjson.JSONObject;
import com.demo.cpe.plugin.cache.PluginManage;
import com.demo.cpe.plugin.model.PluginModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 模拟插件
 * Created by ZJL on 2016/11/29.
 */
public class PlugInAction implements Serializable {

    private static final long serialVersionUID = -1220553082535572311L;

    private String sn;

    private JSONObject request;

    public PlugInAction(String sn, JSONObject request) {
        this.sn = sn;
        this.request = request;
    }

    public JSONObject methodControl() {
        String method = request.getString("RPCMethod");
        switch (method) {
            case "Install":
                return install();
            case "Install_query":
                return installQuery();
            case "Install_cancel":
                return installCancel();
            case "UnInstall":
                return uninstall();
            case "Stop":
                return stop();
            case "Run":
                return run();
            case "FactoryPlugin":
                return factoryPlugin();
            case "ListPlugin":
                return listPlugin();
            default:
                JSONObject response = new JSONObject();
                response.put("ID", request.getIntValue("ID"));
                response.put("Result", -500);
                return response;
        }
    }


    /**
     * 插件安装
     *
     * @return json
     */
    private JSONObject install() {
        JSONObject response = new JSONObject();
        response.put("ID", request.getIntValue("ID"));
        PluginModel pluginModel = new PluginModel(request.getString("Plugin_Name"), request.getString("Version"));
        List<PluginModel> pluginModelList = PluginManage.PLUGIN_DB.get(sn);
        boolean install = true;
        if (pluginModelList == null) {
            pluginModelList = new ArrayList<>();
            PluginManage.PLUGIN_DB.put(sn, pluginModelList);
        } else {
            for (PluginModel model : pluginModelList) {
                if (model.getPlugin_Name().equals(pluginModel.getPlugin_Name())) {
                    install = false;
                    break;
                }
            }
        }
        if (install) {
            pluginModelList.add(pluginModel);
            response.put("Result", 0);
        } else {
            response.put("Result", 4);
        }

        return response;
    }


    /**
     * 安装进度查询
     *
     * @return json
     */
    private JSONObject installQuery() {
        JSONObject response = new JSONObject();
        response.put("ID", request.getIntValue("ID"));

        List<PluginModel> pluginModelList = PluginManage.PLUGIN_DB.get(sn);
        boolean installQuery = false;
        if (pluginModelList != null && pluginModelList.size() > 0) {
            for (PluginModel model : pluginModelList) {
                if (model.getPlugin_Name().equals(request.getString("Plugin_Name"))) {
                    installQuery = true;
                    break;
                }
            }
        }
        if (installQuery) {
            response.put("Result", 0);
            response.put("Percent", 100);
        } else {
            response.put("Result", -106);
        }
        return response;
    }


    /**
     * 取消插件安装
     *
     * @return json
     */
    private JSONObject installCancel() {
        JSONObject response = new JSONObject();
        response.put("ID", request.getIntValue("ID"));
        List<PluginModel> pluginModelList = PluginManage.PLUGIN_DB.get(sn);
        boolean installCancel = false;
        if (pluginModelList != null && pluginModelList.size() > 0) {
            for (PluginModel model : pluginModelList) {
                if (model.getPlugin_Name().equals(request.getString("Plugin_Name"))) {
                    installCancel = true;
                    break;
                }
            }
        }
        if (installCancel) {
            response.put("Result", -111);
        } else {
            response.put("Result", -101);
        }
        return response;
    }


    /**
     * 插件卸载
     *
     * @return json
     */
    private JSONObject uninstall() {
        JSONObject response = new JSONObject();
        response.put("ID", request.getIntValue("ID"));
        List<PluginModel> pluginModelList = PluginManage.PLUGIN_DB.get(sn);
        boolean uninstall = false;
        if (pluginModelList != null && pluginModelList.size() > 0) {
            for (int i = 0; i < pluginModelList.size(); i++) {
                PluginModel model = pluginModelList.get(i);
                if (model.getPlugin_Name().equals(request.getString("Plugin_Name"))) {
                    pluginModelList.remove(i);
                    uninstall = true;
                    break;
                }
            }
        }
        if (uninstall) {
            response.put("Result", 0);
        } else {
            response.put("Result", -112);
        }
        return response;

    }


    /**
     * 停止插件
     *
     * @return json
     */
    private JSONObject stop() {
        JSONObject response = new JSONObject();
        response.put("ID", request.getIntValue("ID"));
        List<PluginModel> pluginModelList = PluginManage.PLUGIN_DB.get(sn);
        boolean stop = false;
        if (pluginModelList != null && pluginModelList.size() > 0) {
            for (PluginModel model : pluginModelList) {
                if (model.getPlugin_Name().equals(request.getString("Plugin_Name"))) {
                    model.setRun(1);
                    stop = true;
                    break;
                }
            }
        }
        if (stop) {
            response.put("Result", 0);
        } else {
            response.put("Result", -112);
        }
        return response;
    }


    /**
     * 启动插件
     *
     * @return json
     */
    private JSONObject run() {
        JSONObject response = new JSONObject();
        response.put("ID", request.getIntValue("ID"));
        List<PluginModel> pluginModelList = PluginManage.PLUGIN_DB.get(sn);
        boolean run = false;
        if (pluginModelList != null && pluginModelList.size() > 0) {
            for (PluginModel model : pluginModelList) {
                if (model.getPlugin_Name().equals(request.getString("Plugin_Name"))) {
                    model.setRun(0);
                    run = true;
                    break;
                }
            }
        }
        if (run) {
            response.put("Result", 0);
        } else {
            response.put("Result", -112);
        }
        return response;
    }


    /**
     * 恢复插件默认参数
     *
     * @return json
     */
    private JSONObject factoryPlugin() {
        JSONObject response = new JSONObject();
        response.put("ID", request.getIntValue("ID"));
        List<PluginModel> pluginModelList = PluginManage.PLUGIN_DB.get(sn);
        boolean factoryPlugin = false;
        if (pluginModelList != null && pluginModelList.size() > 0) {
            for (PluginModel model : pluginModelList) {
                if (model.getPlugin_Name().equals(request.getString("Plugin_Name"))) {
                    factoryPlugin = true;
                    break;
                }
            }
        }
        if (factoryPlugin) {
            response.put("Result", 0);
        } else {
            response.put("Result", -112);
        }
        return response;
    }


    /**
     * 获取已安装插件的请求列表
     *
     * @return json
     */
    private JSONObject listPlugin() {
        JSONObject response = new JSONObject();
        response.put("ID", request.getIntValue("ID"));
        List<PluginModel> pluginModelList = PluginManage.PLUGIN_DB.get(sn);
        response.put("Result", 0);
        if (pluginModelList != null) {
            response.put("Plugin", pluginModelList);
        } else {
            response.put("Plugin", new ArrayList<>());
        }
        return response;


    }


}
