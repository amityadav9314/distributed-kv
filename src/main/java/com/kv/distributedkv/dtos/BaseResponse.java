package com.kv.distributedkv.dtos;

public class BaseResponse {
    private ResponseStatus status;
    private ErrorDetails error;

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }
}
