package com.kv.distributedkv.utils;

import com.kv.distributedkv.dtos.BaseResponse;
import com.kv.distributedkv.dtos.ErrorDetails;
import com.kv.distributedkv.dtos.KVResponse;
import com.kv.distributedkv.dtos.ResponseStatus;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class KVUtil {

    @Value("${log.type}")
    private static String logType;

    @Autowired
    public KVUtil(@Value("${log.type}") String logType) {
        this.logType = logType;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(KVUtil.class);

    public static String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void log(String msg) {
        if (logType.equalsIgnoreCase("system")) {
            System.out.println(msg);
        } else if (logType.equalsIgnoreCase("log4j")) {
            LOGGER.info(msg);
        }
    }

    public static void log(String msg, Exception e) {
        if (logType.equalsIgnoreCase("system")) {
            System.out.println(msg);
            e.printStackTrace();
        } else if (logType.equalsIgnoreCase("log4j")) {
            LOGGER.error(msg, e);
        }
    }

    public static Pair<String, String> getIPAndPort() throws UnknownHostException {
        String thisHostName = InetAddress.getLocalHost().getHostAddress();
        String thisPort = System.getProperty("server.port");
        return Pair.of(thisHostName, thisPort);
    }

    public static KVResponse getErrorBaseResponse(int systemCode, Exception e) {
        KVResponse kvResponse = new KVResponse();
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setErrorMessage(e.getMessage());
        errorDetails.setErrorSystemCode(systemCode);
        kvResponse.setError(errorDetails);
        kvResponse.setStatus(ResponseStatus.FAILED);
        return kvResponse;
    }
}
