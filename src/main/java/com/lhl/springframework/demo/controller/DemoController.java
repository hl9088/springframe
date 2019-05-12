package com.lhl.springframework.demo.controller;

import com.lhl.springframework.annotation.Autowired;
import com.lhl.springframework.annotation.Controller;
import com.lhl.springframework.annotation.RequestMapping;
import com.lhl.springframework.annotation.RequestParam;
import com.lhl.springframework.demo.service.DemoService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class DemoController {

    @Autowired
    private DemoService demoService;

    @RequestMapping("/demo")
    public void demo(@RequestParam("demo") String demo, HttpServletResponse response){
        String resp = demoService.work(demo);
        System.out.println(resp);
        try {
            response.getWriter().write(resp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
