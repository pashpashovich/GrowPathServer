package by.bsuir.growpathserver.notification.application.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.notification.application.command.AddRecipientToGroupCommand;
import by.bsuir.growpathserver.notification.application.command.CreateDistributionGroupCommand;
import by.bsuir.growpathserver.notification.application.command.DeleteDistributionGroupCommand;
import by.bsuir.growpathserver.notification.application.command.RemoveRecipientFromGroupCommand;
import by.bsuir.growpathserver.notification.application.command.UpdateDistributionGroupCommand;
import by.bsuir.growpathserver.notification.application.exception.DistributionGroupNotFoundException;
import by.bsuir.growpathserver.notification.application.exception.InvalidDistributionGroupDataException;
import by.bsuir.growpathserver.notification.application.exception.RecipientNotFoundException;
import by.bsuir.growpathserver.notification.application.query.GetDistributionGroupByIdQuery;
import by.bsuir.growpathserver.notification.application.query.GetDistributionGroupsQuery;
import by.bsuir.growpathserver.notification.application.service.DistributionGroupService;
import by.bsuir.growpathserver.notification.domain.aggregate.DistributionGroup;
import by.bsuir.growpathserver.notification.domain.entity.DistributionGroupEntity;
import by.bsuir.growpathserver.notification.domain.entity.RecipientEntity;
import by.bsuir.growpathserver.notification.infrastructure.repository.DistributionGroupRepository;
import by.bsuir.growpathserver.notification.infrastructure.repository.RecipientRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DistributionGroupServiceImpl implements DistributionGroupService {

    private final DistributionGroupRepository distributionGroupRepository;
    private final RecipientRepository recipientRepository;

    @Override
    @Transactional
    public DistributionGroup createDistributionGroup(CreateDistributionGroupCommand command) {
        DistributionGroup group = createGroupAggregate(command.name(), command.description());
        DistributionGroupEntity saved = distributionGroupRepository.save(group.toEntity());
        return DistributionGroup.fromEntity(saved);
    }

    @Override
    @Transactional
    public DistributionGroup updateDistributionGroup(UpdateDistributionGroupCommand command) {
        DistributionGroupEntity entity = requireGroup(command.id());
        if (command.name() != null) {
            entity.setName(command.name());
        }
        if (command.description() != null) {
            entity.setDescription(command.description());
        }
        DistributionGroupEntity saved = distributionGroupRepository.save(entity);
        return DistributionGroup.fromEntity(
                distributionGroupRepository.findWithRecipientsById(saved.getId()).orElse(saved));
    }

    @Override
    @Transactional
    public void deleteDistributionGroup(DeleteDistributionGroupCommand command) {
        Long groupId = command.id();
        if (!distributionGroupRepository.existsById(groupId)) {
            throw new DistributionGroupNotFoundException(groupId);
        }
        distributionGroupRepository.deleteRecipientMemberships(groupId);
        distributionGroupRepository.deleteMailingMemberships(groupId);
        distributionGroupRepository.deleteById(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public DistributionGroup getDistributionGroupById(GetDistributionGroupByIdQuery query) {
        return DistributionGroup.fromEntity(requireGroupWithRecipients(query.id()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DistributionGroup> getDistributionGroups(GetDistributionGroupsQuery query) {
        int page = query.page() != null && query.page() > 0 ? query.page() - 1 : 0;
        int limit = query.limit() != null && query.limit() > 0 ? query.limit() : 10;
        Pageable pageable = PageRequest.of(page, limit);

        return distributionGroupRepository.findAll(pageable)
                .map(entity -> DistributionGroup.fromEntity(
                        distributionGroupRepository.findWithRecipientsById(entity.getId()).orElse(entity)));
    }

    @Override
    @Transactional
    public void addRecipientToGroup(AddRecipientToGroupCommand command) {
        DistributionGroupEntity group = requireGroup(command.groupId());
        RecipientEntity recipient = recipientRepository.findWithGroupsById(command.recipientId())
                .orElseThrow(() -> new RecipientNotFoundException(command.recipientId()));

        boolean alreadyMember = recipient.getDistributionGroups().stream()
                .anyMatch(g -> g.getId().equals(group.getId()));
        if (!alreadyMember) {
            recipient.getDistributionGroups().add(group);
            recipientRepository.save(recipient);
        }
    }

    @Override
    @Transactional
    public void removeRecipientFromGroup(RemoveRecipientFromGroupCommand command) {
        requireGroup(command.groupId());
        RecipientEntity recipient = recipientRepository.findWithGroupsById(command.recipientId())
                .orElseThrow(() -> new RecipientNotFoundException(command.recipientId()));

        recipient.getDistributionGroups().removeIf(g -> g.getId().equals(command.groupId()));
        recipientRepository.save(recipient);
    }

    private DistributionGroup createGroupAggregate(String name, String description) {
        try {
            return DistributionGroup.create(name, description);
        }
        catch (IllegalArgumentException ex) {
            throw new InvalidDistributionGroupDataException(ex.getMessage());
        }
    }

    private DistributionGroupEntity requireGroup(Long id) {
        return distributionGroupRepository.findById(id)
                .orElseThrow(() -> new DistributionGroupNotFoundException(id));
    }

    private DistributionGroupEntity requireGroupWithRecipients(Long id) {
        return distributionGroupRepository.findWithRecipientsById(id)
                .orElseThrow(() -> new DistributionGroupNotFoundException(id));
    }
}
