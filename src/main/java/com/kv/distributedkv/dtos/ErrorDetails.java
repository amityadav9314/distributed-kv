package com.kv.distributedkv.dtos;

public class ErrorDetails {
    private String errorMessage;
    private int errorSystemCode;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getErrorSystemCode() {
        return errorSystemCode;
    }

    public void setErrorSystemCode(int errorSystemCode) {
        this.errorSystemCode = errorSystemCode;
    }
}
