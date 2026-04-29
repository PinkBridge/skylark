package cn.skylark.aiot_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import cn.skylark.aiot_service.iot.access.config.IotAccessProperties;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan(basePackages = "cn.skylark.aiot_service.**.mapper")
@EnableConfigurationProperties({IotAccessProperties.class})
public class AiotServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(AiotServiceApplication.class, args);
  }
}

