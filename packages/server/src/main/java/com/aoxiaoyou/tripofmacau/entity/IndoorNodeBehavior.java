package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("indoor_node_behaviors")
public class IndoorNodeBehavior {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("node_id")
    private Long nodeId;

    @TableField("behavior_code")
    private String behaviorCode;

    @TableField("behavior_name_zh")
    private String behaviorNameZh;

    @TableField("behavior_name_en")
    private String behaviorNameEn;

    @TableField("behavior_name_zht")
    private String behaviorNameZht;

    @TableField("behavior_name_pt")
    private String behaviorNamePt;

    @TableField("appearance_preset_code")
    private String appearancePresetCode;

    @TableField("trigger_template_code")
    private String triggerTemplateCode;

    @TableField("effect_template_code")
    private String effectTemplateCode;

    @TableField("appearance_rules_json")
    private String appearanceRulesJson;

    @TableField("trigger_rules_json")
    private String triggerRulesJson;

    @TableField("effect_rules_json")
    private String effectRulesJson;

    @TableField("path_graph_json")
    private String pathGraphJson;

    @TableField("overlay_geometry_json")
    private String overlayGeometryJson;

    @TableField("inherit_mode")
    private String inheritMode;

    @TableField("runtime_support_level")
    private String runtimeSupportLevel;

    @TableField("sort_order")
    private Integer sortOrder;

    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
