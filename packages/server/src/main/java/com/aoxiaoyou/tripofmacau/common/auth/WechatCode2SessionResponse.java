package com.aoxiaoyou.tripofmacau.common.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class WechatCode2SessionResponse {

    private String openid;

    private String unionid;

    @JsonProperty("session_key")
    private String sessionKey;

    private Integer errcode;

    private String errmsg;

    public boolean hasError() {
        return errcode != null && errcode != 0;
    }

    public boolean hasOpenId() {
        return StringUtils.hasText(openid);
    }
}
