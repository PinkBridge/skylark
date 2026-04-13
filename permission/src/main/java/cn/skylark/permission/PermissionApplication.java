package cn.skylark.permission;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author yaomianwei
 */
@SpringBootApplication
@EnableScheduling
@MapperScan({"cn.skylark.permission.authentication.mapper", 
"cn.skylark.permission.authorization.mapper"})
public class PermissionApplication {
  public static void main(String[] args) {
    SpringApplication.run(PermissionApplication.class, args);
  }

}
