package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reward_presentation_steps")
public class RewardPresentationStep extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("presentation_id")
    private Long presentationId;

    @TableField("step_type")
    private String stepType;

    @TableField("step_code")
    private String stepCode;

    @TableField("title_text")
    private String titleText;

    @TableField("asset_id")
    private Long assetId;

    @TableField("duration_ms")
    private Integer durationMs;

    @TableField("skippable_override")
    private Integer skippableOverride;

    @TableField("trigger_sfx_asset_id")
    private Long triggerSfxAssetId;

    @TableField("voice_over_asset_id")
    private Long voiceOverAssetId;

    @TableField("overlay_config_json")
    private String overlayConfigJson;

    @TableField("sort_order")
    private Integer sortOrder;
}
