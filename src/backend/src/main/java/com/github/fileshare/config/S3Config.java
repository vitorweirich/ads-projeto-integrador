package com.github.fileshare.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

	@Bean
	public S3Presigner createPresigner (@Value("${CLOUDFLARE_R2_ENDPOINT}") URI cloudflareEndpoint,
			@Value("${AWS_ACCESS_KEY_ID}") String accessKeyId,
			@Value("${AWS_SECRET_ACCESS_KEY}") String secretAccessKey,
			@Value("${AWS_REGION:auto}") String region) {

		System.setProperty("aws.accessKeyId", accessKeyId);
	    System.setProperty("aws.secretAccessKey", secretAccessKey);
		
		SystemPropertyCredentialsProvider create = SystemPropertyCredentialsProvider.create();
		
		return S3Presigner.builder()
				// TODO: Colocar region em variavel de ambiente
				.region(Region.of(region))
				.credentialsProvider(create)
				.endpointOverride(cloudflareEndpoint)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
				.build();
	}
	
	@Bean
	public S3Client createClient (@Value("${CLOUDFLARE_R2_ENDPOINT}") URI cloudflareEndpoint,
			@Value("${AWS_ACCESS_KEY_ID}") String accessKeyId,
			@Value("${AWS_SECRET_ACCESS_KEY}") String secretAccessKey,
			@Value("${AWS_REGION:auto}") String region) {
		
		System.setProperty("aws.accessKeyId", accessKeyId);
	    System.setProperty("aws.secretAccessKey", secretAccessKey);
		
		SystemPropertyCredentialsProvider create = SystemPropertyCredentialsProvider.create();
		
		return S3Client.builder()
				.region(Region.of(region))
				.credentialsProvider(create)
				.endpointOverride(cloudflareEndpoint)
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
				.build();
	}
	
}
