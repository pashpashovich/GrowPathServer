package by.bsuir.growpathserver.trainee.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import by.bsuir.growpathserver.trainee.infrastructure.helper.MinioHelper;

@Configuration
public class MinioBucketConfig {

    @Bean(name = "taskArtifactBucket")
    public String taskArtifactBucket(MinioHelper minioHelper,
                                     @Value("${minio.bucket-name}") String bucketName) {
        try {
            minioHelper.initBucket(bucketName);
            return bucketName;
        }
        catch (IllegalStateException e) {
            throw new IllegalStateException("Failed to initialize trainee task artifact bucket: " + bucketName, e);
        }
    }
}
