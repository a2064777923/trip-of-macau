package com.aoxiaoyou.tripofmacau.service;

import com.aoxiaoyou.tripofmacau.common.api.PageResponse;
import com.aoxiaoyou.tripofmacau.entity.TestAccount;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface TestAccountService {
    
    /**
     * 分页获取测试账号列表
     */
    PageResponse<TestAccount> pageTestAccounts(long pageNum, long pageSize, String testGroup);
    
    /**
     * 获取测试账号详情
     */
    TestAccount getById(Long id);
    
    /**
     * 创建测试账号
     */
    TestAccount create(TestAccount testAccount);
    
    /**
     * 更新测试账号
     */
    TestAccount update(Long id, TestAccount testAccount);
    
    /**
     * 删除测试账号
     */
    void delete(Long id);
    
    /**
     * 切换模拟定位状态
     */
    TestAccount toggleMockLocation(Long id, boolean enabled, Double latitude, Double longitude, Long poiId);
    
    /**
     * 调整用户经验值
     */
    void adjustUserExp(Long userId, int expDelta, String reason);
    
    /**
     * 重置用户印章
     */
    void resetUserStamps(Long userId);
    
    /**
     * 添加印章
     */
    void addUserStamp(Long userId, String stampType, Long sourceId);
}
