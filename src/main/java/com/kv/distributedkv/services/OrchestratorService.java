package com.kv.distributedkv.services;

import com.kv.distributedkv.dtos.AvailableNodes;
import com.kv.distributedkv.dtos.ServicePhysicalNode;
import com.kv.distributedkv.hash.ConsistentHash;
import com.kv.distributedkv.utils.KVUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrchestratorService {

    @Value("${replication.factor}")
    private static int replicationFactor;

    @Autowired
    public OrchestratorService(
            @Value("${replication.factor}") int replicationFactor
    ) {
        OrchestratorService.replicationFactor = replicationFactor;
    }

    private AvailableNodes availableNodes = new AvailableNodes();
    private ServicePhysicalNode orchestrator;
    private ConsistentHash consistentHash;

    public AvailableNodes doRegister(String ip, String port) {
        String nodeName = String.format("%s:%s", ip, port);
        String md5HashStr = KVUtil.getMd5(nodeName);
        addANewNode(ip, port, md5HashStr, false, availableNodes);
        // orchestratorServiceHelper.notifyAllNodesAsync(availableNodes);
        return availableNodes;
    }

    public void setOrchestrator(String ip, String port) {
        String nodeName = String.format("%s:%s", ip, port);
        String md5HashStr = KVUtil.getMd5(nodeName);
        addANewNode(ip, port, md5HashStr, true, availableNodes);
        orchestrator = new ServicePhysicalNode(ip, port, md5HashStr, true);
    }

    protected boolean isNodeAlreadyAdded(String ip, String port, AvailableNodes availableNodes) {
        boolean nodeAlreadyAdded = false;
        for (ServicePhysicalNode node : availableNodes.getAllNodes()) {
            if (node.getIp().equalsIgnoreCase(ip) && node.getPort().equalsIgnoreCase(port)) {
                nodeAlreadyAdded = true;
                KVUtil.log(String.format("%s:%s is already in available nodes, so not storing again", ip, port));
                break;
            }
        }
        return nodeAlreadyAdded;
    }

    protected AvailableNodes addANewNode(String ip, String port, String md5HashStr, boolean isOrchestrator, AvailableNodes availableNodes) {
        boolean nodeAlreadyAdded = isNodeAlreadyAdded(ip, port, availableNodes);
        if (!nodeAlreadyAdded) {
            ServicePhysicalNode newNode = new ServicePhysicalNode(ip, port, md5HashStr, isOrchestrator);
            availableNodes.getAllNodes().add(newNode);
        }
        resetAvailableNodes(availableNodes);
        return availableNodes;
    }

    public AvailableNodes resetAvailableNodes(AvailableNodes newAvailableNodes) {
        availableNodes = newAvailableNodes;
        consistentHash = new ConsistentHash(newAvailableNodes.getAllNodes());
        return availableNodes;
    }

    public AvailableNodes getAvailableNodes() {
        return availableNodes;
    }

    public ServicePhysicalNode getOrchestrator() {
        return orchestrator;
    }

    public ConsistentHash getConsistentHash() {
        return consistentHash;
    }

    public AvailableNodes getAvailableNodesForAKey(String key) {
        AvailableNodes allAvailableNodes = new AvailableNodes();
        List<ServicePhysicalNode> allNodes = new ArrayList<>();
        ServicePhysicalNode primaryNode = consistentHash.getPrimaryNodeOfKey(key);
        allNodes.add(primaryNode);
        int i = 1;
        while(i <= replicationFactor && availableNodes.getAllNodes().size() > replicationFactor) {
            ServicePhysicalNode secondaryNode = consistentHash.getNthSecondaryNodeOfKey(key, i);
            allNodes.add(secondaryNode);
            i++;
        }
        allAvailableNodes.setAllNodes(allNodes);
        return allAvailableNodes;
    }
}
