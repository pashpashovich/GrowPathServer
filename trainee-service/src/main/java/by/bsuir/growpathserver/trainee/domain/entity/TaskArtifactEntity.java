package by.bsuir.growpathserver.trainee.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.SoftDelete;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_artifacts")
@SoftDelete
@Getter
@Setter
@NoArgsConstructor
public class TaskArtifactEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private TaskEntity task;

    @Column(name = "artifact_type", nullable = false, length = 16)
    private String artifactType;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "content_type", length = 255)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "object_key", length = 512)
    private String objectKey;

    @Column(nullable = false, length = 1024)
    private String url;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (Objects.isNull(uploadedAt)) {
            uploadedAt = LocalDateTime.now();
        }
    }
}
