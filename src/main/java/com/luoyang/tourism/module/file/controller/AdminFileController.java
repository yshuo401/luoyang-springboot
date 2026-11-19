package com.luoyang.tourism.module.file.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.file.pojo.vo.FileVO;
import com.luoyang.tourism.module.file.service.FileService;
import com.luoyang.tourism.security.principal.AuthClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * AdminFileController 控制器。
 * <p>负责接收 HTTP 请求、校验参数，并委托对应的业务接口处理。</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/files")
@Tag(name = "管理端-文件", description = "阿里云 OSS 私有文件上传和签名 URL")
@SecurityRequirement(name = "bearerAuth")
public class AdminFileController {
    private final FileService fileService;

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_file:upload')")
    @Operation(summary = "上传文件")
    public ApiResponse<FileVO> upload(@RequestPart("file") MultipartFile file,
                                      @RequestPart("bizScene") String bizScene,
                                      @RequestParam(defaultValue = "1") Integer visibility,
                                      @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(fileService.upload(file, bizScene, visibility, 2, AuthClaims.userId(jwt)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_file:read')")
    @Operation(summary = "文件分页列表")
    public ApiResponse<PageResult<FileVO>> list(
            @RequestParam(required = false) String bizScene,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(fileService.list(bizScene, status, page, pageSize));
    }

    @GetMapping("/{id}/url")
    @PreAuthorize("hasAuthority('PERM_file:read')")
    @Operation(summary = "获取短时签名 URL")
    public ApiResponse<Map<String, String>> url(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "1800") Integer expiresSeconds) {
        return ApiResponse.success(Map.of("url", fileService.url(id, expiresSeconds)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_file:delete')")
    @Operation(summary = "删除未被引用的文件")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return ApiResponse.success();
    }
}
