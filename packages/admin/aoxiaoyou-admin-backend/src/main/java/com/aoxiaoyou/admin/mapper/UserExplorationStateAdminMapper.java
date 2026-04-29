package com.aoxiaoyou.admin.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface UserExplorationStateAdminMapper {

    @Delete({
            "<script>",
            "DELETE FROM user_exploration_state",
            "WHERE user_id = #{userId}",
            "  AND scope_type = #{scopeType}",
            "  <choose>",
            "    <when test='scopeId != null'> AND scope_id = #{scopeId}</when>",
            "    <otherwise> AND scope_id IS NULL</otherwise>",
            "  </choose>",
            "</script>"
    })
    int deleteScopeState(
            @Param("userId") Long userId,
            @Param("scopeType") String scopeType,
            @Param("scopeId") Long scopeId);

    @Insert({
            "INSERT INTO user_exploration_state (",
            "  user_id, scope_type, scope_id, completed_weight, available_weight, progress_percent, computed_at, created_at, updated_at",
            ") VALUES (",
            "  #{row.userId}, #{row.scopeType}, #{row.scopeId}, #{row.completedWeight}, #{row.availableWeight},",
            "  #{row.progressPercent}, #{row.computedAt}, #{row.createdAt}, #{row.updatedAt}",
            ")"
    })
    int upsertScopeState(@Param("row") ScopeStateUpsert row);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ScopeStateUpsert {
        private Long userId;
        private String scopeType;
        private Long scopeId;
        private Integer completedWeight;
        private Integer availableWeight;
        private Double progressPercent;
        private LocalDateTime computedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
