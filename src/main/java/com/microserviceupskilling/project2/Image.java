package com.microserviceupskilling.project2;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microserviceupskilling.project2.dto.UploadRequest;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.function.Function;


@RequiredArgsConstructor
@Component
public class Image {

    private static final Logger logger = LoggerFactory.getLogger(Image.class);
    private final ObjectMapper objectMapper;

    @Bean
    public Function<APIGatewayProxyRequestEvent, Object> upload() {
        return (request) -> {
            try (S3Presigner presigner = S3Presigner.create()) {
                System.out.println("STAGING_BUCKET"+System.getenv("STAGING_BUCKET"));
                String key = key = objectMapper.readValue(request.getBody(), UploadRequest.class).getKey();
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(System.getenv("STAGING_BUCKET"))
                        .key(key)
                        .build();
                PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(30))
                        .putObjectRequest(putObjectRequest)
                        .build();
                PresignedPutObjectRequest presignRequest = presigner.presignPutObject(putObjectPresignRequest);
                return presignRequest.url().toString();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch(Exception e) {
                logger.info(e.getMessage());
            }
            return null;
        };
    }

    @Bean
    public Function <Object,Object> initState () {
        return null;
    }

    @Bean
    public Function<Object,Object> processing() {
        //get image
        //
        return null;
    }

    public Function<Object, Object> delete() {

        return null;
    }

    @Bean
    public Function<APIGatewayProxyRequestEvent,Object> view() {
        return null;
    }

    @Bean
    public Function<APIGatewayProxyRequestEvent, Object> share() {
        return (request) -> {
            try (S3Presigner presigner = S3Presigner.create()) {
                System.out.println("PRIMARY_BUCKET"+System.getenv("PRIMARY_BUCKET"));
                String key = key = objectMapper.readValue(request.getBody(), UploadRequest.class).getKey();
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(System.getenv("PRIMARY_BUCKET"))
                        .key(key)
                        .build();
                GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(30))
                        .getObjectRequest(getObjectRequest)
                        .build();
                PresignedGetObjectRequest presignRequest = presigner.presignGetObject(getObjectPresignRequest);
                return presignRequest.url().toString();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch(Exception e) {
                logger.info(e.getMessage());
            }
            return null;
        };
    }
}
