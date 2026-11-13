package com.connectly.infrastructure.event;

import com.connectly.domain.port.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class SpringEventPublisher implements EventPublisher {
    private final ApplicationEventPublisher delegate;
    SpringEventPublisher(ApplicationEventPublisher delegate) { this.delegate = delegate; }
    public void publish(Object event) { delegate.publishEvent(event); }
}
