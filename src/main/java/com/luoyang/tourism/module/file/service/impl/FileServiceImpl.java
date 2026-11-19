package com.luoyang.tourism.module.file.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.Paging;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.config.AppProperties;
import com.luoyang.tourism.integration.oss.OssStorageService;
import com.luoyang.tourism.module.file.mapper.FileMapper;
import com.luoyang.tourism.module.file.pojo.entity.FileObject;
import com.luoyang.tourism.module.file.pojo.vo.FileVO;
import com.luoyang.tourism.module.file.service.FileService;
import com.luoyang.tourism.module.file.service.FileUrlService;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * FileServiceImpl 业务实现。
 * <p>负责事务、业务规则、逻辑外键校验和外部服务编排。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of("pdf", "docx", "md", "txt");
    private static final Set<String> SCENES = Set.of("SPOT", "GUIDE", "FOOD", "BANNER", "AVATAR", "KNOWLEDGE");

    private final FileMapper fileMapper;
    private final FileUrlService fileUrlService;
    private final OssStorageService storageService;
    private final AppProperties properties;

@Override
public FileVO upload(MultipartFile upload, String bizScene, Integer visibility,
                         int uploadedByType, long uploadedBy) {
        String scene = normalizeScene(bizScene);
        if (!storageService.isAvailable()) {
            throw new BizException(ResultCode.OSS_UNAVAILABLE);
        }
        if (upload == null || upload.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }

        String originalName = cleanName(upload.getOriginalFilename());
        String extension = extension(originalName);
        boolean knowledge = "KNOWLEDGE".equals(scene);
        if (knowledge ? !DOCUMENT_EXTENSIONS.contains(extension) : !IMAGE_EXTENSIONS.contains(extension)) {
            throw new BizException(ResultCode.BAD_REQUEST, knowledge ? "不支持的文档格式" : "仅支持 JPG/PNG/WEBP/GIF 图片");
        }

        byte[] bytes;
        try {
            bytes = upload.getBytes();
        } catch (IOException exception) {
            throw new BizException(ResultCode.BAD_REQUEST, "文件读取失败");
        }
        long maxBytes = knowledge ? properties.getOss().getMaxDocumentBytes() : properties.getOss().getMaxImageBytes();
        if (bytes.length == 0 || bytes.length > maxBytes) {
            throw new BizException(ResultCode.BAD_REQUEST, "文件大小超出限制");
        }

        // 同时校验扩展名、MIME 和文件头，降低伪装文件上传风险。
        String contentType = contentType(extension);
        validateMagic(bytes, extension, contentType, upload.getContentType());
        String objectKey = objectKey(scene, extension);
        OssStorageService.UploadResult uploadResult = storageService.upload(objectKey, contentType, bytes);

        FileObject file = new FileObject();
        file.setBizScene(scene);
        file.setOriginalName(originalName);
        file.setObjectKey(objectKey);
        file.setBucket(storageService.bucket());
        file.setContentType(contentType);
        file.setExtension(extension);
        file.setFileSize((long) bytes.length);
        file.setSha256(sha256(bytes));
        file.setEtag(uploadResult.etag());
        file.setProvider("OSS");
        file.setVisibility(visibility == null || visibility != 2 ? 1 : 2);
        file.setStatus(1);
        file.setUploadedByType(uploadedByType);
        file.setUploadedBy(uploadedBy);
        file.setMetadata("{}");
        // OSS 已上传成功，数据库写入失败时补偿删除对象，避免产生孤儿文件。
        try {
            fileMapper.insert(file);
        } catch (RuntimeException exception) {
            try {
                storageService.delete(objectKey);
            } catch (RuntimeException cleanupFailure) {
                log.warn("failed to clean up orphan OSS object after database error");
            }
            throw exception;
        }
        return toVo(file);
    }

@Override
public PageResult<FileVO> list(String bizScene, Integer status, Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        List<FileVO> records = fileMapper.selectFiles(bizScene, status, Paging.offset(safePage, safeSize), safeSize)
                .stream().map(this::toVo).toList();
        return Paging.of(records, fileMapper.countFiles(bizScene, status), safePage, safeSize);
    }

@Override
public String url(Long id, Integer expiresSeconds) {
        FileObject file = fileMapper.selectById(id);
        if (file == null) {
            throw BizException.notFound();
        }
        if (!storageService.isAvailable()) {
            throw new BizException(ResultCode.OSS_UNAVAILABLE);
        }
        long ttl = expiresSeconds == null ? 1800 : Math.min(Math.max(expiresSeconds, 60), 86400);
        return fileUrlService.signedUrl(file.getObjectKey(), ttl);
    }

@Override
public void delete(Long id) {
        FileObject file = fileMapper.selectById(id);
        if (file == null) {
            return;
        }
        if (fileMapper.countReferences(id) > 0) {
            throw new BizException(ResultCode.UNIQUE_CONFLICT, "文件仍被业务内容引用，不能删除");
        }
        try {
            storageService.delete(file.getObjectKey());
        } catch (RuntimeException exception) {
            fileMapper.updateStatus(id, 2);
            throw new BizException(ResultCode.OSS_UNAVAILABLE);
        }
        fileMapper.softDelete(id);
    }

    private FileVO toVo(FileObject file) {
        return new FileVO(Long.toString(file.getId()), file.getOriginalName(), file.getContentType(),
                file.getFileSize(), file.getStatus(), fileUrlService.signedUrl(file));
    }

    private String normalizeScene(String scene) {
        String value = scene == null ? "" : scene.trim().toUpperCase(Locale.ROOT);
        if (!SCENES.contains(value)) {
            throw new BizException(ResultCode.BAD_REQUEST, "不支持的文件业务场景");
        }
        return value;
    }

    private String cleanName(String name) {
        if (name == null || name.isBlank()) {
            return "upload";
        }
        String clean = name.replace('\\', '/');
        clean = clean.substring(clean.lastIndexOf('/') + 1).replaceAll("[\\r\\n\"]", "");
        return clean.length() > 255 ? clean.substring(clean.length() - 255) : clean;
    }

    private String extension(String name) {
        int index = name.lastIndexOf('.');
        return index < 0 ? "" : name.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String contentType(String extension) {
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            case "pdf" -> "application/pdf";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "md" -> "text/markdown";
            case "txt" -> "text/plain";
            default -> "application/octet-stream";
        };
    }

    private void validateMagic(byte[] bytes, String extension, String expectedType, String suppliedType) {
        boolean valid = switch (extension) {
            case "jpg", "jpeg" -> startsWith(bytes, 0xFF, 0xD8, 0xFF);
            case "png" -> startsWith(bytes, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "gif" -> startsWith(bytes, 0x47, 0x49, 0x46, 0x38);
            case "webp" -> startsWith(bytes, 0x52, 0x49, 0x46, 0x46)
                    && bytes.length > 11 && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
            case "pdf" -> startsWith(bytes, 0x25, 0x50, 0x44, 0x46);
            case "docx" -> startsWith(bytes, 0x50, 0x4B, 0x03, 0x04);
            default -> true;
        };
        if (!valid) {
            throw new BizException(ResultCode.BAD_REQUEST, "文件内容与扩展名不匹配");
        }
        if (suppliedType != null && expectedType.startsWith("image/") && !suppliedType.startsWith("image/")) {
            throw new BizException(ResultCode.BAD_REQUEST, "文件 MIME 类型不正确");
        }
    }

    private boolean startsWith(byte[] bytes, int... prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if ((bytes[i] & 0xFF) != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private String objectKey(String scene, String extension) {
        return scene.toLowerCase(Locale.ROOT) + "/" + LocalDate.now() + "/" + UlidCreator.getUlid() + "." + extension;
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
