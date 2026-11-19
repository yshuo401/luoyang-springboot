package com.luoyang.tourism.module.knowledge.service.impl;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.api.Paging;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.common.trace.TraceIds;
import com.luoyang.tourism.common.util.TimeUtils;
import com.luoyang.tourism.integration.ai.AiClient;
import com.luoyang.tourism.integration.ai.AiClientException;
import com.luoyang.tourism.integration.ai.AiDtos;
import com.luoyang.tourism.module.content.pojo.dto.ContentDtos;
import com.luoyang.tourism.module.file.mapper.FileMapper;
import com.luoyang.tourism.module.file.pojo.entity.FileObject;
import com.luoyang.tourism.module.file.pojo.vo.FileVO;
import com.luoyang.tourism.module.file.service.FileService;
import com.luoyang.tourism.module.knowledge.mapper.KnowledgeMapper;
import com.luoyang.tourism.module.knowledge.pojo.dto.KnowledgeDtos;
import com.luoyang.tourism.module.knowledge.pojo.entity.AiIndexJob;
import com.luoyang.tourism.module.knowledge.pojo.entity.KnowledgeDocument;
import com.luoyang.tourism.module.knowledge.pojo.vo.KnowledgeVos;
import com.luoyang.tourism.module.knowledge.service.KnowledgePersistenceService;
import com.luoyang.tourism.module.knowledge.service.KnowledgeService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * KnowledgeServiceImpl 业务实现。
 * <p>负责事务、业务规则、逻辑外键校验和外部服务编排。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {
    private final KnowledgeMapper mapper;
    private final KnowledgePersistenceService persistence;
    private final FileService fileService;
    private final FileMapper fileMapper;
    private final AiClient aiClient;

@Override
public KnowledgeVos.UploadResult upload(MultipartFile file, String title, String category,
                                           Integer versionNo, LocalDateTime effectiveAt, Long adminId) {
        if (category == null || category.isBlank() || category.length() > 64) {
            throw new BizException(ResultCode.BAD_REQUEST, "知识分类不能为空且不能超过 64 字符");
        }
        FileVO upload = fileService.upload(file, "KNOWLEDGE", 1, 2, adminId);
        Long fileId = Long.valueOf(upload.fileId());
        FileObject fileObject = fileMapper.selectById(fileId);
        if (fileObject == null) {
            throw new BizException(ResultCode.DATA_INCONSISTENCY);
        }
        KnowledgeDocument document = new KnowledgeDocument();
        document.setTitle(title == null || title.isBlank() ? fileObject.getOriginalName() : title.trim());
        if (document.getTitle().length() > 255) {
            throw new BizException(ResultCode.BAD_REQUEST, "文档标题不能超过 255 字符");
        }
        document.setCategory(category.trim());
        document.setFileId(fileId);
        document.setDocType(docType(fileObject.getExtension()));
        document.setVersionNo(versionNo == null || versionNo < 1 ? 1 : versionNo);
        document.setChecksum(fileObject.getSha256());
        document.setStatus(1);
        document.setIndexStatus(0);
        document.setEffectiveAt(effectiveAt);
        document.setCreatedBy(adminId);
        document.setUpdatedBy(adminId);

        AiIndexJob job = new AiIndexJob();
        job.setJobType(1);
        job.setStatus(0);
        job.setProgress(0);
        job.setTotalChunks(0);
        job.setProcessedChunks(0);
        job.setRequestedBy(adminId);
        // 文档和任务先落库，外部索引提交失败时仍保留可重试记录。
        KnowledgePersistenceService.Created created = persistence.createDocument(document, job);
        return submit(created.document(), created.job());
    }

@Override
public KnowledgeVos.UploadResult reindex(Long documentId, KnowledgeDtos.ReindexRequest request, Long adminId) {
        KnowledgeDocument document = requiredDocument(documentId);
        AiIndexJob existing = mapper.selectJobByIdempotency(request.idempotencyKey());
        if (existing != null) {
            if (!existing.getDocumentId().equals(documentId)) {
                throw new BizException(ResultCode.IDEMPOTENCY_CONFLICT);
            }
            return new KnowledgeVos.UploadResult(Long.toString(documentId), document.getVersionNo(),
                    Long.toString(existing.getId()), document.getIndexStatus(), "索引任务已存在");
        }
        AiIndexJob job = new AiIndexJob();
        job.setDocumentId(documentId);
        job.setDocumentVersion(document.getVersionNo());
        job.setJobType(2);
        job.setStatus(0);
        job.setIdempotencyKey(request.idempotencyKey());
        job.setProgress(0);
        job.setRequestedBy(adminId);
        persistence.createJob(job);
        return submit(document, job);
    }

@Override
public PageResult<KnowledgeVos.DocumentVO> documents(String keyword, Integer status,
                                                         Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        List<KnowledgeVos.DocumentVO> records = mapper.selectDocuments(keyword, status,
                        Paging.offset(safePage, safeSize), safeSize).stream().map(this::documentVo).toList();
        return Paging.of(records, mapper.countDocuments(keyword, status), safePage, safeSize);
    }

@Override
public KnowledgeVos.DocumentVO document(Long id) {
        return documentVo(requiredDocument(id));
    }

@Override
public KnowledgeVos.DocumentVO updateStatus(Long id, ContentDtos.StatusRequest request, Long adminId) {
        requiredDocument(id);
        int status = request.status();
        if (status < 0 || status > 3) {
            throw new BizException(ResultCode.BAD_REQUEST, "知识文档状态值不合法");
        }
        if (mapper.updateDocumentStatus(id, status, request.version(), adminId) != 1) {
            throw new BizException(ResultCode.VERSION_CONFLICT);
        }
        return documentVo(requiredDocument(id));
    }

@Override
public void delete(Long id, Integer version, Long adminId) {
        KnowledgeDocument document = requiredDocument(id);
        try {
            aiClient.deleteDocumentVectors(Long.toString(id), TraceIds.current());
        } catch (AiClientException exception) {
            mapper.updateDocumentIndexStatus(id, 4, "等待重试删除向量", null, null);
            throw new BizException(ResultCode.AI_UNAVAILABLE, "向量删除失败，文档已标记为待重试");
        }
        persistence.deleteDocument(document, adminId);
    }

@Override
public PageResult<KnowledgeVos.JobVO> jobs(Long documentId, Integer status, Integer page, Integer pageSize) {
        int safePage = Paging.normalizePage(page);
        int safeSize = Paging.normalizePageSize(pageSize);
        List<KnowledgeVos.JobVO> records = mapper.selectJobs(documentId, status,
                        Paging.offset(safePage, safeSize), safeSize).stream().map(this::jobVo).toList();
        return Paging.of(records, mapper.countJobs(documentId, status), safePage, safeSize);
    }

@Override
public KnowledgeVos.JobVO job(Long id) {
        AiIndexJob job = mapper.selectJob(id);
        if (job == null) {
            throw BizException.notFound();
        }
        return jobVo(job);
    }

    /**
     * 由定时任务轮询 FastAPI 索引状态；数据库更新通过独立事务完成。
     */
@Override
public void pollRunningJobs() {
        for (AiIndexJob job : mapper.selectRunningJobs(20)) {
            try {
                AiDtos.IndexJobResult result = aiClient.queryIndexJob(job.getExternalJobId(), TraceIds.current());
                persistence.applyJobResult(job, result);
            } catch (RuntimeException exception) {
                log.warn("index job polling failed jobId={} traceId={}", job.getId(), TraceIds.current());
            }
        }
    }

    /**
     * 提交索引任务。这里刻意不在数据库事务内调用 FastAPI，避免长耗时外部调用占用连接。
     */
    private KnowledgeVos.UploadResult submit(KnowledgeDocument document, AiIndexJob job) {
        try {
            String downloadUrl = fileService.url(document.getFileId(), 900);
            AiDtos.IndexJobResult result = aiClient.submitIndexJob(new AiDtos.IndexJobRequest(
                    Long.toString(document.getId()), document.getVersionNo(), Long.toString(document.getFileId()),
                    downloadUrl, document.getDocType(), document.getTitle(), document.getCategory(),
                    job.getIdempotencyKey()), TraceIds.current());
            persistence.submitted(job.getId(), result);
            return new KnowledgeVos.UploadResult(Long.toString(document.getId()), document.getVersionNo(),
                    Long.toString(job.getId()), 0, "文档已保存，索引任务已提交");
        } catch (RuntimeException exception) {
            // AI 服务不可用不删除已上传文档，保留任务供管理员重新提交。
            persistence.submissionFailed(job);
            return new KnowledgeVos.UploadResult(Long.toString(document.getId()), document.getVersionNo(),
                    Long.toString(job.getId()), 0, "文档已保存，索引任务提交失败，可稍后重试");
        }
    }

    private KnowledgeDocument requiredDocument(Long id) {
        KnowledgeDocument document = mapper.selectDocument(id);
        if (document == null) {
            throw BizException.notFound();
        }
        return document;
    }

    private int docType(String extension) {
        String ext = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        return switch (ext) {
            case "pdf" -> 1;
            case "docx" -> 2;
            case "md" -> 3;
            case "txt" -> 4;
            default -> throw new BizException(ResultCode.BAD_REQUEST, "不支持的知识文档格式");
        };
    }

    private KnowledgeVos.DocumentVO documentVo(KnowledgeDocument document) {
        return new KnowledgeVos.DocumentVO(Long.toString(document.getId()), document.getTitle(),
                document.getCategory(), Long.toString(document.getFileId()), document.getDocType(),
                document.getVersionNo(), document.getChecksum(), document.getStatus(),
                document.getIndexStatus(), document.getIndexError(), document.getChunkCount(),
                document.getSourceUrl(), TimeUtils.offset(document.getEffectiveAt()),
                TimeUtils.offset(document.getPublishedAt()), document.getVersion(),
                TimeUtils.offset(document.getUpdatedAt()));
    }

    private KnowledgeVos.JobVO jobVo(AiIndexJob job) {
        return new KnowledgeVos.JobVO(Long.toString(job.getId()), Long.toString(job.getDocumentId()),
                job.getDocumentVersion(), job.getJobType(), job.getStatus(), job.getExternalJobId(),
                job.getProgress(), job.getTotalChunks(), job.getProcessedChunks(), job.getErrorMessage(),
                TimeUtils.offset(job.getStartedAt()), TimeUtils.offset(job.getFinishedAt()),
                TimeUtils.offset(job.getCreatedAt()));
    }
}
