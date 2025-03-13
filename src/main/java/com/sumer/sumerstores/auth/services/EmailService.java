package com.sumer.sumerstores.auth.services;

import com.sumer.sumerstores.auth.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    // Assuming the XML file path
    @Value("${sanctions.xml.url}")
    private String sanctionsListFilePath;

    public String sendMail(User user) {
        String subject = "Verify your email";
        String senderName = "Sumer";
        String mailContent = "Hi " + user.getFirstName() + " " + user.getLastName() + ",\n";
        mailContent += "Your verification code is: " + user.getConfirmationCode() + "\n";
        mailContent += "Please enter this code to verify your email.";
        mailContent += "\n";
        mailContent += senderName;

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(sender);
            mailMessage.setTo(user.getEmail());
            mailMessage.setText(mailContent);
            mailMessage.setSubject(subject);
            javaMailSender.send(mailMessage);
        } catch (Exception e) {
            return "Error while Sending Mail";
        }
        return "Email sent";
    }

    public String sendSanctionedNotification(User user) {
        // Notify the user they are on the sanctions list
        String subject = "Sanctioned Notification";
        String senderName = "Sumer";
        String mailContent = "Hi " + user.getFirstName() + " " + user.getLastName() + ",\n";
        mailContent += "We regret to inform you that you are on the sanctions list.\n";
        mailContent += "As a result, you cannot purchase insurance from us.\n\n";
        mailContent += "Sumer Team";

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(sender);
            mailMessage.setTo(user.getEmail());
            mailMessage.setText(mailContent);
            mailMessage.setSubject(subject);
            javaMailSender.send(mailMessage);
        } catch (Exception e) {
            return "Error while sending sanctions notification.";
        }
        return "Sanctions notification email sent successfully.";
    }
}
