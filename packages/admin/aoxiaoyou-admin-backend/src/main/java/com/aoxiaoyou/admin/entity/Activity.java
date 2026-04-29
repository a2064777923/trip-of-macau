package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activities")
public class Activity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    @TableField("activity_type")
    private String activityType;

    private String title;

    private String description;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    private String status;

    @TableField("participation_count")
    private Integer participationCount;

    @TableField("title_zh")
    private String titleZh;

    @TableField("title_en")
    private String titleEn;

    @TableField("title_zht")
    private String titleZht;

    @TableField("title_pt")
    private String titlePt;

    @TableField("summary_zh")
    private String summaryZh;

    @TableField("summary_en")
    private String summaryEn;

    @TableField("summary_zht")
    private String summaryZht;

    @TableField("summary_pt")
    private String summaryPt;

    @TableField("description_zh")
    private String descriptionZh;

    @TableField("description_en")
    private String descriptionEn;

    @TableField("description_zht")
    private String descriptionZht;

    @TableField("description_pt")
    private String descriptionPt;

    @TableField("html_zh")
    private String htmlZh;

    @TableField("html_en")
    private String htmlEn;

    @TableField("html_zht")
    private String htmlZht;

    @TableField("html_pt")
    private String htmlPt;

    @TableField("venue_name_zh")
    private String venueNameZh;

    @TableField("venue_name_en")
    private String venueNameEn;

    @TableField("venue_name_zht")
    private String venueNameZht;

    @TableField("venue_name_pt")
    private String venueNamePt;

    @TableField("address_zh")
    private String addressZh;

    @TableField("address_en")
    private String addressEn;

    @TableField("address_zht")
    private String addressZht;

    @TableField("address_pt")
    private String addressPt;

    @TableField("organizer_name")
    private String organizerName;

    @TableField("organizer_contact")
    private String organizerContact;

    @TableField("organizer_website")
    private String organizerWebsite;

    @TableField("signup_capacity")
    private Integer signupCapacity;

    @TableField("signup_fee_amount")
    private BigDecimal signupFeeAmount;

    @TableField("signup_start_at")
    private LocalDateTime signupStartAt;

    @TableField("signup_end_at")
    private LocalDateTime signupEndAt;

    @TableField("publish_start_at")
    private LocalDateTime publishStartAt;

    @TableField("publish_end_at")
    private LocalDateTime publishEndAt;

    @TableField("is_pinned")
    private Integer isPinned;

    @TableField("cover_asset_id")
    private Long coverAssetId;

    @TableField("hero_asset_id")
    private Long heroAssetId;

    @TableField("sort_order")
    private Integer sortOrder;
}
