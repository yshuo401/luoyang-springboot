package com.luoyang.tourism.module.content.service;

import com.luoyang.tourism.common.api.PageResult;
import com.luoyang.tourism.module.content.pojo.vo.ContentVos;
import java.util.List;

/**
 * PublicContentService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */
public interface PublicContentService {
    /** 执行 home 业务操作。 */
    public ContentVos.HomeVO home();

    /** 执行 categories 业务操作。 */
    public List<ContentVos.CategoryVO> categories(boolean flat);

    /** 执行 spots 业务操作。 */
    public PageResult<ContentVos.SpotCard> spots(String keyword, Long categoryId, List<Long> tagIds, String sort, Double longitude, Double latitude, Integer page, Integer pageSize);

    /** 执行 spot 业务操作。 */
    public ContentVos.SpotDetail spot(Long id, Long userId);

    /** 执行 relatedSpots 业务操作。 */
    public List<ContentVos.SpotCard> relatedSpots(Long id, Integer limit);

    /** 执行 guides 业务操作。 */
    public PageResult<ContentVos.GuideCard> guides(String keyword, String sort, Integer page, Integer pageSize);

    /** 执行 guide 业务操作。 */
    public ContentVos.GuideDetail guide(Long id, Long userId);

    /** 执行 foods 业务操作。 */
    public PageResult<ContentVos.FoodCard> foods(String keyword, Long categoryId, String flavor, String sort, Integer page, Integer pageSize);

    /** 执行 food 业务操作。 */
    public ContentVos.FoodDetail food(Long id, Long userId);

    /** 执行 search 业务操作。 */
    public PageResult<ContentVos.SearchItem> search(String keyword, String type, Integer page, Integer pageSize);
}
