package com.luoyang.tourism.module.knowledge.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.content.dto.ContentDtos;
import com.luoyang.tourism.module.knowledge.dto.KnowledgeDtos;
import com.luoyang.tourism.module.knowledge.service.KnowledgeService;
import com.luoyang.tourism.module.knowledge.vo.KnowledgeVos;
import com.luoyang.tourism.security.principal.AuthClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/knowledge")
@Tag(name = "管理端-知识库", description = "知识文档、索引任务与 Milvus 向量生命周期")
@SecurityRequirement(name = "bearerAuth")
public class AdminKnowledgeController {
    private final KnowledgeService service;

    @GetMapping("/documents")
    @PreAuthorize("hasAuthority('PERM_knowledge:read')")
    @Operation(summary = "知识文档分页列表")
    public ApiResponse<PageResult<KnowledgeVos.DocumentVO>> documents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(service.documents(keyword, status, page, pageSize));
    }

    @GetMapping("/documents/{id}")
    @PreAuthorize("hasAuthority('PERM_knowledge:read')")
    @Operation(summary = "知识文档详情")
    public ApiResponse<KnowledgeVos.DocumentVO> document(@PathVariable Long id) {
        return ApiResponse.success(service.document(id));
    }

    @PostMapping("/documents")
    @PreAuthorize("hasAuthority('PERM_knowledge:upload') and hasAuthority('PERM_knowledge:index')")
    @Operation(summary = "上传文档并提交索引任务")
    public ApiResponse<KnowledgeVos.UploadResult> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam String category,
            @RequestParam(required = false) Integer versionNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime effectiveAt,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.upload(file, title, category, versionNo, effectiveAt,
                AuthClaims.userId(jwt)));
    }

    @PostMapping("/documents/{id}/reindex")
    @PreAuthorize("hasAuthority('PERM_knowledge:index')")
    @Operation(summary = "重新索引文档")
    public ApiResponse<KnowledgeVos.UploadResult> reindex(
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeDtos.ReindexRequest body,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.reindex(id, body, AuthClaims.userId(jwt)));
    }

    @PatchMapping("/documents/{id}/status")
    @PreAuthorize("hasAuthority('PERM_knowledge:update')")
    @Operation(summary = "启用、禁用或归档知识文档")
    public ApiResponse<KnowledgeVos.DocumentVO> status(
            @PathVariable Long id,
            @Valid @RequestBody ContentDtos.StatusRequest body,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(service.updateStatus(id, body, AuthClaims.userId(jwt)));
    }

    @DeleteMapping("/documents/{id}")
    @PreAuthorize("hasAuthority('PERM_knowledge:delete')")
    @Operation(summary = "删除文档与向量")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @RequestParam("version") Integer version,
                                    @AuthenticationPrincipal Jwt jwt) {
        service.delete(id, version, AuthClaims.userId(jwt));
        return ApiResponse.success();
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasAuthority('PERM_knowledge:read')")
    @Operation(summary = "索引任务分页列表")
    public ApiResponse<PageResult<KnowledgeVos.JobVO>> jobs(
            @RequestParam(required = false) Long documentId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(service.jobs(documentId, status, page, pageSize));
    }

    @GetMapping("/jobs/{jobId}")
    @PreAuthorize("hasAuthority('PERM_knowledge:read')")
    @Operation(summary = "索引任务详情")
    public ApiResponse<KnowledgeVos.JobVO> job(@PathVariable Long jobId) {
        return ApiResponse.success(service.job(jobId));
    }
}
