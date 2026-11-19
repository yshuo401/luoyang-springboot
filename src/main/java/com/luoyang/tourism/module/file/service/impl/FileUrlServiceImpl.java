package com.luoyang.tourism.module.file.service.impl;

import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.config.AppProperties;
import com.luoyang.tourism.integration.oss.OssStorageService;
import com.luoyang.tourism.module.file.mapper.FileMapper;
import com.luoyang.tourism.module.file.pojo.entity.FileObject;
import com.luoyang.tourism.module.file.service.FileUrlService;
import org.springframework.stereotype.Service;

/**
 * FileUrlServiceImpl 业务实现。
 * <p>负责事务、业务规则、逻辑外键校验和外部服务编排。</p>
 */
@Service
public class FileUrlServiceImpl implements FileUrlService {
    private final FileMapper fileMapper;
    private final OssStorageService storageService;
    private final AppProperties properties;

    public FileUrlServiceImpl(FileMapper fileMapper, OssStorageService storageService, AppProperties properties) {
        this.fileMapper = fileMapper;
        this.storageService = storageService;
        this.properties = properties;
    }

@Override
public String signedUrl(Long fileId) {
        if (fileId == null) {
            return null;
        }
        FileObject file = fileMapper.selectById(fileId);
        if (file == null || file.getStatus() == null || file.getStatus() != 1) {
            return null;
        }
        return signedUrl(file);
    }

@Override
public String signedUrl(FileObject file) {
        if (file == null || !storageService.isAvailable()) {
            return null;
        }
        return storageService.signedUrl(file.getObjectKey(), properties.getOss().getSignedUrlExpirySeconds());
    }

@Override
public String signedUrl(String objectKey, long expiresSeconds) {
        return storageService.signedUrl(objectKey, expiresSeconds);
    }

@Override
public FileObject requireUsable(Long fileId) {
        FileObject file = fileMapper.selectById(fileId);
        if (file == null || file.getStatus() == null || file.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "文件不存在或尚未上传完成");
        }
        return file;
    }
}
