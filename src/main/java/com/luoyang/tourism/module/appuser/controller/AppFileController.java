package com.luoyang.tourism.module.appuser.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.module.file.service.FileService;
import com.luoyang.tourism.module.file.vo.FileVO;
import com.luoyang.tourism.security.principal.AuthClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app/files")
@Tag(name = "小程序-文件", description = "用户头像上传")
@SecurityRequirement(name = "bearerAuth")
public class AppFileController {
    private final FileService fileService;

    @PostMapping("/avatar")
    @Operation(summary = "上传头像")
    public ApiResponse<FileVO> avatar(@RequestPart("file") MultipartFile file,
                                      @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(fileService.upload(file, "AVATAR", 1, 1, AuthClaims.userId(jwt)));
    }
}
