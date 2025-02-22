package com.microserviceupskilling.project2;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@SpringBootApplication
public class Project2Application {
	public static void main(String[] args) {
		SpringApplication.run(Project2Application.class, args);
	}


	@Bean
	public ObjectMapper objectMapper () {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		FilterProvider filters = new SimpleFilterProvider().addFilter("detail", SimpleBeanPropertyFilter.serializeAllExcept("detail"));
		objectMapper.setFilterProvider(filters);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		return objectMapper;
	}

	@Bean
	public S3Client s3Client () {
		return S3Client.builder()
				.region(Region.EU_CENTRAL_1)
				.build();
	}

	@Bean
	public SqsClient sqsClient() {
		return SqsClient.builder()
				.region(Region.EU_CENTRAL_1)
				.build();
	}

	@Bean
	public DynamoDbClient dynamoDbClient () {
		return DynamoDbClient.builder()
				.region(Region.EU_CENTRAL_1)
				.build();
	}

	@Bean
	public SnsClient snsClient() {
		return SnsClient.builder()
				.region(Region.EU_CENTRAL_1)
				.build();
	}
}
