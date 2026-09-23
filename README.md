# Sport Club Registration

Консольная информационная система спортивного клуба. Основная сущность: запись участника на тренировку.

## Технологии

- Java 17
- Maven
- PostgreSQL
- JDBC
- Apache POI для экспорта `.xlsx`

## Запуск через Docker

Для Docker Desktop на Windows должны быть включены WSL2 и Virtual Machine
Platform. Если Docker Engine отвечает ошибкой HTTP 500, выполните:

```powershell
wsl --install
```

После установки обязательно перезагрузите компьютер, затем запустите Docker
Desktop и дождитесь статуса `Docker Desktop is running`.

Запустить PostgreSQL и консольное приложение:

```bash
docker compose up --build
```

PostgreSQL будет доступен с компьютера на `localhost:5433`. Внутри Docker-сети
приложение подключается к PostgreSQL по адресу `postgres:5432`. Скрипт
`database/schema.sql` выполнится автоматически при первом создании Docker volume.

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

Если Docker еще не установлен, PostgreSQL можно использовать как Windows-службу.
В текущем окружении PostgreSQL уже запущен и слушает `localhost:5432`. Создать
базу и загрузить таблицы/тестовые данные можно так:

```powershell
.\database\setup-local.ps1
mvn clean compile exec:java
```

Скрипт может запросить пароль пользователя `postgres`. Само приложение не
требует открытия pgAdmin: оно подключается к работающей службе PostgreSQL по
JDBC.

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

Если PostgreSQL сообщает `password authentication failed for user "postgres"`,
укажите фактический пароль в текущем PowerShell перед запуском приложения:

```powershell
$env:SPORTCLUB_DB_PASSWORD = "ВАШ_ПАРОЛЬ_POSTGRES"
mvn clean compile exec:java
```

Переменная действует только в текущем окне PowerShell. Пароль не нужно записывать
в исходный код или коммитить в проект.

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
