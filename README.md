# TaskFlow & NotificationService — продоподобный backend (Spring Boot + Postgres + Mongo + Redis + Kafka + RabbitMQ + Elasticsearch)

- **TaskFlow** (`server.port=8080`)
  - Postgres: `localhost:5432` БД `taskflow_database`, `user/password`
  - MongoDB: `localhost:27017` (`root/password`, `auth-db=admin`, DB `taskflow`)
  - Redis: `localhost:6379`
  - Kafka: `localhost:9092`
  - RabbitMQ: `localhost:5672` (`user/password`), UI `http://localhost:15672`
  - Elasticsearch: `http://localhost:9200` (**security off**)
- **NotificationService** (`server.port=8081`)
  - Postgres: `localhost:5433` БД `notification_database`, `user/password`
  - RabbitMQ: `localhost:5672` (`user/password`)
  - Redis: `localhost:6379`

---

## 🧭 Оглавление
- [Что это](#что-это)
- [Локальный запуск (быстро, через Docker Compose, монорепо)](#локальный-запуск-быстро-через-docker-compose-монорепо)
- [Альтернатива: запуск инфраструктуры docker run](#альтернатива-запуск-инфраструктуры-docker-run)
- [Запуск приложений](#запуск-приложений)
- [Проверка «сквозняка»](#проверка-сквозняка)
- [API](#api)
- [Конфигурация (как в приложениях)](#конфигурация-как-в-приложениях)
- [Наблюдаемость](#наблюдаемость)
- [Тесты](#тесты)
- [Траблшутинг](#траблшутинг)
- [Лицензия](#лицензия)

---

## Что это
**TaskFlow** — сервис задач (CRUD + поиск + события).  
**NotificationService** — потребитель событий (уведомления/история).

Стек: Spring Boot 3.x, Postgres, MongoDB, Redis, Kafka, RabbitMQ, Elasticsearch.

---

## Локальный запуск (быстро, через Docker Compose, монорепо)
> Режим «клонировал → запустил». Поднимает все зависимости одной командой. Совместимо с текущими портами.

1) Возьми файлы из репозитория (лежать рядом с README):
   - `docker-compose.yml` (если отсутствует — переименуй прилагаемый `docker-compose.monorepo.yml`)
   - `.env` (если отсутствует — скопируй из `.env.example`)
2) Подними инфраструктуру:
```bash
docker compose up -d
```
3) Запусти приложения (в отдельных терминалах):
```bash
./mvnw -q -DskipTests spring-boot:run -pl taskflow
./mvnw -q -DskipTests spring-boot:run -pl notification-service
```
4) Проверка health:
- TaskFlow: http://localhost:8080/actuator/health  
- NotificationService: http://localhost:8081/actuator/health

> Остановка: `docker compose down`

---

## Альтернатива: запуск инфраструктуры docker run
Если удобнее без compose — можно так (порты совпадают с приложениями):
```bash
# Postgres для TaskFlow (5432)
docker run -d --name pg-taskflow -p 5432:5432 \
  -e POSTGRES_DB=taskflow_database -e POSTGRES_USER=user -e POSTGRES_PASSWORD=password \
  postgres:15

# Postgres для NotificationService (5433)
docker run -d --name pg-notif -p 5433:5432 \
  -e POSTGRES_DB=notification_database -e POSTGRES_USER=user -e POSTGRES_PASSWORD=password \
  postgres:15

# MongoDB (root/password, auth-db=admin)
docker run -d --name mongo -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=password \
  mongo:6

# Redis
docker run -d --name redis -p 6379:6379 redis:7

# RabbitMQ (user/password)
docker run -d --name rabbit -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=user -e RABBITMQ_DEFAULT_PASS=password \
  rabbitmq:3-management

# Kafka (PLAINTEXT://localhost:9092) + Zookeeper
docker run -d --name zk -p 2181:2181 -e ZOOKEEPER_CLIENT_PORT=2181 confluentinc/cp-zookeeper:7.5.1
docker run -d --name kafka -p 9092:9092 --link zk \
  -e KAFKA_ZOOKEEPER_CONNECT=zk:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:7.5.1

# Elasticsearch 8.x без security (9200)
docker run -d --name es -p 9200:9200 \
  -e "discovery.type=single-node" -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:8.13.4
```

---

## Запуск приложений


---

## Проверка «сквозняка»
```bash
# 1) Создать задачу
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Demo","description":"Hello","priority":"HIGH","dueDate":"2025-12-31"}'

# 2) Получить по id
curl http://localhost:8080/api/tasks/{id}

# 3) Обновить
curl -X PUT http://localhost:8080/api/tasks/{id} \
  -H "Content-Type: application/json" \
  -d '{"title":"Demo v2"}'

# 4) Поиск (через сервис)
curl "http://localhost:8080/api/tasks/search?q=Demo"

# 4b) Прямо в ES
curl "http://localhost:9200/task-index/_search?q=Demo"

# 5) Нотификации (если есть REST-эндпоинт)
curl http://localhost:8081/api/notifications
```

---

## API
- Swagger UI TaskFlow: <http://localhost:8080/swagger-ui/index.html>  
- OpenAPI JSON TaskFlow: <http://localhost:8080/v3/api-docs>

---

## Конфигурация (как в приложениях)

**TaskFlow:**
- `server.port=8080`
- PostgreSQL: `jdbc:postgresql://localhost:5432/taskflow_database` (`user/password`)
- MongoDB: `localhost:27017`, `username=root`, `password=password`, `authentication-database=admin`, DB `taskflow`
- Redis: `localhost:6379`
- Kafka: `bootstrap-servers=localhost:9092`
- RabbitMQ: `host=localhost`, `port=5672`, `username=user`, `password=password`
- Elasticsearch: `http://localhost:9200` (без логина/пароля)

**NotificationService:**
- `server.port=8081`
- PostgreSQL: `jdbc:postgresql://localhost:5433/notification_database` (`user/password`)
- RabbitMQ: `host=localhost`, `port=5672`, `username=user`, `password=password`
- Redis: `localhost:6379`

---

## Наблюдаемость
- Actuator: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`
- Micrometer → Prometheus (если включен)
- OpenTelemetry (если настроен экспортер)

---

## Тесты
```bash
./mvnw test
```
Рекомендуются Testcontainers для Postgres/Mongo/Redis/Kafka/Rabbit/ES.

---

## Траблшутинг
- `password authentication failed` (Postgres) → проверь порт и логин/пароль (`user/password`), контейнеры `pg-taskflow` и/или `pg-notif`.
- `Connection is closed` (ES) → запусти ES **без security**, как в инструкциях выше.
- `Connection refused` (Kafka/Rabbit/Redis/Mongo) → контейнер не запущен или порт занят.
- `Address already in use` → 8080/8081/5432/5433 заняты другим процессом.

---

