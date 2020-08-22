package com.kv.distributedkv.dtos;

import java.util.List;

public class BaseResponse {
    private ResponseStatus status;
    private List<ErrorDetails> errors;

    public List<ErrorDetails> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorDetails> errors) {
        this.errors = errors;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }
}
