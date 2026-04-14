package com.aoxiaoyou.tripofmacau.service;

import com.aoxiaoyou.tripofmacau.dto.response.RuntimeGroupResponse;
import com.aoxiaoyou.tripofmacau.entity.AppRuntimeSetting;

import java.util.List;

public interface RuntimeSettingsService {

    List<AppRuntimeSetting> listPublishedSettingsByGroup(String group);

    RuntimeGroupResponse getRuntimeSettingsByGroup(String group, String localeHint);
}
