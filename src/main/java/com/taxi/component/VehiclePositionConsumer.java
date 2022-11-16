package com.taxi.component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.taxi.dto.VehicleDistanceInfo;
import com.taxi.dto.VehicleInfo;
import com.taxi.dto.VehiclePosition;
import com.taxi.service.VehicleInfoService;

import lombok.extern.slf4j.Slf4j;

/**
 * Vehicle position consumer.
 * Gets vehicle positions from REST controller.
 */
@Slf4j
@Component
public class VehiclePositionConsumer {

  private static final String VEHICLE_POSITION_KAFKA_LISTENER_CONTAINER_FACTORY = "vehiclePositionKafkaListenerContainerFactory";

  private final String loggingTopicName;

  private final KafkaTemplate stringKeyJsonValueKafkaTemplate;

  private final VehicleInfoService vehicleInfoService;

  private final Lock lock = new ReentrantLock();

  @Autowired
  public VehiclePositionConsumer(@Value(value = "${spring.kafka.logging.topic.name}") String loggingTopicName,
                                 KafkaTemplate stringKeyJsonValueKafkaTemplate,
                                 VehicleInfoService vehicleInfoService) {
    this.loggingTopicName = loggingTopicName;
    this.stringKeyJsonValueKafkaTemplate = stringKeyJsonValueKafkaTemplate;
    this.vehicleInfoService = vehicleInfoService;
  }

  @KafkaListener(topics = "${spring.kafka.distanceCalculation.topic.name}",
      groupId = "${spring.kafka.distanceCalculation.group.id}",
      containerFactory = VEHICLE_POSITION_KAFKA_LISTENER_CONTAINER_FACTORY,
      concurrency = "${spring.kafka.distanceCalculation.listener.concurrency}")
  public void consume(VehiclePosition vehiclePosition) {
    log.info("Vehicle position consumer. Input data: {}, thread: {}", vehiclePosition, Thread.currentThread().getId());

    String vehicleId = vehiclePosition.getVehicleId();
    double totalDistance;

    lock.lock();
    try {
      VehicleInfo vehicleInfo = vehicleInfoService.getOrCreateNew(vehicleId);
      double distance = vehicleInfoService.calculateDistance(vehicleInfo.getX(),
          vehicleInfo.getY(),
          vehiclePosition.getX(),
          vehiclePosition.getY());

      totalDistance = vehicleInfo.getDistance() + distance;
      log.info("Previous vehicle state: {}. New position: {}. New total distance: {}",
          vehicleInfo,
          vehiclePosition,
          totalDistance);

      vehicleInfo.setX(vehiclePosition.getX());
      vehicleInfo.setY(vehiclePosition.getY());
      vehicleInfo.setDistance(totalDistance);
    } finally {
      lock.unlock();
    }

    publishDistance(vehicleId, totalDistance);
  }

  /**
   * Publishes vehicle total distance to the logging consumer.
   *
   * @param vehicleId     vehicle identifier
   * @param totalDistance vehicle total distance
   */
  private void publishDistance(String vehicleId, double totalDistance) {
    VehicleDistanceInfo vehicleDistanceInfo = new VehicleDistanceInfo(vehicleId, totalDistance);
    ListenableFuture<SendResult<String, VehicleDistanceInfo>> resultFuture = stringKeyJsonValueKafkaTemplate.send(loggingTopicName,
        vehicleId,
        vehicleDistanceInfo);

    // Log result
    resultFuture.addCallback(new ListenableFutureCallback<>() {

      @Override
      public void onSuccess(SendResult<String, VehicleDistanceInfo> result) {
        log.info("Vehicle distance info was published to logging topic: {}.", vehicleDistanceInfo);
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Error during publishing vehicle distance info to vehicle logging topic: {}. Exception: {}.", vehicleDistanceInfo, ex.getMessage());
      }
    });
  }
}
