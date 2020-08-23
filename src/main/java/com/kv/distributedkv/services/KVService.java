package com.kv.distributedkv.services;

import com.kv.distributedkv.dtos.ErrorDetails;
import com.kv.distributedkv.dtos.KVResponse;
import com.kv.distributedkv.dtos.ResponseStatus;
import com.kv.distributedkv.dtos.ServicePhysicalNode;
import com.kv.distributedkv.rest.RESTCall;
import com.kv.distributedkv.utils.JsonConverter;
import com.kv.distributedkv.utils.KVUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KVService {

    private Map<String, String> data = new HashMap<>();

    @Autowired
    private OrchestratorService orchestratorService;

    @Autowired
    private RESTCall restCall;

    public KVResponse get(String key) {
        KVResponse kvResponse = new KVResponse();
        try {
            ServicePhysicalNode node = orchestratorService.getConsistentHash().routeNode(key);
            Pair<String, String> ipPortPair = KVUtil.getIPAndPort();
            String thisHostName = ipPortPair.getLeft();
            String thisPort = ipPortPair.getRight();
            if (node.getPort().equalsIgnoreCase(thisPort) && thisHostName.equalsIgnoreCase(node.getIp())) {
                String value = data.get(key);
                if(value == null) {
                    throw new RuntimeException("Value is not found");
                }
                kvResponse.setKey(key);
                kvResponse.setValue(value);
                kvResponse.setMetaData(JsonConverter.convertObjectToJsonSafe(node));
            } else {
                // Rest call
                return restCall.getDataFromNode(key, node);
            }
            kvResponse.setStatus(ResponseStatus.SUCCESS);
        } catch (Exception e) {
            List<ErrorDetails> errors = new ArrayList<>();
            ErrorDetails errorDetails = new ErrorDetails();
            errorDetails.setErrorMessage(e.getMessage());
            errorDetails.setErrorSystemCode(500);
            errorDetails.setErrorDescription("Error");
            errors.add(errorDetails);
            kvResponse.setErrors(errors);
            kvResponse.setStatus(ResponseStatus.FAILED);
        }
        return kvResponse;
    }

    public KVResponse post(String key, String payload) {
        KVResponse kvResponse = new KVResponse();
        try {
            ServicePhysicalNode node = orchestratorService.getConsistentHash().routeNode(key);
            Pair<String, String> ipPortPair = KVUtil.getIPAndPort();
            String thisHostName = ipPortPair.getLeft();
            String thisPort = ipPortPair.getRight();
            if (node.getPort().equalsIgnoreCase(thisPort) && thisHostName.equalsIgnoreCase(node.getIp())) {
                data.put(key, payload);
                kvResponse.setValue(payload);
                kvResponse.setKey(key);
                kvResponse.setMetaData(JsonConverter.convertObjectToJsonSafe(node));
            } else {
                // Rest call
                return restCall.postDataToNode(key, payload, node);
            }
            kvResponse.setStatus(ResponseStatus.SUCCESS);
        } catch (Exception e) {
            List<ErrorDetails> errors = new ArrayList<>();
            ErrorDetails errorDetails = new ErrorDetails();
            errorDetails.setErrorMessage(e.getMessage());
            errorDetails.setErrorSystemCode(500);
            errorDetails.setErrorDescription("Error");
            errors.add(errorDetails);
            kvResponse.setErrors(errors);
            kvResponse.setStatus(ResponseStatus.FAILED);
        }
        return kvResponse;
    }

    public KVResponse put(String key, String data) {
        return null;
    }
}
