package com.example.assignment.models;

import java.util.List;

public class ResponeDataFromMyServer {
    private List<HackNasa> data;
    private String message;
    private int status;

    public ResponeDataFromMyServer() {
    }

    public ResponeDataFromMyServer(List<HackNasa> data, String message, int status) {
        this.data = data;
        this.message = message;
        this.status = status;
    }

    public List<HackNasa> getData() {
        return data;
    }

    public void setData(List<HackNasa> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
