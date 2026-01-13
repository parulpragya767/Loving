package com.lovingapp.loving.auth;

import java.util.UUID;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.lovingapp.loving.model.dto.UserDTOs.UserDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthContext authContext;

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory) {

        Class<?> type = parameter.getParameterType();

        UserDTO user;
        try {
            user = authContext.getAppUser();
        } catch (Exception e) {
            log.error("Failed to resolve @CurrentUser from auth context");
            throw e;
        }

        if (UserDTO.class.isAssignableFrom(type)) {
            return user;
        }

        if (UUID.class.isAssignableFrom(type)) {
            return user.getId();
        }

        log.error("Unsupported @CurrentUser parameter type type={}", type.getName());
        throw new IllegalArgumentException("Unsupported @CurrentUser parameter type: " + type.getName());
    }
}
