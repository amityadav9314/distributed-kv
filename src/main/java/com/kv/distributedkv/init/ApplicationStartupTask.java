package com.kv.distributedkv.init;

import com.kv.distributedkv.rest.RESTCall;
import com.kv.distributedkv.services.OrchestratorService;
import com.kv.distributedkv.utils.KVUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;

@Component
public class ApplicationStartupTask {

    @Autowired
    private OrchestratorService orchestratorService;

    @Autowired
    private RESTCall restCall;

    @EventListener(ApplicationReadyEvent.class)
    public void registerANodeToOrchestrator() throws UnknownHostException {
        Pair<String, String> ipPortPair = KVUtil.getIPAndPort();
        String thisHostName = ipPortPair.getLeft();
        String thisPort = ipPortPair.getRight();

        String orchestrator = System.getProperty("orchestrator");
        if (orchestrator == null) {
            // This is orchestrator only
            orchestrator = String.format("%s:%s", thisHostName, thisPort);
        }
        KVUtil.log(String.format("Orchestrator is: %s", orchestrator));
        String[] splittedOrchestrator = orchestrator.split(":");
        String ip = splittedOrchestrator[0];
        String port = splittedOrchestrator[1];
        orchestratorService.setOrchestrator(ip, port);
        restCall.callOrchestratorToRegister(thisHostName, thisPort, orchestratorService.getOrchestrator());
    }
}
