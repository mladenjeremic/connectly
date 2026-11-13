package com.connectly.app;

import com.connectly.application.MessageService;
import com.connectly.domain.event.StoryReplied;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Odgovor na priču se isporučuje kao poruka u razgovoru sa autorom priče (u istoj transakciji kao odgovor). */
@Component
class StoryReplyListener {
    private final MessageService messages;

    StoryReplyListener(MessageService messages) { this.messages = messages; }

    @EventListener
    void onStoryReplied(StoryReplied e) {
        messages.sendStoryReply(e.actorId(), e.storyAuthorId(), e.storyId(), e.content());
    }
}
