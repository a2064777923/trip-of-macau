package com.aoxiaoyou.tripofmacau.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Bootstrap check-in snapshot")
public class UserBootstrapCheckinRequest {

    private Long poiId;

    private String triggerMode;

    private String checkedAt;
}
