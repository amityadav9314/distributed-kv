package com.kv.distributedkv.dtos;

public class PingResponse {
    private HostStatus status;

    public HostStatus getStatus() {
        return status;
    }

    public void setStatus(HostStatus status) {
        this.status = status;
    }
}
