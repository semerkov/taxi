package com.taxi.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.taxi.dto.VehiclePosition;
import com.taxi.dto.VehiclePositionRequest;
import com.taxi.component.VehiclePositionProducer;

import lombok.extern.slf4j.Slf4j;

/**
 * Vehicle position controller.
 */
@Slf4j
@RestController()
@RequestMapping(value = "/taxi/position/")
public class VehiclePositionController {

  @Autowired
  private VehiclePositionProducer vehiclePositionProducer;

  @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(value = HttpStatus.OK)
  public void addVehiclePosition(@Valid @RequestBody VehiclePositionRequest request) {
    log.info("Vehicle position request: {}.", request);

    VehiclePosition vehiclePosition = VehiclePosition.builder()
        .vehicleId(request.getVehicleId())
        .x(request.getX())
        .y(request.getY())
        .build();

    vehiclePositionProducer.publishCoordinates(vehiclePosition);
  }
}