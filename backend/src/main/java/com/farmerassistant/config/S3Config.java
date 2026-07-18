package com.farmerassistant.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@Slf4j
public class S3Config {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.access-key:}")
    private String accessKey;

    @Value("${aws.secret-key:}")
    private String secretKey;

    @Bean
    public AmazonS3 amazonS3() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withRegion(region);
        if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
            builder.withCredentials(new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(accessKey, secretKey)));
        } else {
            builder.withCredentials(DefaultAWSCredentialsProviderChain.getInstance());
        }
        return builder.build();
    }
}
