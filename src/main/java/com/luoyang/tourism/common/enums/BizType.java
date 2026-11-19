package com.luoyang.tourism.common.enums;

import com.luoyang.tourism.common.api.ResultCode;
import com.luoyang.tourism.common.exception.BizException;

/**
 * BizType 通用组件。
 * <p>为各业务模块提供统一的基础能力。</p>
 */

public enum BizType {
    SPOT(1, "SPOT", "scenic_spot", "景点"),
    GUIDE(2, "GUIDE", "travel_guide", "攻略"),
    FOOD(3, "FOOD", "food", "美食");

    private final int code;
    private final String apiValue;
    private final String tableName;
    private final String label;

    BizType(int code, String apiValue, String tableName, String label) {
        this.code = code;
        this.apiValue = apiValue;
        this.tableName = tableName;
        this.label = label;
    }

    public int code() {
        return code;
    }

    public String apiValue() {
        return apiValue;
    }

    public String tableName() {
        return tableName;
    }

    public String label() {
        return label;
    }

    public static BizType fromApi(String value) {
        if (value != null) {
            for (BizType type : values()) {
                if (type.apiValue.equalsIgnoreCase(value)) {
                    return type;
                }
            }
        }
        throw new BizException(ResultCode.UNSUPPORTED_VALUE, "不支持的业务类型");
    }

    public static BizType fromCode(Integer code) {
        if (code != null) {
            for (BizType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
        }
        throw new BizException(ResultCode.UNSUPPORTED_VALUE, "不支持的业务类型");
    }
}
