package org.acme.interceptors;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

@Idempotent
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class IdempotencyInterceptor {

    @Context
    HttpHeaders headers;

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        String idempotencyKey = headers.getHeaderString("Idempotency-Key");
        
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            idempotencyKey = UUID.randomUUID().toString();
        }

        try {
            Object result = context.proceed();

            if (result instanceof Response response) {
                return Response.fromResponse(response)
                    .header("Idempotency-Key", idempotencyKey)
                    .build();
            }
            
            return result;
        } catch (Exception e) {
            throw e;
        }
    }
} 