# Домашнее задание №6 по Java

**Группа:** BHEMBD-25  
**Студент:** Арсений Паниклов

Тема: «Работа с базой данных. Часть 1».

Spring Boot REST API хранит пользователей и сообщения в PostgreSQL через `JdbcTemplate`.
Для интеграционных тестов используется H2 в режиме совместимости с PostgreSQL.

## Реализовано

- внешняя PostgreSQL и готовый `docker-compose.yml`;
- JDBC-репозитории для пользователей и сообщений;
- связь `users` → `messages` через внешний ключ;
- `404 Not Found` для отсутствующих сообщений и пользователей;
- пагинация `page`/`size`;
- пакетная вставка до 1000 сообщений в транзакции;
- поиск сообщений по username или отображаемому имени автора;
- валидация запросов и единые JSON-ответы об ошибках;
- интеграционные тесты REST API и репозиториев.

## API

- `POST /api/users` — создать пользователя;
- `GET /api/users` — список пользователей;
- `POST /api/messages` — создать сообщение;
- `POST /api/messages/batch` — пакетное создание;
- `GET /api/messages?page=0&size=30` — страница сообщений;
- `GET /api/messages/{id}` — сообщение по ID;
- `GET /api/messages/search?author=...&page=0&size=30` — поиск по автору.

## Сборка и тесты

```powershell
.\gradlew.bat clean test
```

## Запуск с PostgreSQL

```powershell
docker compose up -d
.\gradlew.bat bootRun
```

Настройки подключения можно переопределить переменными `DB_URL`, `DB_USER`, `DB_PASSWORD`.
