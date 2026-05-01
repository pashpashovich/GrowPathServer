package by.bsuir.growpathserver.trainee.application.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import by.bsuir.growpathserver.trainee.application.service.UserAvatarStorageService;
import by.bsuir.growpathserver.trainee.infrastructure.helper.MinioHelper;

@Service
public class UserAvatarStorageServiceImpl implements UserAvatarStorageService {

    private final MinioHelper minioHelper;
    private final String userAvatarBucket;

    public UserAvatarStorageServiceImpl(MinioHelper minioHelper,
                                        @Qualifier("userAvatarBucket") String userAvatarBucket) {
        this.minioHelper = minioHelper;
        this.userAvatarBucket = userAvatarBucket;
    }

    @Override
    public String createPresignedUploadUrl(String objectKey) {
        return minioHelper.getPutPresignedUrl(userAvatarBucket, objectKey, 15 * 60);
    }

    @Override
    public Resource downloadFile(String objectKey) {
        return minioHelper.download(userAvatarBucket, objectKey);
    }
}
