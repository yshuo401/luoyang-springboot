package com.luoyang.tourism.module.content.service.impl;

import com.luoyang.tourism.module.appuser.service.AppUserService;
import com.luoyang.tourism.module.content.mapper.ContentMapper;
import com.luoyang.tourism.module.content.pojo.entity.ScenicCategory;
import com.luoyang.tourism.module.content.pojo.vo.ContentVos;
import com.luoyang.tourism.module.content.service.ContentAssembler;
import com.luoyang.tourism.module.file.service.FileUrlService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * 公开内容服务回归测试，重点覆盖顶层分类 parentId 为空和小程序首页空数据场景。
 */
@ExtendWith(MockitoExtension.class)
class PublicContentServiceImplTest {
    @Mock
    private ContentMapper mapper;
    @Mock
    private ContentAssembler assembler;
    @Mock
    private FileUrlService fileUrlService;
    @Mock
    private AppUserService appUserService;

    private PublicContentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PublicContentServiceImpl(mapper, assembler, fileUrlService, appUserService);
    }

    @Test
    void flatCategoriesAllowTopLevelNullParentId() {
        ScenicCategory root = category(1L, null, "龙门石窟", 1);
        when(mapper.selectCategories(false, null, null)).thenReturn(List.of(root));
        when(mapper.countPublishedSpotsInCategory(1L)).thenReturn(3);

        List<ContentVos.CategoryVO> result = service.categories(true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo("1");
        assertThat(result.get(0).parentId()).isNull();
        assertThat(result.get(0).spotCount()).isEqualTo(3);
        assertThat(result.get(0).children()).isEmpty();
    }

    @Test
    void categoryTreeBuildsParentChildRelationWhenTopLevelParentIdIsNull() {
        ScenicCategory root = category(1L, null, "人文古迹", 1);
        ScenicCategory child = category(2L, 1L, "石窟寺院", 1);
        when(mapper.selectCategories(false, null, null)).thenReturn(List.of(root, child));

        List<ContentVos.CategoryVO> result = service.categories(false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).parentId()).isNull();
        assertThat(result.get(0).children()).hasSize(1);
        assertThat(result.get(0).children().get(0).id()).isEqualTo("2");
        assertThat(result.get(0).children().get(0).parentId()).isEqualTo("1");
    }

    @Test
    void emptyCategoryTableStillReturnsFlatAndTreeEmptyCollections() {
        when(mapper.selectCategories(false, null, null)).thenReturn(List.of());

        assertThat(service.categories(true)).isEmpty();
        assertThat(service.categories(false)).isEmpty();
    }

    @Test
    void homeReturnsAllAggregateFieldsWhenDatabaseHasNoPublishedContent() {
        when(mapper.selectPublicBanners(anyString(), any(LocalDateTime.class))).thenReturn(List.of());
        when(mapper.selectCategories(false, null, null)).thenReturn(List.of());
        when(mapper.selectSpots(anyBoolean(), isNull(), isNull(), anyList(), isNull(),
                anyString(), anyLong(), anyInt())).thenReturn(List.of());
        when(mapper.selectGuides(anyBoolean(), isNull(), isNull(), anyString(), anyLong(), anyInt()))
                .thenReturn(List.of());
        when(mapper.selectFoods(anyBoolean(), isNull(), isNull(), isNull(), isNull(),
                anyString(), anyLong(), anyInt())).thenReturn(List.of());

        ContentVos.HomeVO result = service.home();

        assertThat(result.banners()).isEmpty();
        assertThat(result.categories()).isEmpty();
        assertThat(result.hotSpots()).isEmpty();
        assertThat(result.featuredGuides()).isEmpty();
        assertThat(result.featuredFoods()).isEmpty();
    }

    private ScenicCategory category(Long id, Long parentId, String name, int sortOrder) {
        ScenicCategory category = new ScenicCategory();
        category.setId(id);
        category.setParentId(parentId);
        category.setName(name);
        category.setCode("category-" + id);
        category.setSortOrder(sortOrder);
        category.setStatus(1);
        return category;
    }
}