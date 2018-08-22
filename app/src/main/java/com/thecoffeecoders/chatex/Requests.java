package com.thecoffeecoders.chatex;

/**
 * Created by bikalpa on 1/8/2018.
 */

public class Requests {
    public String request_type;

    public Requests(){

    }

    public Requests(String status) {
        this.request_type = status;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String  status) {
        this.request_type = status;
    }
}
