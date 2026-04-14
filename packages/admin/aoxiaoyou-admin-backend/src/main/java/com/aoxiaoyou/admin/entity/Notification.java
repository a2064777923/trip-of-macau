package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notifications")
public class Notification extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    @TableField("title_zh")
    private String titleZh;

    @TableField("title_en")
    private String titleEn;

    @TableField("title_zht")
    private String titleZht;

    @TableField("title_pt")
    private String titlePt;

    @TableField("content_zh")
    private String contentZh;

    @TableField("content_en")
    private String contentEn;

    @TableField("content_zht")
    private String contentZht;

    @TableField("content_pt")
    private String contentPt;

    @TableField("notification_type")
    private String notificationType;

    @TableField("target_scope")
    private String targetScope;

    @TableField("cover_asset_id")
    private Long coverAssetId;

    @TableField("action_url")
    private String actionUrl;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("publish_start_at")
    private LocalDateTime publishStartAt;

    @TableField("publish_end_at")
    private LocalDateTime publishEndAt;
}
