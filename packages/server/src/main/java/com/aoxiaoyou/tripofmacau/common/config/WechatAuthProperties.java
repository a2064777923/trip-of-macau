package com.aoxiaoyou.tripofmacau.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.wechat")
public class WechatAuthProperties {

    private String miniAppId;

    private String miniAppSecret;

    private String code2sessionUrl = "https://api.weixin.qq.com/sns/jscode2session";

    private int authTimeoutMs = 5000;

    private boolean devBypassEnabled = false;
}
