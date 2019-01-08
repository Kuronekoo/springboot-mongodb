package cn.kuroneko.demo.springbootmongodb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude
        = {DataSourceAutoConfiguration.class})
public class MongoDemoApp {
    public static void main(String[] args) {
        SpringApplication.run(MongoDemoApp.class, args);
    }
}
