package com.luoyang.tourism.integration.wechat;

import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;
import com.luoyang.tourism.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WechatClient {
    private final AppProperties properties;
    private final RestClient.Builder restClientBuilder;

    public WechatSession login(String code) {
        if (code == null || code.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "微信登录 code 不能为空");
        }
        if (properties.getWechat().isMockEnabled()) {
            String openid = code.startsWith("mock:") ? code.substring(5) : code;
            if (openid.length() > 64) {
                openid = openid.substring(0, 64);
            }
            return new WechatSession(openid, null);
        }
        if (blank(properties.getWechat().getAppId()) || blank(properties.getWechat().getAppSecret())) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "微信登录暂未配置");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> body = restClientBuilder.build().get()
                .uri("https://api.weixin.qq.com/sns/jscode2session"
                                + "?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code",
                        properties.getWechat().getAppId(), properties.getWechat().getAppSecret(), code)
                .retrieve().body(Map.class);
        if (body == null || toInt(body.get("errcode")) != 0 || blank((String) body.get("openid"))) {
            throw new BizException(ResultCode.BAD_REQUEST, "微信登录凭证无效");
        }
        return new WechatSession((String) body.get("openid"), (String) body.get("unionid"));
    }

    private int toInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record WechatSession(String openid, String unionid) {
    }
}
