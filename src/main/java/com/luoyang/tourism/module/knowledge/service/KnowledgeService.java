package com.luoyang.tourism.module.knowledge.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.content.pojo.dto.ContentDtos;
import com.luoyang.tourism.module.knowledge.pojo.dto.KnowledgeDtos;
import com.luoyang.tourism.module.knowledge.pojo.vo.KnowledgeVos;
import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile;

/**
 * KnowledgeService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */

public interface KnowledgeService {
    /** 上传文件或文档。 */
    public KnowledgeVos.UploadResult upload(MultipartFile file, String title, String category, Integer versionNo, LocalDateTime effectiveAt, Long adminId);

    /** 重新提交文档索引。 */
    public KnowledgeVos.UploadResult reindex(Long documentId, KnowledgeDtos.ReindexRequest request, Long adminId);

    /** 查询知识文档列表。 */
    public PageResult<KnowledgeVos.DocumentVO> documents(String keyword, Integer status, Integer page, Integer pageSize);

    /** 查询知识文档详情。 */
    public KnowledgeVos.DocumentVO document(Long id);

    /** 执行 updateStatus 业务操作。 */
    public KnowledgeVos.DocumentVO updateStatus(Long id, ContentDtos.StatusRequest request, Long adminId);

    /** 删除数据。 */
    public void delete(Long id, Integer version, Long adminId);

    /** 查询索引任务列表。 */
    public PageResult<KnowledgeVos.JobVO> jobs(Long documentId, Integer status, Integer page, Integer pageSize);

    /** 查询索引任务。 */
    public KnowledgeVos.JobVO job(Long id);

    /** 轮询运行中的索引任务。 */
    public void pollRunningJobs();
}
