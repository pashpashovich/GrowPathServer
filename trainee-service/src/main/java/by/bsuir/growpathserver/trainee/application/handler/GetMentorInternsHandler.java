package by.bsuir.growpathserver.trainee.application.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetMentorInternsQuery;
import by.bsuir.growpathserver.trainee.application.service.MentorService;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetMentorInternsHandler {

    private final MentorService mentorService;

    public List<User> handle(GetMentorInternsQuery query) {
        return mentorService.getMentorInterns(query);
    }
}
