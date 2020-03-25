package com.wbc.utilserver.model;

import com.google.gson.Gson;

public class ErrorInfo {
    private String result;
    private String errorText;

    public ErrorInfo() {
        result="ok";
        errorText="success";
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson( this );
    }

    public boolean isOK() {
        if ( result.equals("ok") ) return true;
        return false;
    }
}
