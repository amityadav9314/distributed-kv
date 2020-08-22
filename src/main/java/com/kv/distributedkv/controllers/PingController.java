package com.kv.distributedkv.controllers;

import com.kv.distributedkv.constants.KVUrl;
import com.kv.distributedkv.dtos.HostStatus;
import com.kv.distributedkv.dtos.PingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {
    @GetMapping(KVUrl.HEALTH)
    public PingResponse getPingResponse() {
        PingResponse pingResponse = new PingResponse();
        pingResponse.setStatus(HostStatus.UP);
        return pingResponse;
    }
}
