package com.kv.distributedkv.controllers;

import com.kv.distributedkv.constants.KVUrl;
import com.kv.distributedkv.dtos.KVResponse;
import com.kv.distributedkv.dtos.ResponseStatus;
import com.kv.distributedkv.services.KVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class KVController {

    @Autowired
    private KVService kvService;

    @GetMapping(KVUrl.KV + "/{key}")
    public KVResponse get(@PathVariable("key") String key, HttpServletResponse servletResponse) {
        KVResponse kvRes = kvService.get(key);
        if (kvRes.getStatus() == ResponseStatus.FAILED) {
            servletResponse.setStatus(404);
        }
        return kvRes;
    }

    @GetMapping(KVUrl.KV_GET_FROM_NODE + "/{key}")
    public KVResponse getFromPrimaryNode(
            @PathVariable("key") String key,
            HttpServletResponse servletResponse
    ) {
        KVResponse kvRes = kvService.getFromANode(key);
        if (kvRes.getStatus() == ResponseStatus.FAILED) {
            servletResponse.setStatus(404);
        }
        return kvRes;
    }

    @PostMapping(KVUrl.KV + "/{key}")
    public KVResponse post(@PathVariable("key") String key, @RequestBody String data, HttpServletResponse servletResponse) {
        KVResponse kvRes = kvService.post(key, data, null);
        if (kvRes.getStatus() == ResponseStatus.FAILED) {
            servletResponse.setStatus(500);
        }
        return kvRes;
    }

    @PostMapping(KVUrl.KV_REPLICATE + "/{key}/{replication}")
    public KVResponse postReplicate(
            @PathVariable("key") String key,
            @PathVariable("replication") int replication,
            @RequestBody String data
    ) {
        return kvService.post(key, data, replication);
    }

    @PutMapping(KVUrl.KV + "/{key}")
    public KVResponse put(@PathVariable("key") String key, @RequestBody String data) {
        return kvService.put(key, data);
    }

    @GetMapping(KVUrl.KV_ALL_DATA)
    public Map<String, String> getAllDataStoredOnThisMachine() {
        return kvService.getData();
    }
}
