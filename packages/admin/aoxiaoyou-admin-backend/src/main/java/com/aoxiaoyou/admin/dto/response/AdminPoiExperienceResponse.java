package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminPoiExperienceResponse {

    @Data
    @Builder
    public static class Snapshot {
        private PoiSummary poi;
        private Flow flow;
        private Binding binding;
        private List<Step> steps;
        private List<AdminExperienceResponse.Template> templates;
        private List<ValidationFinding> validationFindings;
        private String publicRuntimePath;
    }

    @Data
    @Builder
    public static class PoiSummary {
        private Long poiId;
        private Long cityId;
        private Long subMapId;
        private String code;
        private String nameZh;
        private String nameZht;
        private String nameEn;
        private String namePt;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Integer triggerRadius;
        private Integer manualCheckinRadius;
        private Integer staySeconds;
        private Long coverAssetId;
        private Long mapIconAssetId;
        private Long audioAssetId;
        private String status;
    }

    @Data
    @Builder
    public static class Flow {
        private Long id;
        private String code;
        private String flowType;
        private String mode;
        private String nameZh;
        private String nameZht;
        private String nameEn;
        private String namePt;
        private String descriptionZh;
        private String descriptionZht;
        private String descriptionEn;
        private String descriptionPt;
        private String mapPolicyJson;
        private String advancedConfigJson;
        private String status;
        private Integer sortOrder;
        private LocalDateTime publishedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class Binding {
        private Long id;
        private String ownerType;
        private Long ownerId;
        private String ownerCode;
        private String bindingRole;
        private Long flowId;
        private Integer priority;
        private String inheritPolicy;
        private String status;
        private Integer sortOrder;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class Step {
        private Long id;
        private Long flowId;
        private String stepCode;
        private String stepType;
        private Long templateId;
        private AdminExperienceResponse.Template template;
        private String stepNameZh;
        private String stepNameZht;
        private String stepNameEn;
        private String stepNamePt;
        private String descriptionZh;
        private String descriptionZht;
        private String descriptionEn;
        private String descriptionPt;
        private String triggerType;
        private String triggerConfigJson;
        private String conditionConfigJson;
        private String effectConfigJson;
        private Long mediaAssetId;
        private String rewardRuleIdsJson;
        private String explorationWeightLevel;
        private Boolean requiredForCompletion;
        private String inheritKey;
        private String status;
        private Integer sortOrder;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class ValidationFinding {
        private String severity;
        private String findingType;
        private String title;
        private String description;
        private Long stepId;
        private String stepCode;
    }
}
