package com.kv.distributedkv.rest;

import com.kv.distributedkv.constants.KVUrl;
import com.kv.distributedkv.dtos.AvailableNodes;
import com.kv.distributedkv.dtos.KVResponse;
import com.kv.distributedkv.dtos.ServicePhysicalNode;
import com.kv.distributedkv.services.OrchestratorService;
import com.kv.distributedkv.utils.JsonConverter;
import com.kv.distributedkv.utils.KVUtil;
import com.squareup.okhttp.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RESTCall {

    @Autowired
    private OkHttpClient httpClient;

    @Autowired
    private OrchestratorService orchestratorService;

    public KVResponse getDataFromNode(String key, ServicePhysicalNode node, int replicationFactor) {
        try {
            String url = String.format("http://%s:%s%s/%s", node.getIp(), node.getPort(), KVUrl.KV, key);
            Request request = new Request.Builder().url(url).build();
            Response response = httpClient.newCall(request).execute();
            boolean isSuccess = response.isSuccessful();
            if (isSuccess) {
                return JsonConverter.convertJsonToObjectSafe(response.body().string(), KVResponse.class);
            } else {
                int i = 1;
                while (i <= replicationFactor) {
                    ServicePhysicalNode secondaryNode = orchestratorService.getConsistentHash().getNthPrimaryNode(key, i);
                    url = String.format("http://%s:%s%s/%s", secondaryNode.getIp(), secondaryNode.getPort(), KVUrl.KV, key);
                    request = new Request.Builder().url(url).build();
                    response = httpClient.newCall(request).execute();
                    isSuccess = response.isSuccessful();
                    if (isSuccess) {
                        return JsonConverter.convertJsonToObjectSafe(response.body().string(), KVResponse.class);
                    } else {
                        i++;
                    }
                }
                throw new RuntimeException("Data can not be found");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public KVResponse postDataToNode(String key, String payload, ServicePhysicalNode node, Integer i) {
        try {
            String url;
            if(i == null) {
                url = String.format("http://%s:%s%s/%s", node.getIp(), node.getPort(), KVUrl.KV, key);
            } else {
                url = String.format("http://%s:%s%s/%s/%s", node.getIp(), node.getPort(), KVUrl.KV_REPLICATE, key, i);
            }
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    payload
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            Response response = httpClient.newCall(request).execute();
            boolean isSuccess = response.isSuccessful();
            if (isSuccess) {
                return JsonConverter.convertJsonToObjectSafe(response.body().string(), KVResponse.class);
            } else {
                throw new RuntimeException("Data can not be posted");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void callOrchestratorToRegister(String ip, String port, ServicePhysicalNode orchestrator) {
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

    public boolean checkHealth(String ip, String port) {
        try {
            String url = String.format("http://%s:%s%s", ip, port, KVUrl.HEALTH);
            Request request = new Request.Builder().url(url).build();
            Response response = httpClient.newCall(request).execute();
            boolean isSuccess = response.isSuccessful();
            response.body().string();
            return isSuccess;
        } catch (IOException e) {
            return false;
        }
    }

    public void doNotifyAllNodes(AvailableNodes availableNodes) {
        try {
            String availableNodesJson = JsonConverter.convertObjectToJsonSafe(availableNodes);
            // todo - amit use threads to notify
            for (ServicePhysicalNode node : availableNodes.getAllNodes()) {
                String url = String.format("http://%s:%s%s", node.getIp(), node.getPort(), KVUrl.CALLBACK_TO_UPDATE_NODES);
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        availableNodesJson
                );

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Response response = httpClient.newCall(request).execute();
                response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
