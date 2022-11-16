package com.taxi;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Implement a pair of "at least once" producer and "at most once" consumer.
 * <ul>
 *   <li>No web application required;</li>
 *   <li>write an integration test using the Kafka Containers library.</li>
 * </ul>
 *
 * @see <a href="https://www.testcontainers.org/modules/kafka/#adding-this-module-to-your-project-dependencies">Kafka Containers</a>
 */
public class FirstTaskTest {

  private static final String DOCKER_IMAGE_NAME = "confluentinc/cp-kafka:latest";

  private static final String TOPIC_NAME = "task-one-topic";

  private static final String GROUP_ID = "test-consumer";

  private static final String EARLIEST = "earliest";

  private static final String AUTO_COMMIT_INTERVAL_IN_MILLISECONDS = "15000";

  private static final String ACKNOWLEDGEMENT_VALUE = "1";

  private static final Duration CONSUMER_TIMEOUT = Duration.of(500, ChronoUnit.MILLIS);

  private static KafkaContainer kafkaContainer;

  private static KafkaProducer<String, String> producer;

  private static KafkaConsumer<String, String> consumer;

  @BeforeAll
  public static void setUp() {

    // Kafka container
    Network network = Network.newNetwork();
    DockerImageName dockerImageName = DockerImageName.parse(DOCKER_IMAGE_NAME);
    kafkaContainer = new KafkaContainer(dockerImageName)
        .withNetwork(network);

    kafkaContainer.start();

    // Producer
    producer = createProducer(kafkaContainer.getBootstrapServers());

    // Consumer
    consumer = createConsumer(kafkaContainer.getBootstrapServers());
  }

  @AfterAll
  public static void cleanUp() {
    if (kafkaContainer != null) {
      kafkaContainer.close();
    }
  }

  @Test
  public void test() {
    String key = UUID.randomUUID().toString();
    String value = "qwerty1";

    // Publish message
    ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC_NAME, key, value);
    producer.send(producerRecord);

    // Get message
    ConsumerRecords<String, String> recordsMap = consumer.poll(CONSUMER_TIMEOUT);
    List<ConsumerRecord<String, String>> records = StreamSupport.stream(recordsMap.spliterator(), false)
        .toList();

    assertEquals(records.size(), 1);

    ConsumerRecord<String, String> record = records.get(0);
    assertEquals(record.topic(), TOPIC_NAME);
    assertEquals(record.key(), key);
    assertEquals(record.value(), value);
  }

  /**
   * Creates producer by specified bootstrap servers value.
   *
   * @param bootstrapServers bootstrap servers value
   * @return producer
   */
  private static KafkaProducer<String, String> createProducer(String bootstrapServers) {
    Properties producerProperties = new Properties();
    producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

    /*
     * "At least once" producer.
     * acks = 1
     * After sending a record, producer waits for a confirmation that the leader responsible
     * for the target partition was able to store the data
     */
    producerProperties.put(ProducerConfig.ACKS_CONFIG, ACKNOWLEDGEMENT_VALUE);
    return new KafkaProducer<>(producerProperties, Serdes.String().serializer(), Serdes.String().serializer());
  }

  /**
   * Creates consumer by specified bootstrap servers value.
   *
   * @param bootstrapServers bootstrap servers value
   */
  private static KafkaConsumer<String, String> createConsumer(String bootstrapServers) {
    Properties consumerProperties = new Properties();
    consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
    consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);

    // "At most once" consumer
    consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, BooleanUtils.TRUE);
    consumerProperties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, AUTO_COMMIT_INTERVAL_IN_MILLISECONDS);

    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties, Serdes.String().deserializer(), Serdes.String().deserializer());
    consumer.subscribe(Collections.singleton(TOPIC_NAME));

    return consumer;
  }
}
