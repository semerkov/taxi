package com.taxi.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Information about vehicle current state.
 */
@Getter
@Setter
@ToString
@Builder
public class VehicleInfo {

  /**
   * Vehicle identifier
   */
  private String vehicleId;

  /**
   * Abscissa
   */
  private long x;

  /**
   * Ordinate
   */
  private long y;

  /**
   * Total distance
   */
  private double distance;
}
