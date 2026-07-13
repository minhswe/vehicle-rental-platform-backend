package com.rentalplatform.backend.common.event;

public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
