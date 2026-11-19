package com.luoyang.tourism.module.system.service.impl;

import com.luoyang.tourism.module.system.mapper.SystemMapper;
import com.luoyang.tourism.module.system.pojo.vo.SystemVos;
import com.luoyang.tourism.module.system.service.AppUserAdminService;
import com.luoyang.tourism.module.system.service.DashboardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * DashboardServiceImpl 业务实现。
 * <p>负责事务、业务规则、逻辑外键校验和外部服务编排。</p>
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final SystemMapper mapper;
    private final AppUserAdminService appUserAdminService;

@Override
public SystemVos.DashboardVO overview() {
        List<SystemVos.AppUserAdminVO> recentUsers = mapper.selectAppUsers(null, null, null, null, 0, 5)
                .stream().map(user -> appUserAdminService.detail(user.getId())).toList();
        return new SystemVos.DashboardVO(mapper.countSpotTotal(), mapper.countSpotPublished(),
                mapper.countUserTotal(), mapper.countFavoriteTotal(), mapper.countConversationTotal(),
                mapper.countMessagesToday(), mapper.countKnowledgeTotal(), mapper.countIndexFailed(),
                recentUsers, mapper.selectSevenDayTrend());
    }
}
