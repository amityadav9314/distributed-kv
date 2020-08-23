package com.kv.distributedkv.controllers;

import com.kv.distributedkv.constants.KVUrl;
import com.kv.distributedkv.dtos.KVResponse;
import com.kv.distributedkv.dtos.ResponseStatus;
import com.kv.distributedkv.services.KVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
public class KVController {

    @Autowired
    private KVService kvService;

    @GetMapping(KVUrl.KV + "/{key}")
    public KVResponse get(@PathVariable("key") String key, HttpServletResponse servletResponse) {
        KVResponse kvRes = kvService.get(key);
        if(kvRes.getStatus() == ResponseStatus.FAILED) {
            servletResponse.setStatus(404);
        }
        return kvRes;
    }

    @PostMapping(KVUrl.KV + "/{key}")
    public KVResponse post(@PathVariable("key") String key, @RequestBody String data) {
        return kvService.post(key, data);
    }

    @PutMapping(KVUrl.KV + "/{key}")
    public KVResponse put(@PathVariable("key") String key, @RequestBody String data) {
        return kvService.put(key, data);
    }
}
