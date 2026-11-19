package com.luoyang.tourism.config.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.luoyang.tourism.config.AppProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfig {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = "app.oss", name = "enabled", havingValue = "true")
    public OSS ossClient(AppProperties properties) {
        return new OSSClientBuilder().build(properties.getOss().getEndpoint(),
                properties.getOss().getAccessKeyId(), properties.getOss().getAccessKeySecret());
    }
}
