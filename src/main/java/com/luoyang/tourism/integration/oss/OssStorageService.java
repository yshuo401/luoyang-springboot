package com.luoyang.tourism.integration.oss;

import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyun.oss.OSS;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.config.AppProperties;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * OssStorageService 外部服务集成组件。
 * <p>集中封装第三方 HTTP、对象存储或模型服务调用。</p>
 */
@Service
public class OssStorageService {
    private final AppProperties properties;
    private final ObjectProvider<OSS> clientProvider;

    public OssStorageService(AppProperties properties, ObjectProvider<OSS> clientProvider) {
        this.properties = properties;
        this.clientProvider = clientProvider;
    }

    public boolean isAvailable() {
        return properties.getOss().isConfigured() && clientProvider.getIfAvailable() != null;
    }

    public UploadResult upload(String objectKey, String contentType, byte[] bytes) {
        OSS client = requireClient();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType(contentType);
        PutObjectResult result = client.putObject(properties.getOss().getBucket(), objectKey,
                new ByteArrayInputStream(bytes), metadata);
        return new UploadResult(result.getETag());
    }

    public String signedUrl(String objectKey, long expiresSeconds) {
        OSS client = requireClient();
        Date expiration = new Date(System.currentTimeMillis() + Math.max(60, expiresSeconds) * 1000L);
        URL url = client.generatePresignedUrl(properties.getOss().getBucket(), objectKey, expiration);
        return url.toString();
    }

    public void delete(String objectKey) {
        requireClient().deleteObject(properties.getOss().getBucket(), objectKey);
    }

    public String bucket() {
        return properties.getOss().getBucket();
    }

    private OSS requireClient() {
        OSS client = clientProvider.getIfAvailable();
        if (client == null) {
            throw new BizException(ResultCode.OSS_UNAVAILABLE);
        }
        return client;
    }

    public record UploadResult(String etag) {
    }
}
