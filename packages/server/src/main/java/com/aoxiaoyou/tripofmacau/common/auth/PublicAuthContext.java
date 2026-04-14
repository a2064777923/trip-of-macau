package com.aoxiaoyou.tripofmacau.common.auth;

import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;

public final class PublicAuthContext {

    public static final String USER_ID_ATTRIBUTE = "publicUserId";
    public static final String OPEN_ID_ATTRIBUTE = "publicOpenId";
    public static final String NICKNAME_ATTRIBUTE = "publicNickname";

    private PublicAuthContext() {
    }

    public static Long requireUserId(HttpServletRequest request) {
        Object value = request.getAttribute(USER_ID_ATTRIBUTE);
        if (value instanceof Long userId) {
            return userId;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new BusinessException(4010, "Unauthorized");
    }
}
