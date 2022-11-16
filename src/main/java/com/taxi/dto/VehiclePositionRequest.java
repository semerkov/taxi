package com.taxi.dto;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Vehicle position request.
 * Used in REST controller.
 */
@Getter
@Setter
@ToString
public class VehiclePositionRequest {

  /**
   * Vehicle identifier
   */
  @NotNull
  private String vehicleId;

  /**
   * Abscissa
   */
  @NotNull
  private Long x;

  /**
   * Ordinate
   */
  @NotNull
  private Long y;
}
