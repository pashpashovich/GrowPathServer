package by.bsuir.growpathserver.notification.application.service;

import org.springframework.data.domain.Page;

import by.bsuir.growpathserver.notification.application.command.AddRecipientToGroupCommand;
import by.bsuir.growpathserver.notification.application.command.CreateDistributionGroupCommand;
import by.bsuir.growpathserver.notification.application.command.DeleteDistributionGroupCommand;
import by.bsuir.growpathserver.notification.application.command.RemoveRecipientFromGroupCommand;
import by.bsuir.growpathserver.notification.application.command.UpdateDistributionGroupCommand;
import by.bsuir.growpathserver.notification.application.query.GetDistributionGroupByIdQuery;
import by.bsuir.growpathserver.notification.application.query.GetDistributionGroupsQuery;
import by.bsuir.growpathserver.notification.domain.aggregate.DistributionGroup;

public interface DistributionGroupService {
    DistributionGroup createDistributionGroup(CreateDistributionGroupCommand command);

    DistributionGroup updateDistributionGroup(UpdateDistributionGroupCommand command);

    void deleteDistributionGroup(DeleteDistributionGroupCommand command);

    DistributionGroup getDistributionGroupById(GetDistributionGroupByIdQuery query);

    Page<DistributionGroup> getDistributionGroups(GetDistributionGroupsQuery query);

    void addRecipientToGroup(AddRecipientToGroupCommand command);

    void removeRecipientFromGroup(RemoveRecipientFromGroupCommand command);
}
