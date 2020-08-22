package com.kv.distributedkv.services;

import com.kv.distributedkv.constants.KVUrl;
import com.kv.distributedkv.dtos.AvailableNodes;
import com.kv.distributedkv.dtos.Node;
import com.kv.distributedkv.utils.JsonConverter;
import com.kv.distributedkv.utils.KVUtil;
import com.squareup.okhttp.*;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OrchestratorServiceHelper {

    @Autowired
    private OkHttpClient httpClient;

    protected boolean isNodeAlreadyAdded(String ip, String port, String md5HashStr, AvailableNodes availableNodes) {
        boolean nodeAlreadyAdded = false;
        for (Node node : availableNodes.getAllNodes()) {
            if (node.getMd5Hash().equalsIgnoreCase(md5HashStr)) {
                nodeAlreadyAdded = true;
                KVUtil.log(String.format("%s:%s is already in available nodes, so not storing again", ip, port));
                break;
            }
        }
        return nodeAlreadyAdded;
    }

    protected AvailableNodes addANewNode(String ip, String port, String md5HashStr, boolean isOrchestrator, AvailableNodes availableNodes) {
        boolean nodeAlreadyAdded = isNodeAlreadyAdded(ip, port, md5HashStr, availableNodes);
        if (!nodeAlreadyAdded) {
            Node newNode = new Node(ip, port, md5HashStr, isOrchestrator);
            availableNodes.getAllNodes().add(newNode);
        }
        return availableNodes;
    }

    protected void notifyAllNodesAsync(AvailableNodes availableNodes) {
        // todo - amit use threads to notify
        Observable.fromCallable(() -> {
            doNotifyAllNodes(availableNodes);
            return 1;
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void notifyAllNodesSync(AvailableNodes availableNodes) {
        doNotifyAllNodes(availableNodes);
    }

    private void doNotifyAllNodes(AvailableNodes availableNodes) {
        try {
            String availableNodesJson = JsonConverter.convertObjectToJsonSafe(availableNodes);
            // todo - amit use threads to notify
            for (Node node : availableNodes.getAllNodes()) {
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkHealth(String ip, String port) {
        try {
            String url = String.format("http://%s:%s%s", ip, port, KVUrl.HEALTH);
            Request request = new Request.Builder().url(url).build();
            Response response = httpClient.newCall(request).execute();
            return response.isSuccessful();
        } catch (IOException e) {
            return false;
        }
    }
}
