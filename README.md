# Pulse Notification Service

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk\&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?logo=springboot\&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Message%20Broker-FF6600?logo=rabbitmq\&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql\&logoColor=white)
![Spring AMQP](https://img.shields.io/badge/Spring%20AMQP-Message%20Processing-6DB33F?logo=spring\&logoColor=white)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow)

A standalone **Spring Boot notification service** for handling asynchronous application notifications.

Pulse provides a centralized notification pipeline that can be consumed by multiple applications and services. It currently supports **email and WhatsApp notifications**, with RabbitMQ handling asynchronous processing, retries, and dead-lettering.

---

## Features

* Email notifications via SMTP
* WhatsApp notifications via Meta WhatsApp Cloud API
* Transactional outbox
* Asynchronous RabbitMQ processing
* Retry handling with exponential backoff
* Dead-letter queues
* Notification persistence and status tracking
* Basic idempotency
* Request validation
* Application logging
* Unit and integration testing

---

## Architecture

```text
Calling Application --> Pulse REST API --> PostgreSQL + Outbox --> Outbox Publisher --> RabbitMQ
                                                                                         |
                                                                                         +--> Email Consumer --> SMTP
                                                                                         |
                                                                                         +--> WhatsApp Consumer --> Meta Cloud API
```

Pulse is designed to remain independent of the applications that consume it. The authentication service currently acts as an integration client for testing the notification workflow.

---

## Reliability

Pulse uses retry handling at both the outbox and RabbitMQ consumer levels.

**Outbox publishing**

Failed publishing attempts are persisted and retried using increasing retry intervals before the event is marked `FAILED`.

**RabbitMQ consumers**

Consumer failures use exponential backoff with a configured maximum number of attempts. Messages that exhaust their retries are routed to channel-specific dead-letter queues.

Notifications also use persisted status checks to prevent already-sent notifications from being processed again after message redelivery.

---

## WhatsApp Integration

Pulse integrates with the **Meta WhatsApp Cloud API** using Spring `RestClient`.

```text
Pulse --> WhatsAppApiClient --HTTPS--> Meta Graph API --> WhatsApp recipient
```

The provider integration uses a dedicated DTO for the Meta API payload, keeping the external provider's request structure separate from Pulse's internal notification model.

---

## Tech Stack

### Current

* Java 21
* Spring Boot 4.x
* Spring Data JPA
* Spring AMQP
* RabbitMQ
* PostgreSQL
* Spring Mail / SMTP
* Meta WhatsApp Cloud API
* Spring RestClient
* SLF4J / Logback
* JUnit
* Mockito
* Maven

### Planned

* Redis
* Rate limiting
* Docker / Docker Compose
* GitHub Actions
* AWS
* Micrometer / Prometheus
* Monitoring and alerting
* Notification templates
* Scheduled notifications
* Push notifications

---

## Getting Started

### Prerequisites

* Java 21+
* Maven 3.8+
* PostgreSQL
* RabbitMQ
* Meta WhatsApp Cloud API credentials for WhatsApp testing

### Clone

```bash
git clone https://github.com/Ofentse-Magidela/pulse.git
cd pulse
```

### Configuration

Configure PostgreSQL, RabbitMQ, SMTP, and WhatsApp credentials through your local configuration or environment variables.

Required WhatsApp configuration includes:

* Meta Graph API version
* WhatsApp Phone Number ID
* WhatsApp access token

**Never commit credentials or other sensitive configuration to source control.**

---

## Roadmap

* [x] Notification API
* [x] Email delivery
* [x] WhatsApp delivery
* [x] Meta WhatsApp Cloud API integration
* [x] Transactional outbox
* [x] RabbitMQ asynchronous processing
* [x] Retry handling
* [x] Exponential backoff
* [x] Dead Letter Exchange / Queue
* [x] Basic idempotency
* [x] Request validation
* [ ] Redis
* [ ] Rate limiting
* [ ] Notification templates
* [ ] Scheduled notifications
* [ ] Docker / Docker Compose
* [ ] CI/CD
* [ ] AWS deployment
* [ ] Metrics and observability

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
