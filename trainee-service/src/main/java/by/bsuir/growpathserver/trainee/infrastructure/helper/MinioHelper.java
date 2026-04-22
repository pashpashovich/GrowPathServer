package by.bsuir.growpathserver.trainee.infrastructure.helper;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioHelper {

    private final MinioClient minioClient;

    public void initBucket(String bucketName) {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        }
        catch (Exception exception) {
            log.error("Error initializing MinIO bucket {}", bucketName, exception);
            throw new IllegalStateException("Error initializing bucket", exception);
        }
    }

    public String getPutPresignedUrl(String bucket, String key, int expirationInSeconds) {
        return getPresignedUrl(bucket, key, expirationInSeconds, Method.PUT);
    }

    public String getGetPresignedUrl(String bucket, String key, int expirationInSeconds) {
        return getPresignedUrl(bucket, key, expirationInSeconds, Method.GET);
    }

    private String getPresignedUrl(String bucket, String key, int expirationInSeconds, Method method) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(method)
                            .bucket(bucket)
                            .object(key)
                            .expiry(expirationInSeconds, TimeUnit.SECONDS)
                            .build());
        }
        catch (Exception exception) {
            log.error("Error creating presigned URL for {}/{} with method {}", bucket, key, method, exception);
            throw new IllegalStateException("Error creating presigned URL", exception);
        }
    }
}
