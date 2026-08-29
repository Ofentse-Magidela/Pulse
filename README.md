# Pulse Notification Service

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk\&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?logo=springboot\&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Message%20Broker-FF6600?logo=rabbitmq\&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql\&logoColor=white)
![Spring AMQP](https://img.shields.io/badge/Spring%20AMQP-Message%20Processing-6DB33F?logo=spring\&logoColor=white)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow)

A standalone notification service built with **Spring Boot** for handling asynchronous application notifications.

Pulse provides a centralized notification pipeline that can be consumed by multiple applications and services. It currently supports **email and SMS notifications**, with RabbitMQ providing asynchronous message processing, retry handling, and dead-lettering.

The authentication service currently acts as a **test/integration client** for Pulse. It provides a realistic application workflow for testing notification delivery while keeping Pulse independent from authentication-specific business logic.

---

## Features

### Current

* Email notifications
* SMS notifications
* Asynchronous processing with RabbitMQ
* Retry handling with exponential backoff
* Dead-letter queues
* Notification persistence and status tracking
* Basic validation and idempotency
* Unit and integration testing

### Planned

* Push notifications
* Notification templates
* User notification preferences
* Scheduled notifications
* Transactional Outbox hardening
* Notification replay/recovery
* Redis-based caching and supporting infrastructure where appropriate
* Application metrics and observability
* Docker / Docker Compose
* CI/CD with GitHub Actions
* Cloud deployment
* Improved monitoring and alerting

---

## Architecture

Pulse is designed as an independent notification service rather than an authentication-specific component.

Calling applications submit notification requests to Pulse over HTTP. Pulse persists the notification and creates an outbox event for asynchronous processing. RabbitMQ then routes the event to the appropriate channel-specific queue.

Each notification channel has its own consumer and delivery implementation. Failed messages are retried using the configured retry strategy and eventually routed through the appropriate dead-letter path when retries are exhausted.

The authentication service currently acts as an integration client, but Pulse is not coupled to it and can be consumed by other applications.

---

## Notification Lifecycle

A notification follows this general lifecycle:

1. A calling application submits a notification request to Pulse.
2. Pulse validates the request and persists the notification with a `PENDING` status.
3. An outbox event is created for asynchronous processing.
4. The event is published to RabbitMQ.
5. RabbitMQ routes the message to the appropriate notification channel queue.
6. The channel consumer processes the message.
7. Successful processing results in the notification being marked `SENT`.
8. Failed processing is retried using exponential backoff.
9. Messages that exhaust their configured retry attempts are routed to the channel's dead-letter queue.
10. The dead-letter consumer marks the associated notification as `FAILED`.

---

## RabbitMQ

Pulse uses RabbitMQ as its asynchronous message broker.

The notification exchange routes messages using channel-specific routing keys. Email and SMS currently have separate queues and dead-letter queues.

The queues are configured with dead-letter exchanges so messages that exhaust their retry attempts can be isolated from the primary processing queues.

Dead-lettered messages retain RabbitMQ metadata such as `x-death`, allowing the failure history to be inspected.

---

## Reliability

Pulse currently uses RabbitMQ retry handling with exponential backoff.

Retries protect against transient failures such as temporary SMTP, SMS provider, or network failures while preventing permanently failing messages from remaining indefinitely in the primary queues.

Once the configured retry attempts are exhausted, the message follows the dead-letter path for that notification channel.

---

## Idempotency

Because message delivery systems can redeliver messages, Pulse checks the persisted notification state before processing a notification.

A notification that has already reached `SENT` should not be processed again if the same message is subsequently delivered.

This is important because successful external delivery and RabbitMQ acknowledgement are separate events. A message may potentially be delivered more than once even when the external provider has already accepted it.

---

## Notification Status

| Status    | Description                                                                  |
| --------- | ---------------------------------------------------------------------------- |
| `PENDING` | Notification has been created and is awaiting processing                     |
| `SENT`    | Notification processing completed successfully                               |
| `FAILED`  | Notification permanently failed after exhausting the configured failure path |

---

## Integration Testing

The authentication service is currently used as a **test client** for Pulse.

For example, during user registration:

User Registration → Auth Service → Pulse → RabbitMQ → Notification Consumer → Notification Provider

This integration allows Pulse to be tested against a realistic application workflow without making Pulse dependent on authentication-specific logic.

As additional notification channels are implemented, other applications can consume Pulse through the same notification API.

---

## Notification Channels

Pulse is designed around channel-specific notification processing.

Current channels:

* **Email** — SMTP-based delivery
* **SMS** — currently using a mock delivery implementation

The channel-specific delivery logic is isolated from the core notification infrastructure, allowing additional channels to be introduced without coupling them to existing notification implementations.

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
* Unit testing
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

Configure PostgreSQL, RabbitMQ, and external notification provider credentials through your local application configuration or environment variables.

Sensitive credentials should never be committed to source control.

---

## Roadmap

Pulse is being developed incrementally toward a production-oriented notification platform.

* [x] Notification API
* [x] Email delivery
* [x] SMS notification flow
* [x] Notification persistence
* [x] Asynchronous RabbitMQ processing
* [x] Retry handling
* [x] Exponential backoff
* [x] Dead Letter Exchange
* [x] Dead Letter Queue
* [x] Basic idempotency
* [x] Controller validation
* [x] Unit testing
* [ ] Transactional Outbox hardening
* [ ] Notification templates
* [ ] User preferences
* [ ] Scheduled notifications
* [ ] Push notifications
* [ ] Redis
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
