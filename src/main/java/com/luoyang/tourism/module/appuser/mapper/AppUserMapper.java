package com.luoyang.tourism.module.appuser.mapper;

import com.luoyang.tourism.module.appuser.entity.UserBehavior;
import com.luoyang.tourism.module.appuser.entity.UserFavorite;
import com.luoyang.tourism.module.appuser.entity.UserPreference;
import com.luoyang.tourism.module.appuser.entity.UserRecommendationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppUserMapper {
    List<UserPreference> selectPreferences(@Param("userId") Long userId, @Param("source") Integer source);
    int deleteExplicitPreferences(@Param("userId") Long userId);
    int insertPreference(UserPreference preference);

    List<UserFavorite> selectFavorites(@Param("userId") Long userId, @Param("bizType") Integer bizType,
                                       @Param("offset") long offset, @Param("limit") int limit);
    long countFavorites(@Param("userId") Long userId, @Param("bizType") Integer bizType);
    UserFavorite selectFavorite(@Param("userId") Long userId, @Param("bizType") Integer bizType,
                                @Param("bizId") Long bizId);
    int insertFavorite(UserFavorite favorite);
    int deleteFavorite(@Param("userId") Long userId, @Param("bizType") Integer bizType,
                       @Param("bizId") Long bizId);
    int isFavorite(@Param("userId") Long userId, @Param("bizType") Integer bizType,
                   @Param("bizId") Long bizId);
    List<Long> selectFavoriteBizIds(@Param("userId") Long userId, @Param("bizType") Integer bizType);

    int insertBehavior(UserBehavior behavior);
    int insertRecommendationLog(UserRecommendationLog log);
}
