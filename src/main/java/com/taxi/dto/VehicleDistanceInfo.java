package com.taxi.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDistanceInfo {

  /**
   * Vehicle identifier
   */
  private String vehicleId;

  /**
   * Total distance passed by the vehicle
   */
  private double totalDistance;
}
