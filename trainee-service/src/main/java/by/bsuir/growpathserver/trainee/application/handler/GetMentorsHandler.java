package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetMentorsQuery;
import by.bsuir.growpathserver.trainee.application.service.MentorService;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetMentorsHandler {

    private final MentorService mentorService;

    public Page<User> handle(GetMentorsQuery query) {
        return mentorService.getMentors(query);
    }
}
