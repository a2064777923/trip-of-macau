package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.common.api.PageResponse;
import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import com.aoxiaoyou.tripofmacau.entity.TestAccount;
import com.aoxiaoyou.tripofmacau.entity.User;
import com.aoxiaoyou.tripofmacau.mapper.TestAccountMapper;
import com.aoxiaoyou.tripofmacau.mapper.UserMapper;
import com.aoxiaoyou.tripofmacau.service.TestAccountService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TestAccountServiceImpl implements TestAccountService {
    
    private final TestAccountMapper testAccountMapper;
    private final UserMapper userMapper;
    
    @Override
    public PageResponse<TestAccount> pageTestAccounts(long pageNum, long pageSize, String testGroup) {
        LambdaQueryWrapper<TestAccount> queryWrapper = new LambdaQueryWrapper<>();
        if (testGroup != null && !testGroup.isEmpty()) {
            queryWrapper.eq(TestAccount::getTestGroup, testGroup);
        }
        queryWrapper.orderByDesc(TestAccount::getCreatedAt);
        
        Page<TestAccount> page = testAccountMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        
        return new PageResponse<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }
    
    @Override
    public TestAccount getById(Long id) {
        TestAccount testAccount = testAccountMapper.selectById(id);
        if (testAccount == null) {
            throw new BusinessException("测试账号不存在");
        }
        return testAccount;
    }
    
    @Override
    @Transactional
    public TestAccount create(TestAccount testAccount) {
        testAccount.setCreatedAt(LocalDateTime.now());
        testAccount.setUpdatedAt(LocalDateTime.now());
        testAccount.setDeleted(0);
        testAccountMapper.insert(testAccount);
        return testAccount;
    }
    
    @Override
    @Transactional
    public TestAccount update(Long id, TestAccount testAccount) {
        TestAccount existing = testAccountMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("测试账号不存在");
        }
        testAccount.setId(id);
        testAccount.setUpdatedAt(LocalDateTime.now());
        testAccountMapper.updateById(testAccount);
        return testAccountMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        testAccountMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public TestAccount toggleMockLocation(Long id, boolean enabled, Double latitude, Double longitude, Long poiId) {
        TestAccount testAccount = testAccountMapper.selectById(id);
        if (testAccount == null) {
            throw new BusinessException("测试账号不存在");
        }
        
        testAccount.setMockEnabled(enabled);
        if (enabled && latitude != null && longitude != null) {
            testAccount.setMockLatitude(latitude);
            testAccount.setMockLongitude(longitude);
            testAccount.setMockPoiId(poiId);
        }
        testAccount.setUpdatedAt(LocalDateTime.now());
        testAccountMapper.updateById(testAccount);
        
        return testAccount;
    }
    
    @Override
    @Transactional
    public void adjustUserExp(Long userId, int expDelta, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 计算新的经验值
        int newExp = (user.getTotalStamps() != null ? user.getTotalStamps() : 0) + expDelta;
        userMapper.update(null,
                new LambdaUpdateWrapper<User>()
                        .eq(User::getId, userId)
                        .set(newExp >= 0, User::getTotalStamps, newExp)
        );
    }
    
    @Override
    @Transactional
    public void resetUserStamps(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userMapper.update(null,
                new LambdaUpdateWrapper<User>()
                        .eq(User::getId, userId)
                        .set(User::getTotalStamps, 0)
                        .set(User::getLevel, 1)
                        .set(User::getTitle, "探索新手")
        );
    }
    
    @Override
    @Transactional
    public void addUserStamp(Long userId, String stampType, Long sourceId) {
        // TODO: 实现添加印章逻辑
    }
}
