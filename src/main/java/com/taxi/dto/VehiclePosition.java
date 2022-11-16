package com.taxi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiclePosition {

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
}
