package com.aoxiaoyou.tripofmacau.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "User check-in request")
public class UserCheckinRequest {

    @NotNull(message = "poiId must not be null")
    private Long poiId;

    @NotBlank(message = "triggerMode must not be blank")
    private String triggerMode;

    private BigDecimal distanceMeters;

    private BigDecimal gpsAccuracy;

    private BigDecimal latitude;

    private BigDecimal longitude;
}
