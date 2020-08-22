package com.kv.distributedkv.controllers;

import com.kv.distributedkv.constants.KVUrl;
import com.kv.distributedkv.dtos.AvailableNodes;
import com.kv.distributedkv.dtos.BaseResponse;
import com.kv.distributedkv.dtos.ResponseStatus;
import com.kv.distributedkv.services.OrchestratorService;
import com.kv.distributedkv.utils.JsonConverter;
import com.kv.distributedkv.utils.KVUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrchestratorController {

    private OrchestratorService orchestratorService;

    @Autowired
    public OrchestratorController(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @GetMapping(KVUrl.DO_REGISTER)
    public AvailableNodes doRegister(@RequestParam("ip") String ip, @RequestParam("port") String port) {
        AvailableNodes newAvailableNodes = orchestratorService.doRegister(ip, port);
        KVUtil.log(String.format("%s:%s is successfully registered to orchestrator", ip, port));
        KVUtil.log(String.format("New available nodes are: %s", JsonConverter.convertObjectToJsonSafe(newAvailableNodes)));
        return newAvailableNodes;
    }

    @PostMapping(KVUrl.CALLBACK_TO_UPDATE_NODES)
    public BaseResponse updateAvailableNodes(@RequestBody AvailableNodes availableNodes) {
        orchestratorService.resetAvailableNodes(availableNodes);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setStatus(ResponseStatus.SUCCESS);
        return baseResponse;
    }

    @GetMapping(KVUrl.ALL_NODES)
    public AvailableNodes allNodes() {
        return orchestratorService.getAvailableNodes();
    }
}
