package com.luoyang.tourism.module.appuser.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.common.enums.BizType;
import com.luoyang.tourism.module.appuser.pojo.dto.AppUserDtos;
import com.luoyang.tourism.module.appuser.pojo.vo.AppUserVos;
import com.luoyang.tourism.module.auth.pojo.vo.AuthVos;

/**
 * AppUserService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */
public interface AppUserService {
    /** 查询用户资料。 */
    public AuthVos.AppUserVO profile(Long userId);

    /** 执行 updateProfile 业务操作。 */
    public AuthVos.AppUserVO updateProfile(Long userId, AppUserDtos.ProfileUpdateRequest request);

    /** 查询用户偏好。 */
    public AppUserVos.PreferenceVO preferences(Long userId);

    /** 执行 replacePreferences 业务操作。 */
    public AppUserVos.PreferenceVO replacePreferences(Long userId, AppUserDtos.PreferenceUpdateRequest request);

    /** 查询收藏列表。 */
    public PageResult<AppUserVos.FavoriteVO> favorites(Long userId, String bizType, Integer page, Integer pageSize);

    /** 执行 addFavorite 业务操作。 */
    public AppUserVos.FavoriteVO addFavorite(Long userId, AppUserDtos.FavoriteRequest request);

    /** 执行 removeFavorite 业务操作。 */
    public void removeFavorite(Long userId, String bizType, Long bizId);

    /** 执行 isFavorite 业务操作。 */
    public boolean isFavorite(Long userId, BizType type, Long bizId);

    /** 执行 reportBehavior 业务操作。 */
    public void reportBehavior(Long userId, AppUserDtos.BehaviorRequest request);
}
