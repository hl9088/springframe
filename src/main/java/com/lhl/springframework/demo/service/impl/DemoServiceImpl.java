package com.lhl.springframework.demo.service.impl;

import com.lhl.springframework.annotation.Service;
import com.lhl.springframework.demo.service.DemoService;

@Service
public class DemoServiceImpl implements DemoService {

    @Override
    public String work(String demo) {
        return demo;
    }
}
