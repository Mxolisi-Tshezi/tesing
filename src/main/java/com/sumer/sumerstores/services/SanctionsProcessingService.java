package com.sumer.sumerstores.services;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class SanctionsProcessingService {

    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${sanctions.xml.url}")
    private String sanctionsListFilePath;

    @Autowired
    public SanctionsProcessingService(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public boolean isUserSanctioned(String firstName, String lastName) {
        try {
            S3Object s3Object = s3Client.getObject(bucketName, sanctionsListFilePath);
            InputStream xmlInputStream = s3Object.getObjectContent();
            List<String> sanctionedNames = getSanctionedNamesFromStream(xmlInputStream);

            // Concatenate firstName and lastName for comparison
            String fullNameToCheck = (firstName + " " + lastName).trim();

            for (String sanctionedFullName : sanctionedNames) {
                if (sanctionedFullName.equalsIgnoreCase(fullNameToCheck)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<String> getSanctionedNamesFromStream(InputStream xmlInputStream) {
        List<String> sanctionedNames = new ArrayList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlInputStream);
            doc.getDocumentElement().normalize();
            NodeList sanctionNodes = doc.getElementsByTagName("Table");
            for (int i = 0; i < sanctionNodes.getLength(); i++) {
                var sanctionNode = sanctionNodes.item(i);
                String sanctionedFullName = getElementValue(sanctionNode, "FullName");
                sanctionedNames.add(sanctionedFullName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sanctionedNames;
    }

    private String getElementValue(org.w3c.dom.Node node, String tagName) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            org.w3c.dom.Node subNode = nodeList.item(i);
            if (subNode.getNodeName().equals(tagName)) {
                return subNode.getTextContent();
            }
        }
        return "";
    }
}
