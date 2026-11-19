package com.luoyang.tourism.module.file.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.file.pojo.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * FileService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */

public interface FileService {
    /** 上传文件或文档。 */
    public FileVO upload(MultipartFile upload, String bizScene, Integer visibility, int uploadedByType, long uploadedBy);

    /** 分页查询数据列表。 */
    public PageResult<FileVO> list(String bizScene, Integer status, Integer page, Integer pageSize);

    /** 执行 url 业务操作。 */
    public String url(Long id, Integer expiresSeconds);

    /** 删除数据。 */
    public void delete(Long id);
}
