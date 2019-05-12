package com.lhl.springframework.handler;

import java.io.Serializable;
import java.lang.reflect.Method;

public class Handler implements Serializable {

    private Object controller;

    private Method method;

    public Handler() {
    }

    public Handler(Object controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
