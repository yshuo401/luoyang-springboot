package com.luoyang.tourism.module.knowledge.mapper;

import com.luoyang.tourism.module.knowledge.entity.AiIndexJob;
import com.luoyang.tourism.module.knowledge.entity.KnowledgeChunk;
import com.luoyang.tourism.module.knowledge.entity.KnowledgeDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface KnowledgeMapper {
    int insertDocument(KnowledgeDocument document);
    KnowledgeDocument selectDocument(@Param("id") Long id);
    List<KnowledgeDocument> selectDocuments(@Param("keyword") String keyword, @Param("status") Integer status,
                                            @Param("offset") long offset, @Param("limit") int limit);
    long countDocuments(@Param("keyword") String keyword, @Param("status") Integer status);
    int updateDocumentStatus(@Param("id") Long id, @Param("status") Integer status,
                             @Param("version") Integer version, @Param("updatedBy") Long updatedBy);
    int updateDocumentIndexStatus(@Param("id") Long id, @Param("indexStatus") Integer indexStatus,
                                  @Param("indexError") String indexError, @Param("chunkCount") Integer chunkCount,
                                  @Param("publishedAt") LocalDateTime publishedAt);
    int softDeleteDocument(@Param("id") Long id, @Param("version") Integer version,
                           @Param("updatedBy") Long updatedBy);
    int hardDeleteChunks(@Param("documentId") Long documentId);
    int insertChunk(KnowledgeChunk chunk);

    int insertJob(AiIndexJob job);
    AiIndexJob selectJob(@Param("id") Long id);
    AiIndexJob selectJobByIdempotency(@Param("key") String key);
    List<AiIndexJob> selectJobs(@Param("documentId") Long documentId, @Param("status") Integer status,
                                @Param("offset") long offset, @Param("limit") int limit);
    long countJobs(@Param("documentId") Long documentId, @Param("status") Integer status);
    List<AiIndexJob> selectRunningJobs(@Param("limit") int limit);
    int markJobSubmitted(@Param("id") Long id, @Param("externalJobId") String externalJobId,
                         @Param("progress") Integer progress);
    int updateJobProgress(@Param("id") Long id, @Param("progress") Integer progress,
                          @Param("totalChunks") Integer totalChunks,
                          @Param("processedChunks") Integer processedChunks);
    int markJobSucceeded(@Param("id") Long id, @Param("progress") Integer progress,
                         @Param("totalChunks") Integer totalChunks);
    int markJobFailed(@Param("id") Long id, @Param("errorMessage") String errorMessage);
}
