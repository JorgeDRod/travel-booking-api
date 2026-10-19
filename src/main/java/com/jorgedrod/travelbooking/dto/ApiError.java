package com.jorgedrod.travelbooking.dto;

public class ApiError {

    private int status;
    private String error;
    private String message;

    public void setStatus(int status) {
        this.status = status;
    }
    public int getStatus() {
        return this.status;
    }
    public void setError(String error) {
        this.error = error;
    }
    public String getError() {
        return this.error;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return this.message;
    }
}
