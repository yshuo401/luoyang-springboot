package com.luoyang.tourism.unit.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.module.knowledge.mapper.KnowledgeMapper;
import com.luoyang.tourism.module.knowledge.pojo.dto.KnowledgeDtos;
import com.luoyang.tourism.module.knowledge.pojo.entity.KnowledgeChunk;
import com.luoyang.tourism.module.knowledge.pojo.entity.KnowledgeDocument;
import com.luoyang.tourism.module.knowledge.service.impl.KnowledgePersistenceServiceImpl;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgePersistenceServiceImplTest {
    @Mock
    private KnowledgeMapper mapper;

    private KnowledgePersistenceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new KnowledgePersistenceServiceImpl(mapper, new ObjectMapper());
    }

    @Test
    void upsertsBatchChunksAndSerializesMetadata() {
        KnowledgeDocument document = document(18L, 1);
        when(mapper.selectDocument(18L)).thenReturn(document);

        KnowledgeDtos.ChunkBatchResult result = service.upsertChunks(new KnowledgeDtos.ChunkBatchRequest(
                "18", 1, List.of(
                new KnowledgeDtos.ChunkItem(0, "vector-000000000001", "第一段", "a".repeat(64), 3,
                        Map.of("page", 1, "sectionPath", List.of("景区介绍"))),
                new KnowledgeDtos.ChunkItem(1, "vector-000000000002", "第二段", "b".repeat(64), 3,
                        Map.of("page", 1))
        )));

        ArgumentCaptor<KnowledgeChunk> captor = ArgumentCaptor.forClass(KnowledgeChunk.class);
        verify(mapper, org.mockito.Mockito.times(2)).upsertChunk(captor.capture());
        assertThat(result.upsertedChunks()).isEqualTo(2);
        assertThat(captor.getAllValues().get(0).getDocumentId()).isEqualTo(18L);
        assertThat(captor.getAllValues().get(0).getMetadata()).contains("景区介绍");
        assertThat(captor.getAllValues().get(0).getEnabled()).isEqualTo(1);
    }

    @Test
    void rejectsStaleDocumentVersion() {
        when(mapper.selectDocument(18L)).thenReturn(document(18L, 2));

        BizException exception = catchThrowableOfType(() -> service.upsertChunks(
                new KnowledgeDtos.ChunkBatchRequest("18", 1, List.of(
                        new KnowledgeDtos.ChunkItem(0, "vector-000000000001", "第一段", "a".repeat(64), 3,
                                Map.of())
                ))), BizException.class);

        assertThat(exception.resultCode()).isEqualTo(ResultCode.VERSION_CONFLICT);
        verify(mapper, org.mockito.Mockito.never()).upsertChunk(org.mockito.ArgumentMatchers.any());
    }

    private KnowledgeDocument document(Long id, int versionNo) {
        KnowledgeDocument document = new KnowledgeDocument();
        document.setId(id);
        document.setVersionNo(versionNo);
        document.setIsDeleted(0);
        return document;
    }
}