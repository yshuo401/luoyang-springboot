package com.luoyang.tourism.module.system.service;

import com.luoyang.tourism.common.util.TimeUtils;
import com.luoyang.tourism.module.system.mapper.SystemMapper;
import com.luoyang.tourism.module.system.vo.SystemVos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final SystemMapper mapper;
    private final AppUserAdminService appUserAdminService;

    public SystemVos.DashboardVO overview() {
        List<SystemVos.AppUserAdminVO> recentUsers = mapper.selectAppUsers(null, null, null, null, 0, 5)
                .stream().map(user -> appUserAdminService.detail(user.getId())).toList();
        return new SystemVos.DashboardVO(mapper.countSpotTotal(), mapper.countSpotPublished(),
                mapper.countUserTotal(), mapper.countFavoriteTotal(), mapper.countConversationTotal(),
                mapper.countMessagesToday(), mapper.countKnowledgeTotal(), mapper.countIndexFailed(),
                recentUsers, mapper.selectSevenDayTrend());
    }
}
