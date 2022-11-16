# Taxi Spring Boot application with using Apache Kafka

### Docker

Build kafka and zookeeper images, containers and run them

docker-compose up -d

### REST endpoint for change vehicle position

http://localhost:8085/taxi/position/add

Request example:

```json
{
  "vehicleId": "v1",
  "x": 187,
  "y": 18
}
```
