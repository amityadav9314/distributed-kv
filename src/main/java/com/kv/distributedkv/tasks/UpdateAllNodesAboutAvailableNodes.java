package com.kv.distributedkv.tasks;

import com.kv.distributedkv.dtos.AvailableNodes;
import com.kv.distributedkv.dtos.Node;
import com.kv.distributedkv.services.OrchestratorService;
import com.kv.distributedkv.services.OrchestratorServiceHelper;
import com.kv.distributedkv.utils.JsonConverter;
import com.kv.distributedkv.utils.KVUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Component
public class UpdateAllNodesAboutAvailableNodes {

    @Autowired
    private OrchestratorServiceHelper orchestratorServiceHelper;

    @Autowired
    private OrchestratorService orchestratorService;

    @Scheduled(fixedDelay = 10000)
    public void updateAllNodes() {
        try {
            // Only run this task if this is an orchestrator
            Node orchestrator = orchestratorService.getOrchestrator();
            String thisHostName = InetAddress.getLocalHost().getHostAddress();
            String thisPort = System.getProperty("server.port");
            if(orchestrator == null) {
                return;
            }
            if(!orchestrator.getIp().equalsIgnoreCase(thisHostName) || !orchestrator.getPort().equalsIgnoreCase(thisPort)) {
                return;
            }

            AvailableNodes availableNodes = orchestratorService.getAvailableNodes();
            AvailableNodes realAvailableNodes = new AvailableNodes();
            List<Node> nodes = new ArrayList<>();
            for (Node node : availableNodes.getAllNodes()) {
                boolean isHealthy = orchestratorServiceHelper.checkHealth(node.getIp(), node.getPort());
                if (isHealthy) {
                    nodes.add(node);
                }
            }
            realAvailableNodes.setAllNodes(nodes);
            KVUtil.log(String.format("ScheduleTask running::: New healthy nodes are: %s", JsonConverter.convertObjectToJsonSafe(realAvailableNodes)));
            orchestratorServiceHelper.notifyAllNodesSync(realAvailableNodes);
        } catch (UnknownHostException e) {

        }
    }
}
