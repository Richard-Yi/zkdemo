package com.ric.studio.zkdemo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * commandLineRunner 启动加载类
 * 实现了CommandLineRunner接口的Component会在所有Spring Beans都初始化之后，SpringApplication.run()之前执行，
 * 这个run 方法会在容器实体全部初始化完成后执行
 */
@SpringBootApplication
public class ZkdemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZkdemoApplication.class, args);
    }

}
