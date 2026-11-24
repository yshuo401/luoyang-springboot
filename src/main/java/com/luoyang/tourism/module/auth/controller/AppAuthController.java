package com.luoyang.tourism.module.auth.controller;

import com.luoyang.tourism.common.api.ApiResponse;
import com.luoyang.tourism.common.util.RequestUtils;
import com.luoyang.tourism.module.auth.pojo.dto.AuthDtos;
import com.luoyang.tourism.module.auth.pojo.vo.AuthVos;
import com.luoyang.tourism.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序认证接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app/auth")
@Tag(name = "小程序-认证", description = "微信登录")
public class AppAuthController {
    private final AuthService service;

    @PostMapping("/wechat-login")
    @Operation(summary = "微信小程序登录")
    public ApiResponse<AuthVos.AppLoginVO> login(@Valid @RequestBody AuthDtos.WechatLoginRequest body,
                                                  HttpServletRequest request) {
        return ApiResponse.success(service.wechatLogin(body, RequestUtils.clientIp(request)));
    }
}