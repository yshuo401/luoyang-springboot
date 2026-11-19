package com.luoyang.tourism.module.appuser.service;

import com.luoyang.tourism.module.appuser.pojo.vo.AppUserVos;
import java.util.List;

/**
 * RecommendationService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */

public interface RecommendationService {
    /** 生成个性化推荐。 */
    public List<AppUserVos.RecommendationVO> recommend(Long userId, String scene, Integer limit, Long categoryId, Double longitude, Double latitude);
}
