package com.microserviceupskilling.project2;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microserviceupskilling.project2.dto.ImageData;
import com.microserviceupskilling.project2.dto.UploadRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotification;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotificationRecord;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;


@RequiredArgsConstructor
@Component
public class Image {

    private static final Logger logger = LoggerFactory.getLogger(Image.class);
    private final ObjectMapper objectMapper;
    private final DynamoDbClient dynamoDbClient;

    @Bean
    public Function<APIGatewayProxyRequestEvent, Object> upload() {
        return (request) -> {
            try (S3Presigner presigner = S3Presigner.create()) {
                System.out.println("STAGING_BUCKET"+System.getenv("STAGING_BUCKET"));
                UploadRequest uploadRequest = objectMapper.readValue(request.getBody(), UploadRequest.class);
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(System.getenv("STAGING_BUCKET"))
                        .key(uploadRequest.getKey())
                        .build();
                PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(30))
                        .putObjectRequest(putObjectRequest)
                        .build();
                PresignedPutObjectRequest presignRequest = presigner.presignPutObject(putObjectPresignRequest);
                saveImage(uploadRequest);
                return presignRequest.url().toString();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch(Exception e) {
                logger.info(e.getMessage());
            }
            return null;
        };
    }

    private void saveImage(UploadRequest request) {
        ImageData imageData = ImageData.builder()
                .id(String.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)))
                .url(request.getKey())
                .owner(request.getOwner())
                .status("PENDING")
                .build();
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().n(imageData.getId()).build());
        item.put("url", AttributeValue.builder().s(imageData.getUrl()).build());
        item.put("owner", AttributeValue.builder().s(imageData.getOwner()).build());
        item.put("status", AttributeValue.builder().s(imageData.getStatus()).build());
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(System.getenv("IMAGE_TABLE"))
                .item(item)
                .build();
        dynamoDbClient.putItem(putItemRequest);
    }

    private void updateImage(String imageKey, String status){
        Map<String, AttributeValue> key = new HashMap<>();
        Map<String, AttributeValueUpdate> item = new HashMap<>();
        key.put("url", AttributeValue.builder().s(imageKey).build());
        item.put("status",AttributeValueUpdate.builder()
                        .value(AttributeValue.builder()
                                .s(status)
                                .build())
                        .build()
        );
        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                .tableName(System.getenv("IMAGE_TABLE"))
                .key(key)
                .attributeUpdates(item)
                .build();
        dynamoDbClient.updateItem(updateItemRequest);
    }

    private void getImage() {}

    @Bean
    public Function <Object,Object> initState () {
        return null;
    }


    //triggered by sqs
    @Bean
    public Function<SQSEvent.SQSMessage,Object> process() {
        return (event) -> {
            String sqsEventBody = event.getBody();
            S3EventNotification notification = S3EventNotification.fromJson(sqsEventBody);
            Optional<S3EventNotificationRecord> record = notification.getRecords().stream().findFirst();
            if(!record.isPresent()) return null;
            record.get().getS3().getObject().getKey();
            return null;
        };
        //validate
        //get image
        //try process
        //if process succeeds
        //if process fails
        //if process fails again
    }

    //check if image both exists and is of specified state
    //when image doesnt exist throw image does not exist exception
    public boolean verifyStatus () {
        return false;
    }

    //scan table for images with 2,3
    //when 3 fails delete the image from STAGINGBUCKET
    @Bean
    public Function<Object,Object> retry() {
        return null;
    }

    @Bean
    public Function<Object, Object> delete() {
        //verifyState(PROCESSED) if false delete permanently
        return null;
    }

    @Bean
    public Function<APIGatewayProxyRequestEvent,Object> view() {
        return null;
    }

    @Bean
    public Function<APIGatewayProxyRequestEvent, Object> share() {
        return (request) -> {
            //use verifyState(PROCESSED) if false image can not be shared exception
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
