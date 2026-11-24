package com.luoyang.tourism.module.knowledge.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.integration.ai.AiDtos;
import com.luoyang.tourism.module.knowledge.mapper.KnowledgeMapper;
import com.luoyang.tourism.module.knowledge.pojo.dto.KnowledgeDtos;
import com.luoyang.tourism.module.knowledge.pojo.entity.AiIndexJob;
import com.luoyang.tourism.module.knowledge.pojo.entity.KnowledgeChunk;
import com.luoyang.tourism.module.knowledge.pojo.entity.KnowledgeDocument;
import com.luoyang.tourism.module.knowledge.service.KnowledgePersistenceService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * KnowledgePersistenceServiceImpl 业务实现。
 * <p>负责事务、业务规则、逻辑外键校验和外部服务编排。</p>
 */
@Service
@RequiredArgsConstructor
public class KnowledgePersistenceServiceImpl implements KnowledgePersistenceService {
    private final KnowledgeMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Created createDocument(KnowledgeDocument document, AiIndexJob job) {
        mapper.insertDocument(document);
        job.setDocumentId(document.getId());
        job.setDocumentVersion(document.getVersionNo());
        if (job.getIdempotencyKey() == null || job.getIdempotencyKey().isBlank()) {
            job.setIdempotencyKey(document.getId() + ":" + document.getVersionNo() + ":FULL_INDEX");
        }
        mapper.insertJob(job);
        return new Created(document, job);
    }

    @Override
    @Transactional
    public AiIndexJob createJob(AiIndexJob job) {
        mapper.insertJob(job);
        mapper.updateDocumentIndexStatus(job.getDocumentId(), 0, null, null, null);
        return job;
    }

    @Override
    public void submitted(Long jobId, AiDtos.IndexJobResult result) {
        mapper.markJobSubmitted(jobId, result.externalJobId(),
                result.progress() == null ? 0 : result.progress());
    }

    @Override
    @Transactional
    public void submissionFailed(AiIndexJob job) {
        mapper.markJobFailed(job.getId(), "索引任务提交失败");
        mapper.updateDocumentIndexStatus(job.getDocumentId(), 3, "索引任务提交失败", null, null);
    }

    @Override
    @Transactional
    public void applyJobResult(AiIndexJob job, AiDtos.IndexJobResult result) {
        String status = result.status() == null ? "" : result.status().toUpperCase(Locale.ROOT);
        if (status.contains("SUCCEED") || status.contains("SUCCESS") || status.contains("COMPLETE")) {
            // 索引完成时清理同一文档版本按旧切分策略留下的尾部分片。
            if (result.totalChunks() != null && result.totalChunks() > 0) {
                mapper.deleteChunksAtOrAfter(job.getDocumentId(), result.totalChunks());
            }
            mapper.markJobSucceeded(job.getId(), result.progress() == null ? 100 : result.progress(),
                    result.totalChunks());
            mapper.updateDocumentIndexStatus(job.getDocumentId(), 2, null, result.totalChunks(),
                    LocalDateTime.now());
        } else if (status.contains("FAIL") || status.contains("ERROR")) {
            String error = result.errorMessage() == null ? "索引任务失败" : truncate(result.errorMessage(), 1000);
            mapper.markJobFailed(job.getId(), error);
            mapper.updateDocumentIndexStatus(job.getDocumentId(), 3, error, null, null);
        } else {
            mapper.updateJobProgress(job.getId(), result.progress(), result.totalChunks(),
                    result.processedChunks());
        }
    }

    @Override
    @Transactional
    public void deleteDocument(KnowledgeDocument document, Long adminId) {
        mapper.hardDeleteChunks(document.getId());
        mapper.updateDocumentIndexStatus(document.getId(), 5, null, 0, null);
        if (mapper.softDeleteDocument(document.getId(), document.getVersion(), adminId) != 1) {
            throw new BizException(ResultCode.VERSION_CONFLICT);
        }
    }

    @Override
    @Transactional
    public KnowledgeDtos.ChunkBatchResult upsertChunks(KnowledgeDtos.ChunkBatchRequest request) {
        Long documentId = parseDocumentId(request.documentId());
        KnowledgeDocument document = mapper.selectDocument(documentId);
        if (document == null) {
            throw BizException.notFound();
        }
        if (!document.getVersionNo().equals(request.documentVersion())) {
            throw new BizException(ResultCode.VERSION_CONFLICT, "知识文档版本已变化，拒绝旧版本分片写入");
        }
        if (request.chunks() == null || request.chunks().isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "知识分片不能为空");
        }

        // 同一批请求内先做唯一性校验，避免依赖数据库异常暴露底层实现。
        Set<Integer> chunkIndexes = new HashSet<>();
        Set<String> vectorIds = new HashSet<>();
        for (KnowledgeDtos.ChunkItem item : request.chunks()) {
            if (!chunkIndexes.add(item.chunkIndex())) {
                throw new BizException(ResultCode.BAD_REQUEST, "chunkIndex 不能重复");
            }
            if (!vectorIds.add(item.vectorId())) {
                throw new BizException(ResultCode.BAD_REQUEST, "vectorId 不能重复");
            }
        }

        for (KnowledgeDtos.ChunkItem item : request.chunks()) {
            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setDocumentId(documentId);
            chunk.setChunkIndex(item.chunkIndex());
            chunk.setVectorId(item.vectorId());
            chunk.setContent(item.content());
            chunk.setContentHash(item.contentHash());
            chunk.setCharCount(item.charCount());
            chunk.setMetadata(metadataJson(item.metadata()));
            chunk.setEnabled(1);
            mapper.upsertChunk(chunk);
        }
        return new KnowledgeDtos.ChunkBatchResult(request.documentId(), request.documentVersion(),
                request.chunks().size());
    }

    private Long parseDocumentId(String raw) {
        try {
            return Long.valueOf(raw);
        } catch (NumberFormatException exception) {
            throw new BizException(ResultCode.BAD_REQUEST, "documentId 不合法");
        }
    }

    private String metadataJson(Map<String, Object> metadata) {
        Map<String, Object> value = metadata == null ? new HashMap<>() : metadata;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BizException(ResultCode.BAD_REQUEST, "metadata 不是合法 JSON");
        }
    }

    private String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}