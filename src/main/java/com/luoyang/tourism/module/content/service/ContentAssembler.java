package com.luoyang.tourism.module.content.service;

import com.luoyang.tourism.module.content.pojo.entity.Banner;
import com.luoyang.tourism.module.content.pojo.entity.Food;
import com.luoyang.tourism.module.content.pojo.entity.ScenicSpot;
import com.luoyang.tourism.module.content.pojo.entity.Tag;
import com.luoyang.tourism.module.content.pojo.entity.TravelGuide;
import com.luoyang.tourism.module.content.pojo.vo.ContentVos;
import java.util.List;

/**
 * ContentAssembler 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */
public interface ContentAssembler {
    /** 执行 tag 业务操作。 */
    public ContentVos.TagVO tag(Tag tag);

    /** 执行 spotTags 业务操作。 */
    public List<ContentVos.TagVO> spotTags(Long spotId);

    /** 执行 spotCard 业务操作。 */
    public ContentVos.SpotCard spotCard(ScenicSpot spot);

    /** 执行 spotCard 业务操作。 */
    public ContentVos.SpotCard spotCard(ScenicSpot spot, Double userLongitude, Double userLatitude);

    /** 执行 spotDetail 业务操作。 */
    public ContentVos.SpotDetail spotDetail(ScenicSpot spot);

    /** 执行 spotDetail 业务操作。 */
    public ContentVos.SpotDetail spotDetail(ScenicSpot spot, Boolean favorite);

    /** 执行 guideCard 业务操作。 */
    public ContentVos.GuideCard guideCard(TravelGuide guide);

    /** 执行 guideDetail 业务操作。 */
    public ContentVos.GuideDetail guideDetail(TravelGuide guide, boolean publicOnly);

    /** 执行 guideDetail 业务操作。 */
    public ContentVos.GuideDetail guideDetail(TravelGuide guide, boolean publicOnly, Boolean favorite);

    /** 执行 foodCard 业务操作。 */
    public ContentVos.FoodCard foodCard(Food food);

    /** 执行 foodDetail 业务操作。 */
    public ContentVos.FoodDetail foodDetail(Food food);

    /** 执行 foodDetail 业务操作。 */
    public ContentVos.FoodDetail foodDetail(Food food, Boolean favorite);

    /** 执行 banner 业务操作。 */
    public ContentVos.BannerVO banner(Banner banner);

    /** 执行 adminSpot 业务操作。 */
    public ContentVos.AdminSpot adminSpot(ScenicSpot spot);

    /** 执行 adminGuide 业务操作。 */
    public ContentVos.AdminGuide adminGuide(TravelGuide guide);

    /** 执行 adminFood 业务操作。 */
    public ContentVos.AdminFood adminFood(Food food);

    /** 执行 adminBanner 业务操作。 */
    public ContentVos.AdminBanner adminBanner(Banner banner);

    /** 执行 flavorTags 业务操作。 */
    public List<String> flavorTags(String json);
}
