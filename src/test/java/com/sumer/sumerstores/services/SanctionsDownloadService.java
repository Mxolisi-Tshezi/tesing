package com.sumer.sumerstores.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Service
@Slf4j
public class SanctionsDownloadService {

    @Value("${sanctions.xml.url}")
    private String sanctionsXmlUrl;

    private final RestTemplate restTemplate;
    private final AwsS3Service awsS3Service;

    @Autowired
    public SanctionsDownloadService(RestTemplate restTemplate, AwsS3Service awsS3Service) {
        this.restTemplate = restTemplate;
        this.awsS3Service = awsS3Service;
    }

    @Scheduled(fixedRate = 1209600000)
    public void downloadSanctionsFile() {
        try {
            byte[] xmlData = restTemplate.getForObject(sanctionsXmlUrl, byte[].class);
            if (xmlData != null) {
                MultipartFile file = createMultipartFile(xmlData);
                String fileUrl = awsS3Service.saveFileToS3(file);

                log.info("Sanctions XML file uploaded to S3 successfully: {}", fileUrl);
            }
        } catch (Exception e) {
            log.error("Error downloading sanctions XML file: {}", e.getMessage());
        }
    }

    private MultipartFile createMultipartFile(byte[] content) throws IOException {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "sanctions.xml";
            }

            @Override
            public String getOriginalFilename() {
                return "sanctions.xml";
            }

            @Override
            public String getContentType() {
                return "application/xml";
            }

            @Override
            public boolean isEmpty() {
                return content.length == 0;
            }

            @Override
            public long getSize() {
                return content.length;
            }

            @Override
            public byte[] getBytes() {
                return content;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(content);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.copy(new ByteArrayInputStream(content), dest.toPath());
            }
        };
    }
}
