package com.kv.distributedkv;

import com.kv.distributedkv.services.OrchestratorService;
import com.kv.distributedkv.utils.KVUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class DistributedKvApplication implements ApplicationRunner {

    @Autowired
    private OrchestratorService orchestratorService;

    public static void main(String[] args) {
        SpringApplication.run(DistributedKvApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String thisHostName = InetAddress.getLocalHost().getHostAddress();
        String thisPort = System.getProperty("server.port");

        String orchestrator = System.getProperty("orchestrator");
        if (orchestrator == null) {
            // This is orchestrator only
            orchestrator = String.format("%s:%s", thisHostName, thisPort);
        }
        KVUtil.log(String.format("Orchestrator is: %s", orchestrator));
        Map<String, String> orchestratorIPAndPort = new HashMap<>();
        String[] splittedOrchestrator = orchestrator.split(":");
        String ip = splittedOrchestrator[0];
        String port = splittedOrchestrator[1];
        orchestratorService.setOrchestrator(ip, port);
        orchestratorService.callOrchestratorToRegister(thisHostName, thisPort);
    }
}
