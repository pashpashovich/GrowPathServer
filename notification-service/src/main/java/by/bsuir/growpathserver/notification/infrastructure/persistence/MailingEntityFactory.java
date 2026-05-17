package by.bsuir.growpathserver.notification.infrastructure.persistence;

import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.domain.aggregate.Mailing;
import by.bsuir.growpathserver.notification.domain.entity.DistributionGroupEntity;
import by.bsuir.growpathserver.notification.domain.entity.EmailTemplateEntity;
import by.bsuir.growpathserver.notification.domain.entity.MailingEntity;
import by.bsuir.growpathserver.notification.domain.entity.MailingScheduleEntity;

@Component
public class MailingEntityFactory {

    public MailingEntity toNewEntity(Mailing mailing,
                                     EmailTemplateEntity template,
                                     List<DistributionGroupEntity> groups) {
        MailingEntity entity = new MailingEntity();
        entity.setName(mailing.getName());
        entity.setType(mailing.getType());
        entity.setStatus(mailing.getStatus());
        entity.setTemplate(template);
        entity.setExecuteAt(mailing.getExecuteAt());
        entity.setDistributionGroups(new LinkedHashSet<>(groups));
        mailing.getScheduleDefinitions().forEach(definition -> {
            MailingScheduleEntity schedule = new MailingScheduleEntity();
            schedule.setWeekDay(definition.weekDay());
            schedule.setExecuteTime(definition.executeTime());
            schedule.setMailing(entity);
            entity.getSchedules().add(schedule);
        });
        return entity;
    }
}
