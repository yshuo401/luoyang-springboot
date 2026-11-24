package com.luoyang.tourism.module.knowledge.service;

import com.luoyang.tourism.integration.ai.AiDtos;
import com.luoyang.tourism.module.knowledge.pojo.dto.KnowledgeDtos;
import com.luoyang.tourism.module.knowledge.pojo.entity.AiIndexJob;
import com.luoyang.tourism.module.knowledge.pojo.entity.KnowledgeDocument;

/**
 * KnowledgePersistenceService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */

public interface KnowledgePersistenceService {
    /** 执行 createDocument 业务操作。 */
    public Created createDocument(KnowledgeDocument document, AiIndexJob job);

    /** 执行 createJob 业务操作。 */
    public AiIndexJob createJob(AiIndexJob job);

    /** 执行 submitted 业务操作。 */
    public void submitted(Long jobId, AiDtos.IndexJobResult result);

    /** 执行 submissionFailed 业务操作。 */
    public void submissionFailed(AiIndexJob job);

    /** 执行 applyJobResult 业务操作。 */
    public void applyJobResult(AiIndexJob job, AiDtos.IndexJobResult result);

    /** 执行 deleteDocument 业务操作。 */
    public void deleteDocument(KnowledgeDocument document, Long adminId);

    /** 幂等写入 FastAPI 回调的知识分片元数据。 */
    public KnowledgeDtos.ChunkBatchResult upsertChunks(KnowledgeDtos.ChunkBatchRequest request);

    public record Created(KnowledgeDocument document, AiIndexJob job) {
    }
}
