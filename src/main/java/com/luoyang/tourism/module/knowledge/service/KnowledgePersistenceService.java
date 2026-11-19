package com.luoyang.tourism.module.knowledge.service;

import com.luoyang.tourism.integration.ai.AiDtos;
import com.luoyang.tourism.module.knowledge.entity.AiIndexJob;
import com.luoyang.tourism.module.knowledge.entity.KnowledgeDocument;
import com.luoyang.tourism.module.knowledge.mapper.KnowledgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class KnowledgePersistenceService {
    private final KnowledgeMapper mapper;

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

    @Transactional
    public AiIndexJob createJob(AiIndexJob job) {
        mapper.insertJob(job);
        mapper.updateDocumentIndexStatus(job.getDocumentId(), 0, null, null, null);
        return job;
    }

    public void submitted(Long jobId, AiDtos.IndexJobResult result) {
        mapper.markJobSubmitted(jobId, result.externalJobId(),
                result.progress() == null ? 0 : result.progress());
    }

    @Transactional
    public void submissionFailed(AiIndexJob job) {
        mapper.markJobFailed(job.getId(), "索引任务提交失败");
        mapper.updateDocumentIndexStatus(job.getDocumentId(), 3, "索引任务提交失败", null, null);
    }

    @Transactional
    public void applyJobResult(AiIndexJob job, AiDtos.IndexJobResult result) {
        String status = result.status() == null ? "" : result.status().toUpperCase(Locale.ROOT);
        if (status.contains("SUCCEED") || status.contains("SUCCESS") || status.contains("COMPLETE")) {
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

    @Transactional
    public void deleteDocument(KnowledgeDocument document, Long adminId) {
        mapper.hardDeleteChunks(document.getId());
        mapper.updateDocumentIndexStatus(document.getId(), 5, null, 0, null);
        if (mapper.softDeleteDocument(document.getId(), document.getVersion(), adminId) != 1) {
            throw new com.luoyang.tourism.common.exception.BizException(
                    com.luoyang.tourism.common.api.ResultCode.VERSION_CONFLICT);
        }
    }

    private String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }

    public record Created(KnowledgeDocument document, AiIndexJob job) {
    }
}
