package com.taxi;

import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxi.component.LoggingConsumer;
import com.taxi.component.VehiclePositionConsumer;
import com.taxi.component.VehiclePositionProducer;
import com.taxi.dto.VehicleDistanceInfo;
import com.taxi.dto.VehiclePosition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = {TaxiApplication.class})
@EmbeddedKafka(topics = {"${spring.kafka.distanceCalculation.topic.name}", "${spring.kafka.logging.topic.name}"},
    partitions = 3,
    brokerProperties = {"listeners=PLAINTEXT://${spring.kafka.bootstrap-servers}", "port=${spring.kafka.port}"})
public class SecondTaskTest {

  private static final VehicleDistanceInfo VEHICLE_DISTANCE_INFO = new VehicleDistanceInfo("f1", 14.866068747318506);

  private static final String VEHICLE_POSITION_URL = "/taxi/position/add";

  private static final long TIMEOUT = 2000;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @SpyBean
  private VehiclePositionConsumer vehiclePositionConsumer;

  @SpyBean
  private LoggingConsumer loggingConsumer;

  @SpyBean
  private VehiclePositionProducer vehiclePositionProducer;

  @Captor
  private ArgumentCaptor<VehiclePosition> vehiclePositionArgumentCaptor;

  @Captor
  private ArgumentCaptor<VehicleDistanceInfo> vehicleDistanceArgumentCaptor;

  @Autowired
  private ObjectMapper objectMapper;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    this.mockMvc = webAppContextSetup(webApplicationContext).build();
  }

  @Test
  public void testSecondTask() throws Exception {
    VehiclePosition vehiclePosition = new VehiclePosition("f1", 10, 11);

    mockMvc.perform(post(VEHICLE_POSITION_URL)
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(vehiclePosition)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    verify(vehiclePositionProducer, timeout(TIMEOUT).times(1))
        .publishCoordinates(vehiclePositionArgumentCaptor.capture());

    VehiclePosition vehicleCapturedFromProducer = vehiclePositionArgumentCaptor.getValue();

    VehiclePosition capturedVehiclePosition = getVehiclePositionConsumer();

    verify(loggingConsumer, timeout(TIMEOUT).times(1))
        .consume(vehicleDistanceArgumentCaptor.capture());

    VehicleDistanceInfo vehicleDistanceInfo = vehicleDistanceArgumentCaptor.getValue();

    assertNotNull(vehicleCapturedFromProducer);
    assertEquals(vehiclePosition, vehicleCapturedFromProducer);
    assertNotNull(capturedVehiclePosition);
    assertEquals(vehiclePosition, capturedVehiclePosition);
    assertNotNull(vehicleDistanceInfo);
    assertEquals(VEHICLE_DISTANCE_INFO, vehicleDistanceInfo);
  }

  private VehiclePosition getVehiclePositionConsumer() {
    verify(vehiclePositionConsumer, timeout(TIMEOUT).atLeast(0))
        .consume(vehiclePositionArgumentCaptor.capture());

    VehiclePosition capturedVehiclePosition = vehiclePositionArgumentCaptor.getValue();
    if (Objects.nonNull(capturedVehiclePosition)) {
      return capturedVehiclePosition;
    }

    return null;
  }
}
