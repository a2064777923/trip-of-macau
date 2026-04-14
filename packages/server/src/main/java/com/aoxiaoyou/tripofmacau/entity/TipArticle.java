package com.aoxiaoyou.tripofmacau.entity;

import com.aoxiaoyou.tripofmacau.common.enums.ContentStatus;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tip_articles")
public class TipArticle extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("city_id")
    private Long cityId;

    private String code;

    @TableField("category_code")
    private String categoryCode;

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

    @TableField("content_zh")
    private String contentZh;

    @TableField("content_en")
    private String contentEn;

    @TableField("content_zht")
    private String contentZht;

    @TableField("content_pt")
    private String contentPt;

    @TableField("author_display_name")
    private String authorDisplayName;

    @TableField("location_name_zh")
    private String locationNameZh;

    @TableField("location_name_en")
    private String locationNameEn;

    @TableField("location_name_zht")
    private String locationNameZht;

    @TableField("location_name_pt")
    private String locationNamePt;

    @TableField("tags_json")
    private String tagsJson;

    @TableField("cover_asset_id")
    private Long coverAssetId;

    @TableField("source_type")
    private String sourceType;

    @TableField("status")
    private String statusCode;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    public ContentStatus getStatus() {
        return statusCode == null || statusCode.isBlank() ? null : ContentStatus.fromCode(statusCode);
    }

    public void setStatus(ContentStatus status) {
        this.statusCode = status == null ? null : status.getCode();
    }
}
