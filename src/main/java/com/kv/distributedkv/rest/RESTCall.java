package com.kv.distributedkv.rest;

import com.kv.distributedkv.constants.KVUrl;
import com.kv.distributedkv.dtos.KVResponse;
import com.kv.distributedkv.dtos.ServicePhysicalNode;
import com.kv.distributedkv.utils.JsonConverter;
import com.squareup.okhttp.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RESTCall {

    @Autowired
    private OkHttpClient httpClient;

    public KVResponse getDataFromNode(String key, ServicePhysicalNode node) {
        try {
            String url = String.format("http://%s:%s%s/%s", node.getIp(), node.getPort(), KVUrl.KV, key);
            Request request = new Request.Builder().url(url).build();
            Response response = httpClient.newCall(request).execute();
            boolean isSuccess = response.isSuccessful();
            if (isSuccess) {
                return JsonConverter.convertJsonToObjectSafe(response.body().string(), KVResponse.class);
            } else {
                throw new RuntimeException("Data can not be found");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public KVResponse postDataToNode(String key, String payload, ServicePhysicalNode node) {
        try {
            String url = String.format("http://%s:%s%s/%s", node.getIp(), node.getPort(), KVUrl.KV, key);
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
}
