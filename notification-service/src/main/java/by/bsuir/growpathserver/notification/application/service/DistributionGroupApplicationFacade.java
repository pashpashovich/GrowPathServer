package by.bsuir.growpathserver.notification.application.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import by.bsuir.growpathserver.dto.model.AddRecipientToDistributionGroupRequest;
import by.bsuir.growpathserver.dto.model.CreateDistributionGroupRequest;
import by.bsuir.growpathserver.dto.model.DistributionGroupListResponse;
import by.bsuir.growpathserver.dto.model.DistributionGroupResponse;
import by.bsuir.growpathserver.dto.model.RecipientListResponse;
import by.bsuir.growpathserver.dto.model.UpdateDistributionGroupRequest;
import by.bsuir.growpathserver.notification.application.command.AddRecipientToGroupCommand;
import by.bsuir.growpathserver.notification.application.command.CreateDistributionGroupCommand;
import by.bsuir.growpathserver.notification.application.command.DeleteDistributionGroupCommand;
import by.bsuir.growpathserver.notification.application.command.RemoveRecipientFromGroupCommand;
import by.bsuir.growpathserver.notification.application.command.UpdateDistributionGroupCommand;
import by.bsuir.growpathserver.notification.application.handler.AddRecipientToGroupHandler;
import by.bsuir.growpathserver.notification.application.handler.CreateDistributionGroupHandler;
import by.bsuir.growpathserver.notification.application.handler.DeleteDistributionGroupHandler;
import by.bsuir.growpathserver.notification.application.handler.GetDistributionGroupByIdHandler;
import by.bsuir.growpathserver.notification.application.handler.GetDistributionGroupsHandler;
import by.bsuir.growpathserver.notification.application.handler.RemoveRecipientFromGroupHandler;
import by.bsuir.growpathserver.notification.application.handler.UpdateDistributionGroupHandler;
import by.bsuir.growpathserver.notification.application.query.GetDistributionGroupByIdQuery;
import by.bsuir.growpathserver.notification.application.query.GetDistributionGroupsQuery;
import by.bsuir.growpathserver.notification.application.support.NotificationIds;
import by.bsuir.growpathserver.notification.domain.aggregate.DistributionGroup;
import by.bsuir.growpathserver.notification.domain.aggregate.Recipient;
import by.bsuir.growpathserver.notification.infrastructure.mapper.DistributionGroupMapper;
import by.bsuir.growpathserver.notification.infrastructure.mapper.RecipientMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DistributionGroupApplicationFacade {

    private final CreateDistributionGroupHandler createDistributionGroupHandler;
    private final GetDistributionGroupByIdHandler getDistributionGroupByIdHandler;
    private final GetDistributionGroupsHandler getDistributionGroupsHandler;
    private final UpdateDistributionGroupHandler updateDistributionGroupHandler;
    private final DeleteDistributionGroupHandler deleteDistributionGroupHandler;
    private final AddRecipientToGroupHandler addRecipientToGroupHandler;
    private final RemoveRecipientFromGroupHandler removeRecipientFromGroupHandler;
    private final RecipientService recipientService;
    private final DistributionGroupMapper distributionGroupMapper;
    private final RecipientMapper recipientMapper;

    public void addRecipientToDistributionGroup(String groupId, AddRecipientToDistributionGroupRequest request) {
        addRecipientToGroupHandler.handle(new AddRecipientToGroupCommand(
                NotificationIds.parseRequired(groupId, "id"),
                NotificationIds.parseRequired(request.getRecipientId(), "recipientId")
        ));
    }

    public DistributionGroupResponse createDistributionGroup(CreateDistributionGroupRequest request) {
        DistributionGroup group = createDistributionGroupHandler.handle(CreateDistributionGroupCommand.builder()
                                                                                .name(request.getName())
                                                                                .description(request.getDescription())
                                                                                .build());
        return distributionGroupMapper.toDistributionGroupResponse(group);
    }

    public void deleteDistributionGroup(String id) {
        deleteDistributionGroupHandler.handle(
                new DeleteDistributionGroupCommand(NotificationIds.parseRequired(id, "id")));
    }

    public DistributionGroupResponse getDistributionGroupById(String id) {
        DistributionGroup group = getDistributionGroupByIdHandler.handle(
                new GetDistributionGroupByIdQuery(NotificationIds.parseRequired(id, "id")));
        return distributionGroupMapper.toDistributionGroupResponse(group);
    }

    public RecipientListResponse getDistributionGroupRecipients(String groupId) {
        List<Recipient> recipients = recipientService.getRecipientsByGroupId(
                NotificationIds.parseRequired(groupId, "id"));
        return recipientMapper.toRecipientListResponse(recipients);
    }

    public DistributionGroupListResponse getDistributionGroups(Integer page, Integer limit) {
        Page<DistributionGroup> groups = getDistributionGroupsHandler.handle(GetDistributionGroupsQuery.builder()
                                                                                     .page(page)
                                                                                     .limit(limit)
                                                                                     .build());
        return distributionGroupMapper.toDistributionGroupListResponse(groups);
    }

    public void removeRecipientFromDistributionGroup(String groupId, String recipientId) {
        removeRecipientFromGroupHandler.handle(new RemoveRecipientFromGroupCommand(
                NotificationIds.parseRequired(groupId, "id"),
                NotificationIds.parseRequired(recipientId, "recipientId")
        ));
    }

    public DistributionGroupResponse updateDistributionGroup(String id, UpdateDistributionGroupRequest request) {
        DistributionGroup group = updateDistributionGroupHandler.handle(UpdateDistributionGroupCommand.builder()
                                                                                .id(NotificationIds.parseRequired(id,
                                                                                                                  "id"))
                                                                                .name(request.getName())
                                                                                .description(request.getDescription())
                                                                                .build());
        return distributionGroupMapper.toDistributionGroupResponse(group);
    }
}
