package com.luoyang.tourism.module.file.service;

import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.config.AppProperties;
import com.luoyang.tourism.integration.oss.OssStorageService;
import com.luoyang.tourism.module.file.entity.FileObject;
import com.luoyang.tourism.module.file.mapper.FileMapper;
import org.springframework.stereotype.Service;

@Service
public class FileUrlService {
    private final FileMapper fileMapper;
    private final OssStorageService storageService;
    private final AppProperties properties;

    public FileUrlService(FileMapper fileMapper, OssStorageService storageService, AppProperties properties) {
        this.fileMapper = fileMapper;
        this.storageService = storageService;
        this.properties = properties;
    }

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

    public String signedUrl(FileObject file) {
        if (file == null || !storageService.isAvailable()) {
            return null;
        }
        return storageService.signedUrl(file.getObjectKey(), properties.getOss().getSignedUrlExpirySeconds());
    }

    public String signedUrl(String objectKey, long expiresSeconds) {
        return storageService.signedUrl(objectKey, expiresSeconds);
    }

    public FileObject requireUsable(Long fileId) {
        FileObject file = fileMapper.selectById(fileId);
        if (file == null || file.getStatus() == null || file.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "文件不存在或尚未上传完成");
        }
        return file;
    }
}
