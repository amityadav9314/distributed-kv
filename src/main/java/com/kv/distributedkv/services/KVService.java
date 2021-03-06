package com.kv.distributedkv.services;

import com.kv.distributedkv.dtos.KVResponse;
import com.kv.distributedkv.dtos.ReplicationStrategy;
import com.kv.distributedkv.dtos.ResponseStatus;
import com.kv.distributedkv.dtos.ServicePhysicalNode;
import com.kv.distributedkv.rest.RESTCall;
import com.kv.distributedkv.utils.JsonConverter;
import com.kv.distributedkv.utils.KVUtil;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class KVService {
    @Value("${replication.factor}")
    private static int replicationFactor;

    @Value("${replication.strategy}")
    private static ReplicationStrategy replicationStrategy;

    @Autowired
    public KVService(
            @Value("${replication.factor}") int replicationFactor,
            @Value("${replication.strategy}") ReplicationStrategy replicationStrategy
    ) {
        KVService.replicationFactor = replicationFactor;
        KVService.replicationStrategy = replicationStrategy;
    }

    // TODO - amit it is a good idea to store replicated data into another map
    // private Map<String, String> replicatedData = new HashMap<>();
    private final Map<String, String> data = new HashMap<>();

    @Autowired
    private OrchestratorService orchestratorService;

    @Autowired
    private RESTCall restCall;

    public KVResponse get(String key) {
        KVResponse kvResponse = new KVResponse();
        try {
            ServicePhysicalNode primaryNode = orchestratorService.getConsistentHash().getPrimaryNodeOfKey(key);
            KVUtil.log(String.format("Primary node for this key: %s is %s:%s", key, primaryNode.getIp(), primaryNode.getPort()));
            Pair<String, String> ipPortPair = KVUtil.getIPAndPort();
            String thisHostName = ipPortPair.getLeft();
            String thisPort = ipPortPair.getRight();
            KVUtil.log(String.format("This hostname and port is: %s:%s", thisHostName, thisPort));
            if (primaryNode.getPort().equalsIgnoreCase(thisPort) && thisHostName.equalsIgnoreCase(primaryNode.getIp())) {
                String value = data.get(key);
                if (value == null) {
                    throw new RuntimeException("Value is not found");
                }
                kvResponse.setKey(key);
                kvResponse.setValue(value);
            } else {
                // Rest call
                return restCall.getDataFromNode(key, primaryNode, replicationFactor);
            }
            kvResponse.setStatus(ResponseStatus.SUCCESS);
        } catch (Exception e) {
            String msg = String.format("Error in getting data on PRIMARY URL for key: %s", key);
            // KVUtil.log(msg, e);
            KVUtil.log(msg);
            return KVUtil.getErrorBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
        }
        return kvResponse;
    }

    public KVResponse getFromANode(String key) {
        KVResponse kvResponse = new KVResponse();
        try {
            String value = data.get(key);
            if (value == null) {
                throw new RuntimeException("Value is not found");
            }
            kvResponse.setKey(key);
            kvResponse.setValue(value);
            kvResponse.setStatus(ResponseStatus.SUCCESS);
            return kvResponse;
        } catch (Exception e) {
            String msg = String.format("Error in getting data for key: %s", key);
            KVUtil.log(msg, e);
            return KVUtil.getErrorBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
        }
    }

    public KVResponse post(String key, String payload, Integer replication) {
        KVResponse kvResponse = new KVResponse();
        try {
            ServicePhysicalNode node;
            if (replication == null) {
                node = orchestratorService.getConsistentHash().getPrimaryNodeOfKey(key);
            } else {
                node = orchestratorService.getConsistentHash().getNthSecondaryNodeOfKey(key, replication);
            }
            Pair<String, String> ipPortPair = KVUtil.getIPAndPort();
            String thisHostName = ipPortPair.getLeft();
            String thisPort = ipPortPair.getRight();

            KVUtil.log(String.format("Saving data for key: `%s` on %s:%s", key, node.getIp(), node.getPort()));
            if (node.getPort().equalsIgnoreCase(thisPort) && thisHostName.equalsIgnoreCase(node.getIp())) {
                if (data.containsKey(key)) {
                    throw new RuntimeException("Data already exists.");
                }
                data.put(key, payload);
                kvResponse.setValue(payload);
                kvResponse.setKey(key);
            } else {
                // Rest call
                return restCall.postDataToNode(key, payload, node, replication);
            }
            kvResponse.setStatus(ResponseStatus.SUCCESS);
            if (replication == null) {
                replicateData(key, payload, replicationFactor, replicationStrategy);
            }
        } catch (Exception e) {
            KVUtil.log("Error in posting data", e);
            return KVUtil.getErrorBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
        }
        return kvResponse;
    }

    private void replicateData(String key, String payload, int replicationFactor, ReplicationStrategy replicationStrategy) {
        if (replicationStrategy == ReplicationStrategy.ASYNC) {
            Observable.fromCallable(() -> {
                doReplicateData(key, payload, replicationFactor);
                return 1;
            }).subscribeOn(Schedulers.io()).subscribe();
        } else if (replicationStrategy == ReplicationStrategy.SYNC) {
            doReplicateData(key, payload, replicationFactor);
        } else {
            throw new NotImplementedException();
        }
    }

    private void doReplicateData(String key, String payload, int replicationFactor) {
        boolean isReplicationPossible = orchestratorService.getAvailableNodes().getAllNodes().size() > replicationFactor;
        if (!isReplicationPossible) {
            return;
        }
        for (int i = 1; i <= replicationFactor; i++) {
            ServicePhysicalNode node = orchestratorService.getConsistentHash().getNthSecondaryNodeOfKey(key, i);
            String msg = String.format("Replicating key: `%s` on %s:%s", key, node.getIp(), node.getPort());
            KVUtil.log(msg);
            KVResponse r = restCall.postDataToNode(key, payload, node, i);
            KVUtil.log(String.format("Replication of key: `%s` on %s:%s IS DONE....%s", key, node.getIp(), node.getPort(), JsonConverter.convertObjectToJsonSafe(r)));
        }
    }

    public KVResponse put(String key, String data) {
        throw new NotImplementedException();
    }

    public Map<String, String> getData() {
        return data;
    }
}
