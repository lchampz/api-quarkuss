package org.acme.interceptors;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Provider
@Interceptor
@ApiKey
@Priority(1000)
public class ApiKeyInterceptor {

    @Context
    HttpHeaders headers;

    @ConfigProperty(name = "quarkus.api-key.value")
    String validApiValue;

    @ConfigProperty(name = "quarkus.api-key.header-name", defaultValue = "X-API-Key")
    String headerKey;

    @AroundInvoke
    public Object validateApiKey(InvocationContext context) throws Exception {
        String apiKey = headers.getHeaderString(headerKey);

        if (apiKey == null || apiKey.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("API Key não fornecida")
                    .build();
        }

        if (!apiKey.equals(validApiValue)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("API Key inválida")
                    .build();
        }

        return context.proceed();
    }
}