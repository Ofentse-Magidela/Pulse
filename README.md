# Pulse Notification Service

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk\&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot\&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Message%20Broker-FF6600?logo=rabbitmq\&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql\&logoColor=white)
![Spring AMQP](https://img.shields.io/badge/Spring%20AMQP-Message%20Processing-6DB33F?logo=spring\&logoColor=white)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow)

A standalone notification service built with **Spring Boot** for handling asynchronous application notifications.

Pulse is designed to provide a centralized notification pipeline that can be consumed by multiple applications and services. The current implementation focuses on **email notifications**, with RabbitMQ providing asynchronous message processing, retry handling, and dead-lettering.

The authentication service currently acts as a **test/integration client** for Pulse. It provides a realistic application workflow for testing notification delivery while keeping Pulse independent from authentication-specific business logic.

---

## Features

### Current

* Notification API
* Email notifications
* SMTP email delivery
* Notification persistence
* Notification status tracking
* Asynchronous email processing
* RabbitMQ message publishing
* RabbitMQ consumer processing
* JSON message conversion
* Automatic retry handling
* Exponential backoff
* Dead Letter Exchange (DLX)
* Dead Letter Queue (DLQ)
* Basic idempotency handling
* Integration with an authentication service for testing

### Planned

* SMS notifications
* Push notifications
* Notification templates
* User notification preferences
* Scheduled notifications
* Transactional Outbox
* Notification replay/recovery
* Redis-based caching where appropriate
* Application metrics and observability
* Docker / Docker Compose
* CI/CD with GitHub Actions
* Cloud deployment
* Improved monitoring and alerting

---

## Architecture

Pulse is designed as an independent service rather than an authentication-specific component.

```text
┌──────────────────────┐
│   Application        │
│      Services        │
│                      │
│ Auth / Other Apps    │
└──────────┬───────────┘
           │
           │ HTTP
           ▼
┌──────────────────────────────┐
│            Pulse             │
│                              │
│     Notification API         │
│             │                │
│             ▼                │
│     Notification Service     │
│             │                │
│       ┌─────┴─────┐          │
│       ▼           ▼          │
│   PostgreSQL    RabbitMQ     │
│                     │        │
│                     ▼        │
│              Email Consume   │
│                     │        │
└─────────────────────┼────────┘
                      │
                      ▼
                 SMTP Provider
```

The calling application submits a notification request to Pulse over HTTP.

Pulse persists the notification and publishes an asynchronous notification job to RabbitMQ. A dedicated consumer processes the message and communicates with the external email provider.

This prevents the calling application from having to wait for the email provider to complete its work.

---

## Notification Lifecycle

```text
HTTP Request
     │
     ▼
Notification Created
     │
     ▼
PENDING
     │
     ▼
Published to RabbitMQ
     │
     ▼
Email Consumer
     │
     ├── Success ──────────────► SENT
     │
     └── Failure
             │
             ▼
        Retry + Backoff
             │
             ├── Success ──────► SENT
             │
             └── Exhausted
                    │
                    ▼
                   DLX
                    │
                    ▼
                   DLQ
                    │
                    ▼
                  FAILED
```

---

## RabbitMQ Topology

The current notification topology consists of:

```text
pulse.notifications
        │
        │ email
        ▼
pulse.email
        │
        │ retry exhaustion
        ▼
pulse.notifications.dlx
        │
        │ email
        ▼
pulse.email.dlq
```

The email queue is configured with a Dead Letter Exchange so messages that exhaust their retry attempts can be routed to the dedicated dead-letter queue.

Dead-lettered messages retain RabbitMQ metadata such as `x-death`, allowing the failure history to be inspected.

---

## Reliability

Pulse currently uses RabbitMQ retry handling with exponential backoff.

Example retry behaviour:

```text
Initial failure
      │
      ├── wait
      ▼
Retry #1
      │
      ├── wait
      ▼
Retry #2
      │
      ├── wait
      ▼
Retry #3
      │
      ▼
Retries exhausted
      │
      ▼
Dead Letter Exchange
      │
      ▼
Dead Letter Queue
```

This provides protection against transient failures such as temporary SMTP or network problems while preventing permanently failing messages from remaining indefinitely in the primary email queue.

---

## Idempotency

Because message delivery systems can redeliver messages, Pulse checks the persisted notification state before processing an email.

A notification that has already reached `SENT` should not be sent again if the same message is subsequently delivered.

This is important because successful external delivery and RabbitMQ acknowledgement are separate events. A message may potentially be delivered more than once even when the external email provider has already accepted it.

---

## Notification Status

| Status    | Description                                                                  |
| --------- | ---------------------------------------------------------------------------- |
| `PENDING` | Notification has been created and is awaiting processing                     |
| `SENT`    | Email processing completed successfully                                      |
| `FAILED`  | Notification permanently failed after exhausting the configured failure path |

---

## Integration Testing

The authentication service is currently used as a **test client** for Pulse.

For example, during user registration:

```text
User Registration
       │
       ▼
Auth Service
       │
       │ HTTP
       ▼
Pulse
       │
       ▼
RabbitMQ
       │
       ▼
Email Consumer
       │
       ▼
SMTP
       │
       ▼
Verification Email
```

This integration allows Pulse to be tested against a realistic application workflow without making Pulse dependent on authentication-specific logic.

As additional notification channels are implemented, other applications can consume Pulse through the same notification API.

---

## Planned Notification Channels

Pulse is intended to eventually support multiple notification channels:

```text
                 ┌── Email
                 │
                 ├── SMS
Application ──► Pulse ──┼── Push Notifications
                 │
                 └── Future Channels
```

The goal is to keep channel-specific delivery logic behind Pulse's notification infrastructure rather than duplicating notification functionality across individual applications.

---

## Tech Stack

### Current

* Java 21
* Spring Boot 3.x
* Spring Data JPA
* Spring AMQP
* RabbitMQ
* PostgreSQL
* Spring Mail / SMTP
* Jackson
* Maven

### Planned

* Redis
* Docker
* Docker Compose
* GitHub Actions
* AWS
* Micrometer / Prometheus
* Additional messaging and observability tooling where appropriate

> Planned technologies are not currently part of the production implementation and will be added incrementally as the service evolves.

---

## Project Structure

```text
src/main/java/com/ofentse/pulse/
├── notification/
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── email/
│   ├── entity/
│   ├── enums/
│   ├── producer/
│   ├── repository/
│   └── service/
│
└── PulseApplication.java
```

---

## Getting Started

### Prerequisites

* Java 21+
* Maven 3.8+
* PostgreSQL
* RabbitMQ

### Clone the repository

```bash
git clone https://github.com/Ofentse-Magidela/pulse.git
cd pulse
```

### Configure the application

Configure PostgreSQL, RabbitMQ, and SMTP credentials through `application.properties` or environment variables.

```properties
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/pulse
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=your_email
spring.mail.password=your_password
```

Sensitive credentials should never be committed to source control.

---

## Roadmap

Pulse is being developed incrementally toward a production-oriented notification platform.

* [x] Notification API
* [x] Email delivery
* [x] Notification persistence
* [x] Asynchronous RabbitMQ processing
* [x] Retry handling
* [x] Exponential backoff
* [x] Dead Letter Exchange
* [x] Dead Letter Queue
* [x] Basic idempotency
* [ ] Transactional Outbox
* [ ] Notification templates
* [ ] User preferences
* [ ] Scheduled notifications
* [ ] SMS
* [ ] Push notifications
* [ ] Redis
* [ ] Automated testing
* [ ] Docker / Docker Compose
* [ ] CI/CD
* [ ] Cloud deployment
* [ ] Metrics and observability
* [ ] Monitoring and alerting

---

## AI Assistance

AI tools were used during development for documentation, debugging assistance, architectural discussion, and implementation guidance.

The architecture, implementation decisions, integration, testing, and final code were developed, evaluated, and integrated by me.

---

## Author

**Ofentse Magidela**

GitHub: https://github.com/Ofentse-Magidela

---

## License

This project is currently intended for educational and portfolio purposes.
