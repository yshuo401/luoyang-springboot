package com.luoyang.tourism.module.system.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 后台用户请求参数。
 */
public final class SystemDtos {
    private SystemDtos() {
    }

    public record AppUserStatusRequest(
            @NotNull @Min(0) @Max(1) Integer status,
            @Size(max = 255) String reason
    ) {
    }

    public record AdminCreateRequest(
            @NotBlank @Size(max = 64) String username,
            @NotBlank @Size(min = 8, max = 128)
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密码必须同时包含字母和数字")
            String password,
            @NotBlank @Size(max = 64) String nickname
    ) {
    }

    public record AdminUpdateRequest(
            @NotBlank @Size(max = 64) String nickname,
            @Email @Size(max = 128) String email,
            @Size(max = 32) String phone,
            Long avatarFileId,
            @NotNull Integer version
    ) {
    }

    public record AdminResetPasswordRequest(
            @NotBlank @Size(min = 8, max = 128)
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密码必须同时包含字母和数字")
            String newPassword,
            @NotBlank @Size(max = 255) String reason
    ) {
    }

    public record AdminStatusRequest(
            @NotNull @Min(0) @Max(1) Integer status,
            @Size(max = 255) String reason,
            @NotNull Integer version
    ) {
    }
}