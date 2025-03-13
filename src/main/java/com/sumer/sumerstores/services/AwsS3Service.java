package com.sumer.sumerstores.services;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class AwsS3Service {
    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.access}")
    private String awsS3AccessKey;

    @Value("${aws.s3.secrete}")
    private String awsS3SecretKey;
    private AmazonS3 s3Client;

    @PostConstruct
    public void init() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsS3AccessKey, awsS3SecretKey);
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.EU_NORTH_1)
                .build();
    }
    public String saveFileToS3(MultipartFile file) {
        try {
            String s3FileName = file.getOriginalFilename();
            if (s3FileName == null) {
                throw new RuntimeException("File name is null");
            }
            InputStream inputStream = file.getInputStream();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            String contentType = getContentType(s3FileName, file.getContentType());
            metadata.setContentType(contentType);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, s3FileName, inputStream, metadata);
            s3Client.putObject(putObjectRequest);
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, Regions.EU_NORTH_1.getName(), s3FileName);

        } catch (IOException e) {
            log.error("Unable to upload file to S3 bucket: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to upload file to S3 bucket: " + e.getMessage(), e);
        }
    }
    private String getContentType(String fileName, String mimeType) {
        if (fileName.toLowerCase().endsWith(".xml")) {
            return "application/xml";
        } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (fileName.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        } else {
            return mimeType != null ? mimeType : "application/octet-stream";
        }
    }
}
