package com.lhl.springframework.dispatch;

import com.lhl.springframework.annotation.*;
import com.lhl.springframework.handler.Handler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class DispatcherServlet extends HttpServlet {

    private Properties contextConfig = new Properties();

    private List<String> classNames = new ArrayList<>();

    private Map<String, Object> ioc = new HashMap<>();

    private Map<String, Handler> handlerMapping = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 等待请求
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception");
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        System.out.println(url);
        String contextPath = req.getContextPath();
        System.out.println(contextPath);
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        System.out.println(url);

        if (!handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found");
        }

        Handler handler = handlerMapping.get(url);
        if (handler == null) {
            resp.getWriter().write("404 Not Found");
        }
        Method method = handler.getMethod();
        // 获取请求参数
        Map parameterMap = req.getParameterMap();
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> parameterType : parameterTypes) {
            if (parameterType.isAnnotationPresent(RequestParam.class)) {
                RequestParam annotation = parameterType.getAnnotation(RequestParam.class);
                String paramName = annotation.value();
                // todo
            }
        }
        method.invoke(handler.getController(), null);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        // 扫描所有相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
        // 初始化所有相关的类
        doInstance();
        // 自动注入
        doAutowired();
        // -------spring初始化完成-----------
        // 初始化handlerMapping
        initHandlerMapping();
        System.out.println("spring init");
        // doPost
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        try {
            File classDir = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
            for (File file : classDir.listFiles()) {
                if (file.isDirectory()) {
                    doScanner(scanPackage + "." + file.getName());
                } else {
                    String className = scanPackage + "." + file.getName().replace(".class", "");
                    classNames.add(className);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }

        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                // 不是所有的类都实例化的 只实例化加了注解的
                if (clazz.isAnnotationPresent(Controller.class)) {
                    // key 默认类名首字母小写
                    String beanName = lowerFirstCase(clazz.getName());
                    ioc.put(beanName, clazz.newInstance());
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    // 自定义名字;
                    Service service = clazz.getAnnotation(Service.class);
                    String beanName = service.value();
                    if ("".equals(beanName)) {
                        // key 默认采用首字母小写;
                        beanName = lowerFirstCase(clazz.getName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);

                    // 根据接口类型赋值
                    for (Class<?> i : clazz.getInterfaces()) {
                        ioc.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }

        // 循环ioc容器中所有的类 对需要自动赋值的字段或者属性进行赋值
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // 获取注解类的属性
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    Autowired annotation = field.getAnnotation(Autowired.class);
                    String beanName = annotation.value();
                    if ("".equals(beanName)) {
                        beanName = field.getType().getName();
                    }
                    // 暴力访问
                    field.setAccessible(true);
                    try {
                        field.set(entry.getValue(), ioc.get(beanName));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()) return;

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();

            if (clazz.isAnnotationPresent(Controller.class)) {
                String baseUrl = "";
                if (clazz.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping annotation = clazz.getAnnotation(RequestMapping.class);
                    baseUrl = annotation.value();
                }
                for (Method method : clazz.getMethods()) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                        String url = ("/" + baseUrl + annotation.value()).replaceAll("//", "/");
                        handlerMapping.put(url, new Handler(entry.getValue(), method));
                        System.out.println("mapping: " + url + method.getName());
                    }
                }
            }
        }
    }


    private String lowerFirstCase(String className) {
        char[] array = className.toCharArray();
        array[0] += 32;
        return array.toString();
    }
}
