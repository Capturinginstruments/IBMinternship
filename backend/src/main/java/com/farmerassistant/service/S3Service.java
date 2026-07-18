package com.farmerassistant.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.hasText(originalFilename) && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String key = folder + "/" + UUID.randomUUID() + extension;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3.putObject(new PutObjectRequest(bucketName, key, file.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        String url = amazonS3.getUrl(bucketName, key).toString();
        log.info("Uploaded file to S3: {}", url);
        return url;
    }

    public String uploadBytes(byte[] bytes, String folder, String contentType) {
        String extension = contentType != null && contentType.contains("/")
                ? "." + contentType.split("/")[1] : ".jpg";
        String key = folder + "/" + UUID.randomUUID() + extension;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(bytes.length);

        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
        amazonS3.putObject(new PutObjectRequest(bucketName, key, bais, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        return amazonS3.getUrl(bucketName, key).toString();
    }

    public void deleteFile(String s3Key) {
        try {
            amazonS3.deleteObject(bucketName, s3Key);
            log.info("Deleted S3 object: {}", s3Key);
        } catch (Exception e) {
            log.warn("Failed to delete S3 object {}: {}", s3Key, e.getMessage());
        }
    }
}
