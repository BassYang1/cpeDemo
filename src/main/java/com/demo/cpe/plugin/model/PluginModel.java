package com.demo.cpe.plugin.model;

import java.io.Serializable;

/**
 * Created by ZJL on 2016/11/29.
 */
public class PluginModel implements Serializable {

    private static final long serialVersionUID = 8811812716721946628L;
    private String Plugin_Name;
    private String Version;
    private int Run = 1;

    public PluginModel(String plugin_Name, String version) {
        Plugin_Name = plugin_Name;
        Version = version;
    }

    public PluginModel(String plugin_Name, String version, int run) {
        Plugin_Name = plugin_Name;
        Version = version;
        Run = run;
    }

    public String getPlugin_Name() {
        return Plugin_Name;
    }

    public void setPlugin_Name(String plugin_Name) {
        Plugin_Name = plugin_Name;
    }

    public String getVersion() {
        return Version;
    }

    public void setVersion(String version) {
        Version = version;
    }

    public int getRun() {
        return Run;
    }

    public void setRun(int run) {
        Run = run;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PluginModel{");
        sb.append("Plugin_Name='").append(Plugin_Name).append('\'');
        sb.append(", Version='").append(Version).append('\'');
        sb.append(", Run=").append(Run);
        sb.append('}');
        return sb.toString();
    }
}
