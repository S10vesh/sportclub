# Sport Club Registration

Консольная информационная система спортивного клуба. Основная сущность: запись участника на тренировку.

## Технологии

- Java 17
- Maven
- PostgreSQL
- JDBC
- Apache POI для экспорта `.xlsx`

## Запуск через Docker

Запустить PostgreSQL и консольное приложение:

```bash
docker compose up --build
```

PostgreSQL будет доступен на `localhost:5432`. Скрипт `database/schema.sql`
выполнится автоматически при первом создании Docker volume.

Для полной повторной инициализации базы:

```bash
docker compose down -v
docker compose up --build
```

Для запуска только базы и приложения локально:

```bash
docker compose up -d postgres
mvn clean compile exec:java
```

## Быстрый запуск

1. Создайте базу данных PostgreSQL:

```sql
CREATE DATABASE sportclub;
```

2. Выполните `database/schema.sql` в базе `sportclub`.

3. Укажите параметры подключения через переменные окружения:

```text
SPORTCLUB_DB_URL=jdbc:postgresql://localhost:5432/sportclub
SPORTCLUB_DB_USER=postgres
SPORTCLUB_DB_PASSWORD=postgres
```

Значения по умолчанию уже рассчитаны на локальный PostgreSQL с пользователем `postgres` и паролем `postgres`.

4. Запустите приложение:

```bash
mvn clean compile exec:java
```

Если Windows сообщает `ModuleNotFoundError: No module named 'pwd'`, команда
`mvn` занята Python-пакетом, а не Apache Maven. В этом случае запустите:

```powershell
.\run.ps1
```

Или используйте настоящий Maven напрямую:

```powershell
& "$env:TEMP\apache-maven-3.9.11\bin\mvn.cmd" clean compile exec:java
```

Для постоянного исправления установите Apache Maven и добавьте его папку `bin`
в `PATH`. Проверка должна показывать путь к `mvn.cmd`, а не к
`Python311\Scripts\mvn.exe`.

Excel-файл сохраняется в `exports/sport-club-data.xlsx` и содержит листы участников и записей на тренировки.

## Архитектура

`Console UI -> Service -> Repository/JDBC -> PostgreSQL`

- `model` содержит предметные классы и перечисления.
- `repository` содержит интерфейс CRUD и JDBC-реализации.
- `service` содержит бизнес-правила, поиск, фильтрацию, сортировку и статистику.
- `util` содержит подключение к БД и экспорт в Excel.
- `exception` содержит собственные исключения предметной области.

## Бизнес-правила

1. Запись создается только для существующего активного участника.
2. Продолжительность тренировки находится в диапазоне от 30 до 240 минут.
3. Нельзя создать две записи одного участника на одну дату и время.
4. Отмененную или завершенную запись нельзя редактировать.
5. Завершить можно только подтвержденную запись.
6. Нельзя отменить уже завершенную запись.
7. Удаление записи запрещено для завершенной тренировки.
