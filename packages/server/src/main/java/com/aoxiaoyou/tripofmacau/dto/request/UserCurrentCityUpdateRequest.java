package com.aoxiaoyou.tripofmacau.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Current city update request")
public class UserCurrentCityUpdateRequest {

    @NotBlank(message = "cityCode must not be blank")
    private String cityCode;
}
