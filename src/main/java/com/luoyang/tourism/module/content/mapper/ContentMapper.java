package com.luoyang.tourism.module.content.mapper;

import com.luoyang.tourism.module.content.entity.Banner;
import com.luoyang.tourism.module.content.entity.Food;
import com.luoyang.tourism.module.content.entity.FoodCategory;
import com.luoyang.tourism.module.content.entity.FoodImage;
import com.luoyang.tourism.module.content.entity.GuideDay;
import com.luoyang.tourism.module.content.entity.GuideDaySpot;
import com.luoyang.tourism.module.content.entity.ScenicCategory;
import com.luoyang.tourism.module.content.entity.ScenicImage;
import com.luoyang.tourism.module.content.entity.ScenicSpot;
import com.luoyang.tourism.module.content.entity.Tag;
import com.luoyang.tourism.module.content.entity.TravelGuide;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ContentMapper {
    List<ScenicCategory> selectCategories(@Param("admin") boolean admin,
                                          @Param("keyword") String keyword,
                                          @Param("status") Integer status);
    ScenicCategory selectCategory(@Param("id") Long id);
    int insertCategory(ScenicCategory category);
    int updateCategory(ScenicCategory category);
    int updateCategoryStatus(@Param("id") Long id, @Param("status") Integer status,
                             @Param("version") Integer version, @Param("updatedBy") Long updatedBy);
    int softDeleteCategory(@Param("id") Long id, @Param("version") Integer version,
                           @Param("updatedBy") Long updatedBy);
    int countChildCategories(@Param("id") Long id);
    int countSpotsInCategory(@Param("id") Long id);
    int countPublishedSpotsInCategory(@Param("id") Long id);
    int countCategoryCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    List<Tag> selectTags(@Param("admin") boolean admin, @Param("tagType") Integer tagType,
                         @Param("keyword") String keyword, @Param("status") Integer status,
                         @Param("offset") long offset, @Param("limit") int limit);
    long countTags(@Param("admin") boolean admin, @Param("tagType") Integer tagType,
                   @Param("keyword") String keyword, @Param("status") Integer status);
    Tag selectTag(@Param("id") Long id);
    List<Tag> selectTagsByIds(@Param("ids") List<Long> ids);
    List<Tag> selectTagsBySpotId(@Param("spotId") Long spotId);
    int insertTag(Tag tag);
    int updateTag(Tag tag);
    int updateTagStatus(@Param("id") Long id, @Param("status") Integer status,
                        @Param("version") Integer version, @Param("updatedBy") Long updatedBy);
    int softDeleteTag(@Param("id") Long id, @Param("version") Integer version,
                      @Param("updatedBy") Long updatedBy);
    int countTagCode(@Param("tagType") Integer tagType, @Param("code") String code,
                     @Param("excludeId") Long excludeId);
    int countSpotTagRefs(@Param("tagId") Long tagId);

    List<ScenicSpot> selectSpots(@Param("admin") boolean admin,
                                 @Param("keyword") String keyword,
                                 @Param("categoryId") Long categoryId,
                                 @Param("tagIds") List<Long> tagIds,
                                 @Param("status") Integer status,
                                 @Param("sort") String sort,
                                 @Param("offset") long offset,
                                 @Param("limit") int limit);
    long countSpots(@Param("admin") boolean admin,
                    @Param("keyword") String keyword,
                    @Param("categoryId") Long categoryId,
                    @Param("tagIds") List<Long> tagIds,
                    @Param("status") Integer status);
    ScenicSpot selectSpot(@Param("id") Long id);
    List<ScenicSpot> selectRelatedSpots(@Param("id") Long id, @Param("categoryId") Long categoryId,
                                        @Param("limit") int limit);
    List<ScenicSpot> selectPublishedCandidates(@Param("limit") int limit);
    int insertSpot(ScenicSpot spot);
    int updateSpot(ScenicSpot spot);
    int updateSpotStatus(@Param("id") Long id, @Param("status") Integer status,
                         @Param("version") Integer version, @Param("updatedBy") Long updatedBy,
                         @Param("publishedAt") LocalDateTime publishedAt);
    int updateSpotWeight(@Param("id") Long id, @Param("weight") Integer weight,
                         @Param("version") Integer version, @Param("updatedBy") Long updatedBy);
    int softDeleteSpot(@Param("id") Long id, @Param("version") Integer version,
                       @Param("updatedBy") Long updatedBy);
    List<ScenicImage> selectSpotImages(@Param("spotId") Long spotId);
    int deleteSpotImages(@Param("spotId") Long spotId);
    int insertSpotImage(ScenicImage image);
    int deleteSpotTags(@Param("spotId") Long spotId);
    int insertSpotTag(@Param("spotId") Long spotId, @Param("tagId") Long tagId,
                      @Param("createdBy") Long createdBy);

    List<TravelGuide> selectGuides(@Param("admin") boolean admin,
                                   @Param("keyword") String keyword,
                                   @Param("status") Integer status,
                                   @Param("sort") String sort,
                                   @Param("offset") long offset,
                                   @Param("limit") int limit);
    long countGuides(@Param("admin") boolean admin, @Param("keyword") String keyword,
                     @Param("status") Integer status);
    TravelGuide selectGuide(@Param("id") Long id);
    List<GuideDay> selectGuideDays(@Param("guideId") Long guideId);
    List<GuideDaySpot> selectGuideDaySpots(@Param("guideId") Long guideId);
    int insertGuide(TravelGuide guide);
    int updateGuide(TravelGuide guide);
    int updateGuideStatus(@Param("id") Long id, @Param("status") Integer status,
                          @Param("version") Integer version, @Param("updatedBy") Long updatedBy,
                          @Param("publishedAt") LocalDateTime publishedAt);
    int softDeleteGuide(@Param("id") Long id, @Param("version") Integer version,
                        @Param("updatedBy") Long updatedBy);
    int deleteGuideDaysHard(@Param("guideId") Long guideId);
    int insertGuideDay(GuideDay day);
    int deleteGuideDaySpotsHard(@Param("guideId") Long guideId);
    int insertGuideDaySpot(GuideDaySpot daySpot);

    List<FoodCategory> selectFoodCategories(@Param("admin") boolean admin,
                                            @Param("status") Integer status);
    FoodCategory selectFoodCategory(@Param("id") Long id);
    int insertFoodCategory(FoodCategory category);
    int updateFoodCategory(FoodCategory category);
    int updateFoodCategoryStatus(@Param("id") Long id, @Param("status") Integer status,
                                 @Param("version") Integer version, @Param("updatedBy") Long updatedBy);
    int softDeleteFoodCategory(@Param("id") Long id, @Param("version") Integer version,
                               @Param("updatedBy") Long updatedBy);
    int countFoodsInCategory(@Param("id") Long id);
    int countFoodCategoryCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    List<Food> selectFoods(@Param("admin") boolean admin,
                           @Param("keyword") String keyword,
                           @Param("categoryId") Long categoryId,
                           @Param("flavor") String flavor,
                           @Param("status") Integer status,
                           @Param("sort") String sort,
                           @Param("offset") long offset,
                           @Param("limit") int limit);
    long countFoods(@Param("admin") boolean admin, @Param("keyword") String keyword,
                    @Param("categoryId") Long categoryId, @Param("flavor") String flavor,
                    @Param("status") Integer status);
    Food selectFood(@Param("id") Long id);
    int insertFood(Food food);
    int updateFood(Food food);
    int updateFoodStatus(@Param("id") Long id, @Param("status") Integer status,
                         @Param("version") Integer version, @Param("updatedBy") Long updatedBy,
                         @Param("publishedAt") LocalDateTime publishedAt);
    int softDeleteFood(@Param("id") Long id, @Param("version") Integer version,
                       @Param("updatedBy") Long updatedBy);
    List<FoodImage> selectFoodImages(@Param("foodId") Long foodId);
    int deleteFoodImages(@Param("foodId") Long foodId);
    int insertFoodImage(FoodImage image);

    List<Banner> selectPublicBanners(@Param("position") String position,
                                     @Param("now") LocalDateTime now);
    List<Banner> selectBanners(@Param("position") String position,
                               @Param("status") Integer status,
                               @Param("offset") long offset,
                               @Param("limit") int limit);
    long countBanners(@Param("position") String position, @Param("status") Integer status);
    Banner selectBanner(@Param("id") Long id);
    int insertBanner(Banner banner);
    int updateBanner(Banner banner);
    int updateBannerStatus(@Param("id") Long id, @Param("status") Integer status,
                           @Param("version") Integer version, @Param("updatedBy") Long updatedBy);
    int softDeleteBanner(@Param("id") Long id, @Param("version") Integer version,
                         @Param("updatedBy") Long updatedBy);
}
