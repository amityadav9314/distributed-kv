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

    public KVResponse getDataFromNode(String key, ServicePhysicalNode primaryNode, int replicationFactor) {
        try {
            String httpClientResponse;
            String url = String.format("http://%s:%s%s/%s", primaryNode.getIp(), primaryNode.getPort(), KVUrl.KV_GET_FROM_NODE, key);
            KVUtil.log(String.format("Trying to get data from PRIMARY node: %s", url));
            Request request = new Request.Builder().url(url).build();
            Response response = httpClient.newCall(request).execute();
            boolean isSuccess = response.isSuccessful();
            if (isSuccess) {
                httpClientResponse = response.body().string();
                response.body().close();
                return JsonConverter.convertJsonToObjectSafe(httpClientResponse, KVResponse.class);
            } else {
                int i = 1;
                while (i <= replicationFactor && orchestratorService.getAvailableNodes().getAllNodes().size() > replicationFactor) {
                    ServicePhysicalNode secondaryNode = orchestratorService.getConsistentHash().getNthSecondaryNodeOfKey(key, i);
                    url = String.format("http://%s:%s%s/%s", secondaryNode.getIp(), secondaryNode.getPort(), KVUrl.KV_GET_FROM_NODE, key);
                    KVUtil.log(String.format("Trying to get data from SECONDARY node: %s number: %s", url, i));
                    request = new Request.Builder().url(url).build();
                    response = httpClient.newCall(request).execute();
                    isSuccess = response.isSuccessful();
                    if (isSuccess) {
                        httpClientResponse = response.body().string();
                        response.body().close();
                        return JsonConverter.convertJsonToObjectSafe(httpClientResponse, KVResponse.class);
                    } else {
                        i++;
                    }
                }
                response.body().close();
                throw new RuntimeException("Data can not be found in ANY node");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public KVResponse postDataToNode(String key, String payload, ServicePhysicalNode node, Integer i) {
        try {
            String url;
            if (i == null) {
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
                String httpClientResponse = response.body().string();
                response.body().close();
                return JsonConverter.convertJsonToObjectSafe(httpClientResponse, KVResponse.class);
            } else {
                response.body().close();
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
                response.body().close();
                System.exit(1);
            } else {
                // Update availableNodes
                // availableNodes = JsonConverter.convertJsonToObject(response.body().string(), AvailableNodes.class);
                KVUtil.log(String.format("%s:%s is successfully registered to orchestrator", ip, port));
                // KVUtil.log(String.format("New available nodes are: %s", JsonConverter.convertObjectToJsonSafe(availableNodes)));
                response.body().close();
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
            response.body().close();
            return isSuccess;
        } catch (IOException e) {
            KVUtil.log("Error in health check", e);
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
                response.body().close();
            }
        } catch (IOException e) {
            KVUtil.log("Error in notifying to all nodes", e);
        }
    }

}
