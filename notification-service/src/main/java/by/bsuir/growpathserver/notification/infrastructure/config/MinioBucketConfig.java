package by.bsuir.growpathserver.notification.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import by.bsuir.growpathserver.notification.infrastructure.helper.MinioHelper;

@Configuration
public class MinioBucketConfig {

    @Bean(name = "emailTemplateAttachmentBucket")
    public String emailTemplateAttachmentBucket(MinioHelper minioHelper,
                                                @Value("${minio.attachment-bucket-name}") String bucketName) {
        try {
            minioHelper.initBucket(bucketName);
            return bucketName;
        }
        catch (IllegalStateException e) {
            throw new IllegalStateException("Failed to initialize email attachment bucket: " + bucketName, e);
        }
    }
}
