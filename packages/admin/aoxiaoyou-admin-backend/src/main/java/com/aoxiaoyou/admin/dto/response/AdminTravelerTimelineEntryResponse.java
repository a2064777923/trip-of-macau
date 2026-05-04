package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminTravelerTimelineEntryResponse {
    private String entryId;
    private String entryType;
    private String sourceTable;
    private Long sourceRecordId;
    private Long userId;
    private Long storylineId;
    private String storylineName;
    private Long chapterId;
    private String chapterName;
    private Long poiId;
    private String poiName;
    private Long cityId;
    private Long subMapId;
    private String status;
    private String rewardType;
    private Long rewardId;
    private Long gameRewardId;
    private String title;
    private String summary;
    private String payloadPreview;
    private String rawPayload;
    private LocalDateTime occurredAt;
}
