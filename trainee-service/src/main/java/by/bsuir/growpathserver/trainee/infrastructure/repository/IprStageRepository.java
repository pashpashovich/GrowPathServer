package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.IprStageEntity;

@Repository
public interface IprStageRepository extends JpaRepository<IprStageEntity, Long> {

    List<IprStageEntity> findByIprIdOrderByStageOrderAsc(Long iprId);

    Optional<IprStageEntity> findByIdAndIprId(Long id, Long iprId);
}
