package com.aoxiaoyou.admin.common.content;

import com.aoxiaoyou.admin.dto.response.AdminLifecycleOperationResponse;
import com.aoxiaoyou.admin.service.AdminLifecycleOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminLifecycleDueOperationRunner {

    private final AdminLifecycleOperationService lifecycleOperationService;

    public List<AdminLifecycleOperationResponse.Summary> runDueOperations(Long adminUserId, String adminUsername) {
        return lifecycleOperationService.runDueOperations(adminUserId, adminUsername);
    }
}
