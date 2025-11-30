# Secure File Exchange Service

## Prerequisites

*   Java 21
*   Docker & Docker Compose
*   Intellij Idea / gradle

## How to Run

### Using Docker Compose + Intellij

1. Поднять сервисы S3 и Postgres через docker-compose
2. Поднять приложение через `gradle bootRun` или запустить приложение через ide
3. Открыть в браузере `http://localhost:8080`.
4. Потыкать на разные кнопочки (в поле слева вводится api-key, что аналогично логину по логике работы программы)

## API Endpoints

All API endpoints require the `X-API-Key` header for authentication/authorization.

*   `POST /api/v1/files/upload` - Upload a file.
*   `GET /api/v1/files` - List files for the user.
*   `GET /api/v1/files/{file_id}` - Get file info.
*   `POST /api/v1/files/{file_id}/share` - Generate share link.
*   `DELETE /api/v1/files/{file_id}` - Delete a file.
*   `GET /api/v1/stats` - Get user statistics.
