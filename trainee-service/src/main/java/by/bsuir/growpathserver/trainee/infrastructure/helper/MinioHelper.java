package by.bsuir.growpathserver.trainee.infrastructure.helper;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioHelper {

    private final MinioClient minioClient;

    @Value("${minio.gateway-url}")
    private String gatewayUrl;

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

    public void upload(String bucket, String key, InputStream stream, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(stream, size, -1)
                            .contentType(contentType)
                            .build());
        }
        catch (Exception exception) {
            log.error("Error uploading file to {}/{}", bucket, key, exception);
            throw new IllegalStateException("Error uploading file to MinIO", exception);
        }
    }

    public Resource download(String bucket, String key) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build());
            return new InputStreamResource(stream);
        }
        catch (Exception exception) {
            log.error("Error downloading file from {}/{}", bucket, key, exception);
            throw new IllegalStateException("Error downloading file from MinIO", exception);
        }
    }

    private String getPresignedUrl(String bucket, String key, int expirationInSeconds, Method method) {
        try {
            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(method)
                            .bucket(bucket)
                            .object(key)
                            .expiry(expirationInSeconds, TimeUnit.SECONDS)
                            .build());

            java.net.URI uri = new java.net.URI(presignedUrl);
            return gatewayUrl + uri.getPath() + "?" + (uri.getQuery() != null ? uri.getQuery() : "");
        }
        catch (Exception exception) {
            log.error("Error creating presigned URL for {}/{} with method {}", bucket, key, method, exception);
            throw new IllegalStateException("Error creating presigned URL", exception);
        }
    }
}
