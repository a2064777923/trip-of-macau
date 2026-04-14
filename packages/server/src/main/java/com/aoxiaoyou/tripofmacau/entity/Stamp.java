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
@TableName("stamps")
public class Stamp extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    @TableField("name_zh")
    private String nameZh;

    @TableField("name_en")
    private String nameEn;

    @TableField("name_zht")
    private String nameZht;

    @TableField("name_pt")
    private String namePt;

    @TableField("description_zh")
    private String descriptionZh;

    @TableField("description_en")
    private String descriptionEn;

    @TableField("description_zht")
    private String descriptionZht;

    @TableField("description_pt")
    private String descriptionPt;

    @TableField("stamp_type")
    private String stampType;

    private String rarity;

    @TableField("icon_asset_id")
    private Long iconAssetId;

    @TableField("related_poi_id")
    private Long relatedPoiId;

    @TableField("related_storyline_id")
    private Long relatedStorylineId;

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
