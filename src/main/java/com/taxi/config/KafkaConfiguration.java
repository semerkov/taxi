package com.taxi.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import com.taxi.dto.VehicleDistanceInfo;
import com.taxi.dto.VehiclePosition;

@Configuration
public class KafkaConfiguration {

  @Value(value = "${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value(value = "spring.kafka.distanceCalculation.group.id")
  private String distanceCalculationGroupId;

  @Value(value = "spring.kafka.logging.group.id")
  private String loggingGroupId;

  @Value(value = "${spring.kafka.distanceCalculation.topic.name}")
  private String distanceCalculationTopicName;

  @Value(value = "${spring.kafka.logging.topic.name}")
  private String loggingTopicName;

  @Value(value = "${spring.kafka.numberOfPartitions}")
  private int numberOfPartitions;

  @Value(value = "${spring.kafka.replicationFactor}")
  private int replicationFactor;

  @Bean
  public KafkaTemplate<String, Object> stringKeyJsonValueKafkaTemplate() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(configs);
    return new KafkaTemplate<>(producerFactory);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, VehiclePosition> vehiclePositionKafkaListenerContainerFactory() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configs.put(ConsumerConfig.GROUP_ID_CONFIG, distanceCalculationGroupId);
    ConsumerFactory<String, VehiclePosition> consumerFactory = new DefaultKafkaConsumerFactory<>(configs,
        new StringDeserializer(),
        new JsonDeserializer<>(VehiclePosition.class));

    ConcurrentKafkaListenerContainerFactory<String, VehiclePosition> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, VehicleDistanceInfo> loggingKafkaListenerContainerFactory() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configs.put(ConsumerConfig.GROUP_ID_CONFIG, loggingGroupId);

    ConsumerFactory<String, VehicleDistanceInfo> consumerFactory = new DefaultKafkaConsumerFactory<>(configs,
        new StringDeserializer(),
        new JsonDeserializer<>(VehicleDistanceInfo.class));

    ConcurrentKafkaListenerContainerFactory<String, VehicleDistanceInfo> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }

  @Bean
  public NewTopic distanceCalculationTopic() {
    return TopicBuilder.name(distanceCalculationTopicName)
        .partitions(numberOfPartitions)
        .replicas(replicationFactor)
        .build();
  }

  @Bean
  public NewTopic loggingTopic() {
    return TopicBuilder.name(loggingTopicName)
        .partitions(numberOfPartitions)
        .replicas(replicationFactor)
        .build();
  }
}
