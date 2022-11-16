package com.taxi.component;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.taxi.dto.VehicleDistanceInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * Logging consumer.
 * Writes to log information about vehicle total distance.
 */
@Slf4j
@Component
public class LoggingConsumer {

  private static final String LOGGING_KAFKA_LISTENER_CONTAINER_FACTORY = "loggingKafkaListenerContainerFactory";

  @KafkaListener(topics = "${spring.kafka.logging.topic.name}",
      groupId = "${spring.kafka.logging.group.id}",
      containerFactory = LOGGING_KAFKA_LISTENER_CONTAINER_FACTORY)
  public void consume(VehicleDistanceInfo vehicleDistanceInfo) {
    log.info("Logging consumer. Input data: {}, thread: {}", vehicleDistanceInfo, Thread.currentThread().getId());
  }
}
