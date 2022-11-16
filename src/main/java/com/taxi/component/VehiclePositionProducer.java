package com.taxi.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import com.taxi.dto.VehiclePosition;

import lombok.extern.slf4j.Slf4j;

/**
 * Producer that sends vehicle position for distance calculation.
 */
@Slf4j
@Component
public class VehiclePositionProducer {

  private final String distanceCalculationTopicName;

  private final KafkaTemplate stringKeyJsonValueKafkaTemplate;

  @Autowired
  public VehiclePositionProducer(@Value(value = "${spring.kafka.distanceCalculation.topic.name}") String distanceCalculationTopicName,
                                 KafkaTemplate stringKeyJsonValueKafkaTemplate) {
    this.distanceCalculationTopicName = distanceCalculationTopicName;
    this.stringKeyJsonValueKafkaTemplate = stringKeyJsonValueKafkaTemplate;
  }

  public void publishCoordinates(VehiclePosition vehiclePosition) {
    ListenableFuture<SendResult<String, VehiclePosition>> resultFuture = stringKeyJsonValueKafkaTemplate.send(distanceCalculationTopicName,
        vehiclePosition.getVehicleId(),
        vehiclePosition);

    // Log result
    resultFuture.addCallback(new ListenableFutureCallback<>() {

      @Override
      public void onSuccess(SendResult<String, VehiclePosition> result) {
        log.info("Vehicle position was published to vehicle distance calculation topic: {}.", vehiclePosition);
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Error during publishing vehicle position to vehicle distance calculation topic: {}. Exception: {}.", vehiclePosition, ex.getMessage());
      }
    });
  }
}
