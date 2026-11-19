package com.luoyang.tourism.job;

import com.luoyang.tourism.module.knowledge.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KnowledgeIndexScheduler {
    private final KnowledgeService knowledgeService;

    @Scheduled(initialDelay = 30000, fixedDelay = 15000)
    public void poll() {
        knowledgeService.pollRunningJobs();
    }
}
