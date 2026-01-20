package com.lovingapp.loving.config.openApi;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.lovingapp.loving.auth.CurrentUser;

@Configuration
public class OpenApiConfig {

    @Bean
    public OperationCustomizer hideCurrentUserParameter() {
        return (operation, handlerMethod) -> {

            var methodParameters = handlerMethod.getMethodParameters();

            if (operation.getParameters() == null) {
                return operation;
            }

            for (int i = methodParameters.length - 1; i >= 0; i--) {
                if (methodParameters[i].hasParameterAnnotation(CurrentUser.class)) {
                    if (i < operation.getParameters().size()) {
                        operation.getParameters().remove(i);
                    }
                }
            }

            return operation;
        };
    }
}
