package com.luoyang.tourism.module.appuser.mapper;

import com.luoyang.tourism.module.appuser.pojo.entity.UserBehavior;
import com.luoyang.tourism.module.appuser.pojo.entity.UserFavorite;
import com.luoyang.tourism.module.appuser.pojo.entity.UserPreference;
import com.luoyang.tourism.module.appuser.pojo.entity.UserRecommendationLog;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * AppUserMapper MyBatis 数据访问接口。
 * <p>SQL 只在本层维护，业务层不得拼接用户可控 SQL。</p>
 */
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
