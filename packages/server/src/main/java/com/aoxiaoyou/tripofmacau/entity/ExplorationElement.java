package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exploration_elements")
public class ExplorationElement extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("element_code")
    private String elementCode;
    @TableField("element_type")
    private String elementType;
    @TableField("owner_type")
    private String ownerType;
    @TableField("owner_id")
    private Long ownerId;
    @TableField("owner_code")
    private String ownerCode;
    @TableField("city_id")
    private Long cityId;
    @TableField("sub_map_id")
    private Long subMapId;
    @TableField("storyline_id")
    private Long storylineId;
    @TableField("story_chapter_id")
    private Long storyChapterId;
    @TableField("title_zh")
    private String titleZh;
    @TableField("title_en")
    private String titleEn;
    @TableField("title_zht")
    private String titleZht;
    @TableField("title_pt")
    private String titlePt;
    @TableField("weight_level")
    private String weightLevel;
    @TableField("weight_value")
    private Integer weightValue;
    @TableField("include_in_exploration")
    private Boolean includeInExploration;
    @TableField("metadata_json")
    private String metadataJson;
    private String status;
    @TableField("sort_order")
    private Integer sortOrder;
}
