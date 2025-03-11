package com.microserviceupskilling.project2;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microserviceupskilling.project2.dto.AuthRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityClient;
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityRequest;
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityResponse;
import software.amazon.awssdk.services.cognitoidentity.model.GetIdRequest;
import software.amazon.awssdk.services.cognitoidentity.model.GetIdResponse;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class Auth {
    private static final Logger logger = LoggerFactory.getLogger(Auth.class);

    private final ObjectMapper objectMapper;
    @Bean
    public Function<APIGatewayProxyRequestEvent, Object> getCredentials ( ObjectMapper objectMapper) {
        return (request) -> {
            String idToken = null;
            try {
                CognitoIdentityClient client = CognitoIdentityClient.builder()
                        .region(Region.EU_CENTRAL_1)
                        .build();
                idToken = objectMapper.readValue(request.getBody(), AuthRequest.class).getId();
                Map<String, String> logins = new HashMap<>();
                logins.put("cognito-idp."+ System.getenv("AWS_REGION")+ ".amazonaws.com/"+ System.getenv("COGNITO_USER_POOL_ID"),idToken);
                GetIdRequest getIdRequest = GetIdRequest.builder()
                        .identityPoolId(System.getenv("IDENTITY_POOL_ID"))
                        .logins(logins) .build();
                GetIdResponse getIdResponse = client.getId(getIdRequest);
                String identityId = getIdResponse.identityId();
                GetCredentialsForIdentityRequest getCredentialsForIdentityRequest = GetCredentialsForIdentityRequest.builder()
                        .identityId(identityId)
                        .logins(logins)
                        .build();
                GetCredentialsForIdentityResponse response = client.getCredentialsForIdentity(getCredentialsForIdentityRequest);
                Map<String,String> credentials = new HashMap<>();
                credentials.put("accessKeyId",response.credentials().accessKeyId());
                credentials.put("sessionToken",response.credentials().sessionToken());
                credentials.put("secretKey",response.credentials().secretKey());
                return credentials;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Bean
    public Function<APIGatewayProxyRequestEvent, Object>  subscribe() {
        return null;
    }
}
