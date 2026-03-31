package com.github.fileshare.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

	@Bean
	public S3Presigner createPresigner (@Value("${app.s3.endpoint}") URI endpoint,
			@Value("${app.s3.access-key}") String accessKeyId,
			@Value("${app.s3.secret-key}") String secretAccessKey,
			@Value("${app.s3.region}") String region) {

		StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKeyId, secretAccessKey));
		
		return S3Presigner.builder()
				.region(Region.of(region))
				.credentialsProvider(credentials)
				.endpointOverride(endpoint)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
				.build();
	}
	
	@Bean
	public S3Client createClient (@Value("${app.s3.endpoint}") URI endpoint,
			@Value("${app.s3.access-key}") String accessKeyId,
			@Value("${app.s3.secret-key}") String secretAccessKey,
			@Value("${app.s3.region}") String region) {
		
		StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKeyId, secretAccessKey));
		
		return S3Client.builder()
				.region(Region.of(region))
				.credentialsProvider(credentials)
				.endpointOverride(endpoint)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
				.build();
	}
	
}
