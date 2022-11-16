package com.taxi.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import com.taxi.dto.VehicleInfo;

/**
 * Vehicle information service with in-memory storage.
 */
@Service
public class VehicleInfoService {

  private final Map<String, VehicleInfo> vehicleInfoMap = new ConcurrentHashMap<>();

  public VehicleInfo getOrCreateNew(String vehicleId) {
    return vehicleInfoMap.computeIfAbsent(vehicleId, (key) -> VehicleInfo.builder().vehicleId(vehicleId).build());
  }

  /**
   * Calculates distance between two points: (x1, y1) and (x2, y2).
   *
   * @param x1 previous abscissa value
   * @param y1 previous ordinate value
   * @param x2 current abscissa value
   * @param y2 current ordinate value
   * @return distance between two points
   */
  public double calculateDistance(long x1, long y1, long x2, long y2) {
    return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
  }
}
