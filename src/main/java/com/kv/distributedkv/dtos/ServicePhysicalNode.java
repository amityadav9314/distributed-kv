package com.kv.distributedkv.dtos;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class ServicePhysicalNode implements Node {
    private String ip;
    private String port;
    private String md5Hash;
    private boolean orchestrator;

    public ServicePhysicalNode(String ip, String port,  String md5HashStr, boolean orchestrator) {
        this.ip = ip;
        this.port = port;
        this.orchestrator = orchestrator;
        this.md5Hash = md5HashStr;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public boolean isOrchestrator() {
        return orchestrator;
    }

    public void setOrchestrator(boolean orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(ip)
                .append(port)
                .append(orchestrator)
                .append((md5Hash))
                .toHashCode();
    }

    @Override
    public String getKey() {
        return md5Hash;
    }
}
