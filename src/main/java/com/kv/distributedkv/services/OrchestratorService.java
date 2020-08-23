package com.kv.distributedkv.services;

import com.kv.distributedkv.constants.KVUrl;
import com.kv.distributedkv.dtos.AvailableNodes;
import com.kv.distributedkv.dtos.ServicePhysicalNode;
import com.kv.distributedkv.hash.ConsistentHash;
import com.kv.distributedkv.utils.KVUtil;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OrchestratorService {

    @Autowired
    private OkHttpClient httpClient;

    @Autowired
    private OrchestratorServiceHelper orchestratorServiceHelper;

    private AvailableNodes availableNodes = new AvailableNodes();
    private ServicePhysicalNode orchestrator;
    private ConsistentHash consistentHash;

    public AvailableNodes doRegister(String ip, String port) {
        String nodeName = String.format("%s:%s", ip, port);
        String md5HashStr = KVUtil.getMd5(nodeName);
        orchestratorServiceHelper.addANewNode(ip, port, md5HashStr, false, availableNodes);
        // orchestratorServiceHelper.notifyAllNodesAsync(availableNodes);
        return availableNodes;
    }

    public void setOrchestrator(String ip, String port) {
        String nodeName = String.format("%s:%s", ip, port);
        String md5HashStr = KVUtil.getMd5(nodeName);
        orchestratorServiceHelper.addANewNode(ip, port, md5HashStr, true, availableNodes);
        orchestrator = new ServicePhysicalNode(ip, port, md5HashStr, true);
    }

    public void callOrchestratorToRegister(String ip, String port) {
        String url = String.format("http://%s:%s%s?ip=%s&port=%s", orchestrator.getIp(), orchestrator.getPort(), KVUrl.REGISTER_A_NODE, ip, port);
        KVUtil.log(String.format("Calling orchestrator on url: %s to register", url));
        // REST api call to url to register this node as available node.
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                KVUtil.log(String.format("Failed while registering to orchestrator"));
                System.exit(1);
            } else {
                // Update availableNodes
                // availableNodes = JsonConverter.convertJsonToObject(response.body().string(), AvailableNodes.class);
                KVUtil.log(String.format("%s:%s is successfully registered to orchestrator", ip, port));
                // KVUtil.log(String.format("New available nodes are: %s", JsonConverter.convertObjectToJsonSafe(availableNodes)));
            }
        } catch (IOException e) {
            KVUtil.log(String.format("Failed while registering to orchestrator"), e);
            System.exit(1);
        }
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
}
