package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reward_presentations")
public class RewardPresentation extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    @TableField("name_zh")
    private String nameZh;

    @TableField("name_zht")
    private String nameZht;

    @TableField("presentation_type")
    private String presentationType;

    @TableField("first_time_only")
    private Integer firstTimeOnly;

    private Integer skippable;

    @TableField("minimum_display_ms")
    private Integer minimumDisplayMs;

    @TableField("interrupt_policy")
    private String interruptPolicy;

    @TableField("queue_policy")
    private String queuePolicy;

    @TableField("priority_weight")
    private Integer priorityWeight;

    @TableField("cover_asset_id")
    private Long coverAssetId;

    @TableField("voice_over_asset_id")
    private Long voiceOverAssetId;

    @TableField("sfx_asset_id")
    private Long sfxAssetId;

    @TableField("summary_text")
    private String summaryText;

    @TableField("config_json")
    private String configJson;

    private String status;
}
