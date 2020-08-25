package com.kv.distributedkv.tasks;

import com.kv.distributedkv.dtos.AvailableNodes;
import com.kv.distributedkv.dtos.ServicePhysicalNode;
import com.kv.distributedkv.rest.RESTCall;
import com.kv.distributedkv.services.OrchestratorService;
import com.kv.distributedkv.utils.JsonConverter;
import com.kv.distributedkv.utils.KVUtil;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class UpdateAllNodesAboutAvailableNodes {

    @Autowired
    private RESTCall restCall;

    @Autowired
    private OrchestratorService orchestratorService;

    @Scheduled(fixedDelay = 10000)
    public void updateAllNodes() {
        try {
            // Only run this task if this is an orchestrator
            ServicePhysicalNode orchestrator = orchestratorService.getOrchestrator();
            Pair<String, String> ipPortPair = KVUtil.getIPAndPort();
            String thisHostName = ipPortPair.getLeft();
            String thisPort = ipPortPair.getRight();
            if (orchestrator == null) {
                return;
            }
            if (!orchestrator.getIp().equalsIgnoreCase(thisHostName) || !orchestrator.getPort().equalsIgnoreCase(thisPort)) {
                return;
            }

            AvailableNodes availableNodes = orchestratorService.getAvailableNodes();
            AvailableNodes realAvailableNodes = new AvailableNodes();
            List<ServicePhysicalNode> nodes = new ArrayList<>();
            for (ServicePhysicalNode node : availableNodes.getAllNodes()) {
                boolean isHealthy = restCall.checkHealth(node.getIp(), node.getPort());
                if (isHealthy) {
                    nodes.add(node);
                }
            }
            realAvailableNodes.setAllNodes(nodes);
            KVUtil.log(String.format("ScheduleTask running::: New healthy nodes are: %s", JsonConverter.convertObjectToJsonSafe(realAvailableNodes)));
            notifyAllNodesSync(realAvailableNodes);
            rebalance(availableNodes, realAvailableNodes);
        } catch (Exception e) {
            KVUtil.log("Error in updating all nodes", e);
        }
    }

    private void rebalance(AvailableNodes oldAvailableNodes, AvailableNodes newAvailableNodes) {
        Set<ServicePhysicalNode> added = getNewlyAddedNodes(oldAvailableNodes, newAvailableNodes);
        Set<ServicePhysicalNode> removed = getNewlyRemovedNodes(oldAvailableNodes, newAvailableNodes);
        rebalanceRemovedNodes(removed);
        rebalanceAddedNodes(added);
    }

    private void rebalanceAddedNodes(Set<ServicePhysicalNode> added) {
    }

    private void rebalanceRemovedNodes(Set<ServicePhysicalNode> removed) {
    }

    private Set<ServicePhysicalNode> getNewlyRemovedNodes(AvailableNodes oldAvailableNodes, AvailableNodes newAvailableNodes) {
        Set<ServicePhysicalNode> oldSet = new HashSet<>(oldAvailableNodes.getAllNodes());
        Set<ServicePhysicalNode> newSet = new HashSet<>(newAvailableNodes.getAllNodes());
        oldSet.removeAll(newSet);
        return oldSet;
    }

    private Set<ServicePhysicalNode> getNewlyAddedNodes(AvailableNodes oldAvailableNodes, AvailableNodes newAvailableNodes) {
        Set<ServicePhysicalNode> oldSet = new HashSet<>(oldAvailableNodes.getAllNodes());
        Set<ServicePhysicalNode> newSet = new HashSet<>(newAvailableNodes.getAllNodes());
        newSet.removeAll(oldSet);
        return newSet;
    }

    private void notifyAllNodesAsync(AvailableNodes availableNodes) {
        // todo - amit use threads to notify
        Observable.fromCallable(() -> {
            restCall.doNotifyAllNodes(availableNodes);
            return 1;
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    private void notifyAllNodesSync(AvailableNodes availableNodes) {
        restCall.doNotifyAllNodes(availableNodes);
    }
}
