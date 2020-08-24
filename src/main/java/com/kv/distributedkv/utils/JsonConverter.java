package com.kv.distributedkv.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

public class JsonConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }

    public static <T> String convertObjectToJson(T object) throws JsonProcessingException {
        ObjectWriter writer = OBJECT_MAPPER.writer();
        String jsonString;
        jsonString = writer.writeValueAsString(object);
        return jsonString;
    }

    public static <T> String convertObjectToJsonSafe(T object) {
        ObjectWriter writer = OBJECT_MAPPER.writer();
        try {
            return writer.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            KVUtil.log(String.format("Error in converting object to json"), e);
        }
        return null;
    }

    public static <T> T convertJsonToObject(String json, Class<T> type)
            throws IOException {
        if (StringUtils.isNotEmpty(json)) {
            T object = OBJECT_MAPPER.readValue(json, type);
            return object;
        }
        return null;
    }

    public static <T> T convertJsonToObjectSafe(String json, Class<T> type) {
        if (StringUtils.isNotEmpty(json)) {
            try {
                return OBJECT_MAPPER.readValue(json, type);
            } catch (IOException e) {
                KVUtil.log(String.format("Error in converting object to json"), e);
            }
        }
        return null;
    }
}
