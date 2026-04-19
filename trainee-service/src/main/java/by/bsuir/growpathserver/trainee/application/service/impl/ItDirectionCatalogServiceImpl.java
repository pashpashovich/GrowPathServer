package by.bsuir.growpathserver.trainee.application.service.impl;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.dto.model.CreateItDirectionRequest;
import by.bsuir.growpathserver.dto.model.ItDirectionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.ItDirectionRef;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.UpdateItDirectionRequest;
import by.bsuir.growpathserver.trainee.application.service.ItDirectionCatalogService;
import by.bsuir.growpathserver.trainee.domain.entity.ItDirectionEntity;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.ProgramCatalogMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.ItDirectionRepository;
import by.bsuir.growpathserver.trainee.infrastructure.web.CatalogPathIds;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItDirectionCatalogServiceImpl implements ItDirectionCatalogService {

    private final ItDirectionRepository itDirectionRepository;
    private final ProgramCatalogMapper programCatalogMapper;

    @Override
    @Transactional(readOnly = true)
    public ItDirectionCatalogListResponse list() {
        return programCatalogMapper.toItDirectionCatalogListResponse(itDirectionRepository.findAllByOrderByCodeAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public ItDirectionRef getById(String id) {
        long lid = CatalogPathIds.parseLong(id);
        ItDirectionEntity entity = itDirectionRepository.findById(lid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return programCatalogMapper.toItDirectionRef(entity);
    }

    @Override
    @Transactional
    public ItDirectionRef create(CreateItDirectionRequest request) {
        String code = normalizeCode(requireNonBlank(request.getCode(), "code"));
        String displayName = requireNonBlank(request.getDisplayName(), "displayName");
        if (itDirectionRepository.existsByCodeIgnoreCase(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        ItDirectionEntity e = new ItDirectionEntity();
        e.setCode(code);
        e.setDisplayName(displayName);
        return programCatalogMapper.toItDirectionRef(itDirectionRepository.save(e));
    }

    @Override
    @Transactional
    public ItDirectionRef update(String id, UpdateItDirectionRequest request) {
        long lid = CatalogPathIds.parseLong(id);
        ItDirectionEntity entity = itDirectionRepository.findById(lid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String newCode = request.getCode() != null ? normalizeCode(requireNonBlank(request.getCode(), "code"))
                : entity.getCode();
        String newName = request.getDisplayName() != null
                ? requireNonBlank(request.getDisplayName(), "displayName")
                : entity.getDisplayName();
        if (!newCode.equals(entity.getCode()) && itDirectionRepository.existsByCodeIgnoreCaseAndIdNot(newCode, lid)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        entity.setCode(newCode);
        entity.setDisplayName(newName);
        return programCatalogMapper.toItDirectionRef(itDirectionRepository.save(entity));
    }

    @Override
    @Transactional
    public MessageResponse delete(String id) {
        long lid = CatalogPathIds.parseLong(id);
        ItDirectionEntity entity = itDirectionRepository.findById(lid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        itDirectionRepository.delete(entity);
        MessageResponse msg = new MessageResponse();
        msg.setMessage("Deleted");
        return msg;
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field);
        }
        return value.trim();
    }
}
