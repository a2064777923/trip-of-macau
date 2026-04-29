package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("indoor_node_import_batches")
public class IndoorNodeImportBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("floor_id")
    private Long floorId;

    @TableField("source_filename")
    private String sourceFilename;

    @TableField("total_rows")
    private Integer totalRows;

    @TableField("valid_rows")
    private Integer validRows;

    @TableField("invalid_rows")
    private Integer invalidRows;

    @TableField("preview_payload_json")
    private String previewPayloadJson;

    @TableField("created_by_admin_id")
    private Long createdByAdminId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
