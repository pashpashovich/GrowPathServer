package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.ItDirectionEntity;

@Repository
public interface ItDirectionRepository extends JpaRepository<ItDirectionEntity, Long> {

    List<ItDirectionEntity> findAllByOrderByCodeAsc();

}
