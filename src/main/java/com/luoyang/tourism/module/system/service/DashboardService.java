package com.luoyang.tourism.module.system.service;

import com.luoyang.tourism.module.system.pojo.vo.SystemVos;

/**
 * DashboardService 业务接口。
 * <p>定义该领域的业务能力，具体实现位于 service.impl 包。</p>
 */

public interface DashboardService {
    /** 查询仪表盘统计。 */
    public SystemVos.DashboardVO overview();
}
