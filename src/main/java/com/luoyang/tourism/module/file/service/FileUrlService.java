package com.luoyang.tourism.module.file.service;

import com.luoyang.tourism.module.file.pojo.entity.FileObject;

/**
 * FileUrlService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */
public interface FileUrlService {
    /** 执行 signedUrl 业务操作。 */
    public String signedUrl(Long fileId);

    /** 执行 signedUrl 业务操作。 */
    public String signedUrl(FileObject file);

    /** 执行 signedUrl 业务操作。 */
    public String signedUrl(String objectKey, long expiresSeconds);

    /** 执行 requireUsable 业务操作。 */
    public FileObject requireUsable(Long fileId);
}
