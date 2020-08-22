package com.kv.distributedkv.dtos;

public class ErrorDetails {
    private String errorMessage;
    private int errorSystemCode;
    private String errorDescription;

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

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}
