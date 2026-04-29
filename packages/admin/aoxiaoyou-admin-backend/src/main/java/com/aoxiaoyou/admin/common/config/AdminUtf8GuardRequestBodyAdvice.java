package com.aoxiaoyou.admin.common.config;

import com.aoxiaoyou.admin.common.encoding.SuspiciousTextGuard;
import com.aoxiaoyou.admin.common.encoding.SuspiciousTextIssue;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;
import java.util.Optional;

@RestControllerAdvice(basePackages = "com.aoxiaoyou.admin.controller")
public class AdminUtf8GuardRequestBodyAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object afterBodyRead(
            Object body,
            HttpInputMessage inputMessage,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        Optional<SuspiciousTextIssue> issue = SuspiciousTextGuard.findFirstIssue(body);
        if (issue.isPresent()) {
            SuspiciousTextIssue suspiciousTextIssue = issue.get();
            throw new BusinessException(
                    4003,
                    "检测到疑似乱码，请改用 UTF-8 重新提交。字段: "
                            + suspiciousTextIssue.path()
                            + "，原因: "
                            + suspiciousTextIssue.reason()
                            + "，内容片段: "
                            + suspiciousTextIssue.preview()
            );
        }
        return body;
    }
}
