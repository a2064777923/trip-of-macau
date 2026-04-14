package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSessionResponse {

    private String accessToken;
    private String tokenType;
    private UserStateResponse state;
}
