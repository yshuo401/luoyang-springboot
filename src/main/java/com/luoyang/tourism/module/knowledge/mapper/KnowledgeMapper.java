package com.luoyang.tourism.module.knowledge.mapper;

import com.luoyang.tourism.module.knowledge.pojo.entity.AiIndexJob;
import com.luoyang.tourism.module.knowledge.pojo.entity.KnowledgeChunk;
import com.luoyang.tourism.module.knowledge.pojo.entity.KnowledgeDocument;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * KnowledgeMapper MyBatis 数据访问接口。
 * <p>SQL 只在本层维护，业务层不得拼接用户可控 SQL。</p>
 */
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
    int upsertChunk(KnowledgeChunk chunk);
    int deleteChunksAtOrAfter(@Param("documentId") Long documentId,
                              @Param("chunkIndex") Integer chunkIndex);

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
