package cn.nullcat.sckj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("cn.nullcat.sckj.mapper")
//@EnableScheduling  // 开启定时任务
public class SckjApplication {

    public static void main(String[] args) {
        SpringApplication.run(SckjApplication.class, args);
    }

}
