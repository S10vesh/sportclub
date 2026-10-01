# Разбор кода Sport Club для защиты

Этот файл помогает отвечать на вопрос «что делает эта строка?» и тренироваться
писать фрагменты проекта от руки. Разбор относится к текущей версии исходников.
Если код изменится, примеры в этом файле нужно сверить с ним.

## Как читать разбор

- Объяснение идет в том же порядке, что и исходный код.
- Для коротких геттеров и однотипных строк объясняется общий шаблон и каждая
  конкретная строка/роль метода.
- Пустые строки только отделяют блоки.
- `{` начинает класс, метод, цикл, условие или блок; `}` закрывает ближайший
  незакрытый блок. Скобки не выполняют бизнес-логику, а задают структуру Java.
- `;` завершает инструкцию.
- В конце есть упражнения для ручного написания и ответы для самопроверки.

## Проект целиком за 30 секунд

Пользователь вводит команду в `ConsoleApplication`. Интерфейс вызывает метод
сервиса. Сервис проверяет правила и обращается к репозиторию. Репозиторий
исполняет SQL через JDBC и превращает строки PostgreSQL в объекты Java. Результат
возвращается в обратном направлении и выводится на экран.

```text
Main
  -> создает DatabaseManager, repositories, services и ConsoleApplication
ConsoleApplication
  -> принимает команду и вызывает Service
Service
  -> проверяет бизнес-правила и вызывает Repository
Repository
  -> JDBC: Connection + PreparedStatement + ResultSet
PostgreSQL
  -> хранит members и training_registrations
```

Важно: `Main` только соединяет компоненты. Меню не содержит SQL, а Repository не
решает, можно ли завершить запись: это обязанности разных слоев.

# 1. Main.java

Файл: `src/main/java/ru/sportclub/Main.java`

```java
package ru.sportclub;
```
Пакет класса. Путь к файлу соответствует имени пакета: `ru/sportclub`.

```java
import ru.sportclub.repository.MemberRepository;
import ru.sportclub.repository.TrainingRegistrationRepository;
import ru.sportclub.service.MemberService;
import ru.sportclub.service.TrainingRegistrationService;
import ru.sportclub.ui.ConsoleApplication;
import ru.sportclub.util.DatabaseManager;
```
`import` позволяет указывать короткие имена типов вместо полных имен с пакетом.
Импортированы два репозитория, два сервиса, консольный интерфейс и менеджер БД.

```java
public class Main {
```
Объявляет общедоступный класс `Main`. Имя совпадает с именем файла.

```java
public static void main(String[] args) {
```
Точка входа Java-программы. `public` делает ее доступной JVM; `static` позволяет
вызвать метод без создания объекта Main; `void` означает, что результат не
возвращается; `String[] args` содержит аргументы командной строки.

```java
DatabaseManager database = new DatabaseManager();
```
Создается объект, знающий адрес, пользователя и пароль БД. Настройки читаются из
переменных окружения либо берутся значения по умолчанию.

```java
MemberRepository memberRepository = new MemberRepository(database);
```
Создается JDBC-репозиторий участников. Ему передается общий менеджер соединений.
Это внедрение зависимости через конструктор.

```java
TrainingRegistrationRepository registrationRepository =
        new TrainingRegistrationRepository(database);
```
Создается JDBC-репозиторий записей на тренировки с тем же менеджером БД.

```java
MemberService memberService = new MemberService(memberRepository);
```
Сервис участников получает репозиторий и будет использовать его для хранения и
чтения данных, предварительно проверяя бизнес-правила участника.

```java
TrainingRegistrationService registrationService =
        new TrainingRegistrationService(registrationRepository, memberRepository);
```
Сервис записей получает свой репозиторий и репозиторий участников. Второй нужен,
чтобы перед записью проверить, существует ли участник и активен ли он.

```java
new ConsoleApplication(memberService, registrationService).run();
```
Создается консольный интерфейс, ему передаются сервисы, затем сразу вызывается
`run()`, который запускает цикл меню. Объект Main больше ничего не делает.

# 2. Модели

## Member.java

Файл: `src/main/java/ru/sportclub/model/Member.java`

```java
package ru.sportclub.model;
```
Объявляет пакет моделей предметной области.

```java
public class Member {
```
Класс участника. Один объект соответствует одному участнику системы.

```java
public enum MembershipType { BASIC, PREMIUM, ANNUAL }
```
Перечисление допустимых типов абонемента. `enum` не позволяет передать произвольную
строку вроде `VIP??`: значение должно быть одним из трех вариантов.

```java
private long id;
private String fullName;
private String phone;
private String email;
private MembershipType membershipType;
private boolean active;
```
Поля состояния участника. `private` реализует инкапсуляцию: другой класс не может
напрямую менять их. `long` используется для ID, `String` для текста, `boolean` для
двух состояний, `MembershipType` для ограниченного набора типов абонемента.

```java
public Member(long id, String fullName, String phone, String email,
              MembershipType membershipType, boolean active) {
```
Полный конструктор. Вызывается, когда известны все поля, например при чтении
готовой строки из БД.

```java
this.id = id;
this.fullName = fullName;
this.phone = phone;
this.email = email;
this.membershipType = membershipType;
this.active = active;
```
Каждое значение параметра сохраняется в поле текущего объекта. `this.id` означает
поле объекта, а `id` справа - параметр конструктора.

```java
public Member(String fullName, String phone, String email,
              MembershipType membershipType) {
    this(0, fullName, phone, email, membershipType, true);
}
```
Упрощенный конструктор для нового участника. `this(...)` вызывает другой
конструктор этого же класса. ID равен 0, потому что его назначит PostgreSQL;
новый участник считается активным.

```java
public long getId() { return id; }
public String getFullName() { return fullName; }
public String getPhone() { return phone; }
public String getEmail() { return email; }
public MembershipType getMembershipType() { return membershipType; }
public boolean isActive() { return active; }
```
Геттеры возвращают значения закрытых полей. Для boolean принято имя `isActive()`.

```java
public void setId(long id) { this.id = id; }
public void setActive(boolean active) { this.active = active; }
```
Сеттеры меняют только те поля, которые приложение должно обновлять после создания:
БД возвращает ID, а пользователь может переключить активность.

```java
@Override
public String toString() {
```
`@Override` говорит компилятору, что метод переопределяет `toString()` из Object.
Это строковое представление используется при выводе участника в консоль.

```java
return "%d | %s | %s | %s | %s | %s".formatted(
        id, fullName, phone, email, membershipType,
        active ? "активен" : "неактивен");
```
`%d` заменяется числом, `%s` - строками. `formatted(...)` подставляет значения.
Тернарный оператор `условие ? значение1 : значение2` выбирает подпись активности.

## TrainingRegistration.java

```java
package ru.sportclub.model;
```
Пакет модели записи тренировки.

```java
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
```
Подключает типы Java для даты, времени и даты-времени. Они безопаснее, чем хранить
дату и время обычным текстом.

```java
public class TrainingRegistration {
```
Класс одной записи участника на тренировку.

```java
public enum RegistrationStatus {
    PLANNED, CONFIRMED, COMPLETED, CANCELLED
}
```
Допустимые состояния записи: запланирована, подтверждена, завершена или отменена.
Перечисление предотвращает случайные некорректные значения статуса.

Поля:

```java
private long id;
private long memberId;
private String memberName;
private String trainingName;
private String trainerName;
private LocalDate trainingDate;
private LocalTime startTime;
private int durationMinutes;
private RegistrationStatus status;
private String category;
private LocalDateTime createdAt;
```
Это данные записи. `memberId` хранит внешний ключ участника; `memberName` нужен для
удобного отображения результата JOIN. Остальные поля описывают тренировку и ее
статус. Все поля `private`.

Полный конструктор принимает все поля и сохраняет каждый параметр в одноименное
поле через `this.field = field`. Он используется Repository при преобразовании
строки БД в Java-объект.

Короткий конструктор принимает поля новой записи и вызывает полный через `this(...)`.
Он передает `0` как временный ID, пустое имя участника (имя подставит JOIN при
чтении) и `null` для `createdAt` (значение создает БД).

Геттеры возвращают все поля. Сеттеры предусмотрены для ID, имени участника и статуса:
ID назначает БД, имя приходит из JOIN, статус может быть изменен.

`toString()` формирует одну удобную строку с ID, тренировкой, участником, тренером,
датой, временем, длительностью, статусом и категорией. `%d` - целое число, `%s` -
строковое значение.

# 3. Интерфейс CRUD и сервис участников

## CrudRepository.java

```java
package ru.sportclub.repository;
import java.util.List;
import java.util.Optional;
```
Объявляет пакет и подключает список и `Optional`. `Optional` явно показывает, что
поиск по ID может не найти объект.

```java
public interface CrudRepository<T> {
```
Обобщенный интерфейс. `T` - тип сущности, например Member или TrainingRegistration.
Интерфейс описывает контракт, но не содержит SQL.

```java
T save(T entity);
List<T> findAll();
Optional<T> findById(long id);
T update(T entity);
void deleteById(long id);
```
Контракт операций CRUD: создать, прочитать все, прочитать по ID, изменить и удалить.
`void` у удаления означает, что метод ничего не возвращает.

## MemberService.java

`import` подключает собственные исключения, модель Member, интерфейс CRUD и List.
`public class MemberService` объявляет слой бизнес-логики участников.

```java
private final CrudRepository<Member> repository;
```
Сервис зависит от интерфейса, а не от конкретного класса. `final` запрещает заменить
ссылку после конструктора. Это демонстрирует полиморфизм и уменьшает связанность.

Конструктор сохраняет переданный репозиторий в поле `this.repository`.

```java
public Member create(Member member) {
    validate(member);
    return repository.save(member);
}
```
Сначала проверяет участника, затем сохраняет. Неверные данные не доходят до БД.

```java
public List<Member> findAll() { return repository.findAll(); }
```
Передает репозиторию запрос списка и возвращает результат вызывающему коду.

```java
public Member findById(long id) {
    return repository.findById(id).orElseThrow(
        () -> new EntityNotFoundException("Участник не найден: " + id));
}
```
Репозиторий вернет `Optional`. Если объект есть, он возвращается; иначе `orElseThrow`
создает понятное исключение о несуществующем ID. `() -> ...` - лямбда, отложенно
создающая исключение только при отсутствии значения.

```java
public Member update(Member member) {
    findById(member.getId());
    validate(member);
    return repository.update(member);
}
```
Перед изменением проверяет, что участник существует, затем валидирует поля и вызывает
обновление в репозитории.

```java
public void delete(long id) {
    findById(id);
    repository.deleteById(id);
}
```
Не позволяет молча удалять неизвестный ID: сначала делает поиск, затем удаляет.
Связанные записи дополнительно защищены внешним ключом в PostgreSQL.

Метод `validate(Member member)` последовательно проверяет:

- ФИО не равно `null` и не пустое после `isBlank()`;
- телефон существует и не пустой;
- email существует и содержит `@`.

При нарушении `throw new BusinessException(...)` прекращает текущую операцию и
передает понятную причину наверх, в UI.

# 4. Сервис записей на тренировки

Файл: `src/main/java/ru/sportclub/service/TrainingRegistrationService.java`

Импорты подключают бизнес-исключения, модель и enum статуса, оба репозитория, дату,
компаратор, коллекции и Stream Collectors.

```java
private final TrainingRegistrationRepository repository;
private final MemberRepository memberRepository;
```
Сервис хранит зависимости для записей и участников. Первый делает CRUD и проверяет
слоты, второй проверяет существование/активность участника.

Конструктор принимает обе зависимости и присваивает полям.

```java
public TrainingRegistration create(TrainingRegistration registration) {
    validate(registration, false);
    return repository.save(registration);
}
```
При создании запускает все проверки. `false` означает: создавать запись со статусом
COMPLETED нельзя. Если проверки прошли, сохраняет запись.

`findAll()` просто возвращает все записи из репозитория.

`findById(long id)` запрашивает Optional и, если записи нет, выбрасывает
EntityNotFoundException с ID.

```java
public TrainingRegistration update(TrainingRegistration registration) {
    TrainingRegistration current = findById(registration.getId());
    validateStatusTransition(current.getStatus(), registration.getStatus());
    validate(registration, true);
    return repository.update(registration);
}
```
Сначала загружает прежнее состояние. Проверяет допустимость смены статуса, запускает
валидацию (`true` разрешает статус COMPLETED при обновлении), затем сохраняет новую
версию.

```java
public void delete(long id) {
    TrainingRegistration registration = findById(id);
    if (registration.getStatus() == RegistrationStatus.COMPLETED) {
        throw new BusinessException("Завершенную запись нельзя удалить");
    }
    repository.deleteById(id);
}
```
Находит объект, запрещает удаление завершенной тренировки, иначе передает удаление
репозиторию. `==` подходит enum, потому что значения enum - фиксированные объекты.

Поиск:

```java
String normalized = query.toLowerCase();
return findAll().stream()
    .filter(item -> item.getTrainingName().toLowerCase().contains(normalized))
    .toList();
```
Приводит запрос и название к нижнему регистру, чтобы поиск не зависел от регистра.
`stream()` начинает обработку элементов; `filter` оставляет совпавшие; `toList()`
собирает результат в список. Метод по тренеру работает так же, но сравнивает
`trainerName`.

Фильтр по статусу оставляет записи, у которых `item.getStatus() == status`.
Фильтр категории использует `equalsIgnoreCase`, чтобы регистр букв не влиял на поиск.

Сортировка по дате:

```java
return findAll().stream()
    .sorted(Comparator.comparing(TrainingRegistration::getTrainingDate)
        .thenComparing(TrainingRegistration::getStartTime))
    .toList();
```
Сначала сравнивает даты, при совпадении - время. `::getTrainingDate` называется
ссылкой на метод и означает «для элемента вызвать этот геттер».

Сортировка по длительности создает Comparator по `getDurationMinutes()` и вызывает
`reversed()`, поэтому длинные тренировки идут первыми.

```java
public Map<String, Long> statistics() {
    return findAll().stream().collect(Collectors.groupingBy(
        item -> item.getStatus().name(), Collectors.counting()));
}
```
Группирует записи по имени статуса и считает количество в каждой группе. Результат,
например, `{PLANNED=4, CONFIRMED=4, COMPLETED=1, CANCELLED=1}`.

Метод `validate(registration, allowCompleted)` выполняет проверки по порядку:

1. Через memberRepository ищет участника по `memberId`; отсутствие превращается в
   BusinessException.
2. Проверяет `member.isActive()`; неактивный участник не допускается.
3. Проверяет, что название тренировки непустое.
4. Проверяет, что имя тренера непустое.
5. Запрещает дату раньше сегодняшней даты через `isBefore(LocalDate.now())`.
6. Ограничивает длительность диапазоном 30..240 минут.
7. Вызывает `existsAtTime(...)` и запрещает повторный слот. ID текущей записи
   передается как `ignoredId`, чтобы обновление не конфликтовало само с собой.
8. Если `allowCompleted == false`, запрещает сразу создавать завершенную запись.

Метод `validateStatusTransition(current, requested)` проверяет переходы:

- COMPLETED нельзя редактировать;
- CANCELLED нельзя редактировать;
- перевести запись в COMPLETED можно только из CONFIRMED.

Эти проверки находятся в Service, потому что это правила предметной области, а не
правила отображения меню.

# 5. JDBC-репозитории

## Общие понятия JDBC

- `Connection` - открытое соединение с БД.
- `PreparedStatement` - подготовленный параметризованный SQL-запрос.
- `?` в SQL - место параметра; значение передается отдельным методом `setString`,
  `setLong`, `setDate` и т.д. Это защищает от SQL-инъекций и проблем кавычек.
- `ResultSet` - таблица результата SELECT/RETURNING, читаемая по строкам.
- `try (...) {}` - try-with-resources: автоматически закрывает соединение,
  statement и result, даже если внутри возникла ошибка.
- `SQLException` - стандартное checked-исключение JDBC; Repository оборачивает его
  в DataAccessException приложения.

## MemberRepository.java

Класс `implements CrudRepository<Member>`: обещает реализовать каждый метод CRUD.
Поле DatabaseManager получает объект через конструктор.

`save(member)`:

1. SQL INSERT содержит `?` для пяти значений и `RETURNING id` PostgreSQL.
2. В try-with-resources открываются Connection и PreparedStatement.
3. `fill(statement, member)` связывает Java-значения с параметрами SQL.
4. `executeQuery()` получает ResultSet, потому что запрос возвращает ID.
5. `result.next()` перемещается на первую возвращенную строку.
6. `getLong(1)` читает первый столбец ID.
7. ID устанавливается в объект и этот же объект возвращается.
8. При SQLException создается DataAccessException с понятным сообщением.

`findAll()`:

- SELECT запрашивает нужные столбцы из members и сортирует по ID;
- `new ArrayList<>()` создает изменяемый список;
- цикл `while (result.next())` проходит все строки;
- `map(result)` превращает строку в Member;
- готовый список возвращается.

`findById(id)`:

- SQL содержит `WHERE id = ?`;
- `setLong(1, id)` связывает ID с первым параметром;
- если `result.next()` true, возвращается `Optional.of(map(result))`;
- если строки нет, возвращается `Optional.empty()`.

`findByEmail(email)` сейчас загружает всех участников, создает stream, сравнивает
email без учета регистра и возвращает первый Optional. Метод не используется
основным меню; это дополнительный поиск.

`update(member)` заполняет первые пять параметров, шестым передает ID, вызывает
`executeUpdate()` и возвращает обновленный объект. `executeUpdate()` используют для
INSERT/UPDATE/DELETE без возвращаемого набора строк.

`deleteById(id)` запускает `DELETE ... WHERE id = ?`. Если на участника ссылаются
записи, PostgreSQL ограничит удаление через FK `ON DELETE RESTRICT`.

`fill(statement, member)` связывает поля в том порядке, в каком стоят `?` в SQL.
Enum сохраняется как текст через `.name()`, active - как boolean.

`map(result)` читает столбцы по именам и создает Member. `valueOf` преобразует текст
из БД в соответствующее значение MembershipType.

## TrainingRegistrationRepository.java

Класс реализует `CrudRepository<TrainingRegistration>`, хранит DatabaseManager и
получает его через конструктор.

`save(registration)`:

- INSERT добавляет восемь полей и просит PostgreSQL вернуть `id, created_at`;
- `fill` связывает восемь параметров;
- ResultSet получает сгенерированный ID и возвращаемый объект;
- SQL-ошибка становится DataAccessException.

Замечание о текущем коде: INSERT возвращает также created_at, но Java-код присваивает
из ResultSet только ID. Это не мешает последующим чтениям, потому что findById/findAll
загружают created_at из БД; знать эту деталь полезно, если преподаватель спросит.

`findAll()` вызывает общий метод `query` с SELECT и JOIN:

```sql
SELECT r.*, m.full_name AS member_name
FROM training_registrations r
JOIN members m ON m.id = r.member_id
ORDER BY r.id
```
`r` и `m` - короткие псевдонимы таблиц. JOIN добавляет к записи имя участника.

`findById(id)` делает такой же JOIN, добавляя `WHERE r.id = ?` и параметр ID.

`existsAtTime(memberId, date, time, ignoredId)` проверяет, есть ли другая запись
того же участника в эту дату и время. `SELECT EXISTS (...)` возвращает true/false.
`Date.valueOf` и `Time.valueOf` переводят Java-время в JDBC-типы. `id <> ?` исключает
редактируемую запись из проверки.

`update(registration)` формирует UPDATE с восемью полями, вызывает `fill`, затем
девятым параметром задает ID строки для WHERE.

`deleteById(id)` выполняет параметризованный DELETE.

`query(sql)` - вспомогательный метод для SELECT нескольких записей: создает список,
открывает соединение, statement и result, преобразует каждую строку через `map`,
возвращает список и переводит SQLException в DataAccessException.

`fill(statement, registration)` последовательно привязывает 8 значений к знакам `?`.
Дата переводится через `Date.valueOf`, время - через `Time.valueOf`, enum-статус -
через `name()`.

`map(result)` читает столбцы и собирает TrainingRegistration. SQL DATE/TIME/TIMESTAMP
преобразуются в LocalDate/LocalTime/LocalDateTime. `member_name` приходит из JOIN.

# 6. Подключение к БД и ошибки

## DatabaseManager.java

Импорты `Connection`, `DriverManager`, `SQLException` нужны для JDBC.
Поля `url`, `user`, `password` хранят параметры соединения; они `private final`.

Конструктор вызывает `env(name, defaultValue)` для каждой переменной:

- SPORTCLUB_DB_URL, значение по умолчанию `jdbc:postgresql://localhost:5432/sportclub`;
- SPORTCLUB_DB_USER, по умолчанию postgres;
- SPORTCLUB_DB_PASSWORD, по умолчанию postgres.

В Docker Compose URL переопределяется на `jdbc:postgresql://postgres:5432/sportclub`.
Имя `postgres` разрешается Docker DNS внутри сети Compose.

`getConnection()` вызывает `DriverManager.getConnection(url, user, password)` и
возвращает новое JDBC-соединение. SQLException передается вызывающему Repository.

`env(name, defaultValue)` читает переменную процесса. Если она отсутствует (`null`)
или пустая/пробельная (`isBlank`), выбирается значение по умолчанию.

## Исключения

`BusinessException extends RuntimeException`: runtime-исключение бизнес-слоя.
Конструктор передает сообщение родителю через `super(message)`.

`EntityNotFoundException extends RuntimeException`: отдельно обозначает, что
запрашиваемой сущности нет. Сообщение тоже передается в RuntimeException.

`DataAccessException extends RuntimeException`: хранит понятное сообщение и причину.
`super(message + ": " + cause.getMessage(), cause)` сохраняет исходную причину для
диагностики цепочки ошибок.

`serialVersionUID` - технический идентификатор версии сериализуемого исключения;
он не относится к бизнес-логике.

# 7. Консольное приложение

Файл: `src/main/java/ru/sportclub/ui/ConsoleApplication.java`

Импорты подключают три исключения, модели и вложенные enum, сервисы, ExcelExporter,
Path, даты/время, коллекции и Scanner.

Поля:

- `scanner` читает строки из `System.in`;
- `memberService` обрабатывает участников;
- `registrationService` обрабатывает записи;
- `exporter` создает XLSX.

Поля сервисов `final`, потому что зависимости назначаются один раз в конструкторе.
Конструктор получает сервисы от Main и записывает их в поля.

`run()`:

1. `running = true` задает флаг продолжения.
2. `while (running)` повторяет главное меню.
3. `printMainMenu()` печатает пункты.
4. `hasNextLine()` проверяет, остался ли ввод. Если нет, цикл завершается.
5. `nextLine().trim()` получает команду и убирает пробелы по краям.
6. `switch` направляет команды 1..7 в методы; 0 меняет флаг на false.
7. Неизвестный ввод выводит сообщение.
8. Первый catch отдельно обрабатывает ожидаемые исключения приложения.
9. Второй catch ловит прочие исключения ввода/операций и не дает процессу аварийно
   завершиться.
10. После цикла печатается сообщение о завершении.

`printMainMenu()` выводит заголовок и пункты. `println` добавляет перевод строки,
`print` оставляет курсор в строке приглашения ввода.

`membersMenu()` и `registrationsMenu()` работают в бесконечном `while (true)`, пока
пользователь не нажмет 0 или не завершится ввод. Вложенный `switch` вызывает нужный
Service-метод. `return` выходит из метода подменю обратно в главный цикл.

В подменю участников:

- пункт 1 собирает значения и создает Member, затем вызывает create;
- пункт 2 загружает список и печатает его;
- пункт 3 ищет по ID;
- пункт 4 загружает участника, переключает boolean через `!`, вызывает update;
- пункт 5 удаляет по ID;
- пункт 0 возвращается в главное меню.

В подменю записей пункты выполняют аналогичные CRUD-операции для TrainingRegistration.
`newRegistration()` запрашивает поля, преобразует дату через LocalDate.parse,
время через LocalTime.parse и целые значения через inputInt. Новая запись получает
статус PLANNED.

`updateRegistration()` сначала загружает текущую запись, чтобы сохранить ID,
memberId, memberName и createdAt. Затем считывает изменяемые поля и вызывает update.

`searchMenu()` предлагает два варианта. Тернарное выражение выбирает поиск по
названию, если введено "1", иначе поиск по тренеру. Результат печатается.

`filterMenu()` использует switch для фильтра по статусу/категории и двух сортировок.

`printStatistics()` берет список записей и Map количества по статусам, получает
участников, считает активных через Stream (`filter(...).count()`) и печатает
показатели. Статистика отображения частично собирается в UI: общий список и активные
участники считаются здесь, группировка по статусу приходит из Service.

`export()` загружает обе коллекции, задает путь `exports/sport-club-data.xlsx`,
вызывает экспорт и печатает абсолютный путь результата.

`printTables()` печатает заголовок первой таблицы, всех участников, заголовок второй
таблицы и все записи.

Однострочные методы `printMembers` и `printRegistrations` вызывают `forEach` и
передают каждый объект в `System.out.println` через ссылку `System.out::println`.

`input(label)` печатает приглашение, читает строку, удаляет внешние пробелы.
`inputLong` и `inputInt` разбирают целое число. При NumberFormatException выбрасывают
BusinessException с объясняющим сообщением.

`status()` преобразует введенный текст в верхний регистр и ищет соответствующее
значение enum через `valueOf`. `membershipType()` делает то же для абонемента.
Если значение не совпало с enum, `valueOf` бросит IllegalArgumentException, который
попадет в общий обработчик run().

# 8. Экспорт Excel

Файл: `src/main/java/ru/sportclub/util/ExcelExporter.java`

Импорты Apache POI предоставляют Workbook, Sheet, Row и XSSFWorkbook. Java-импорты
нужны для файлов, путей и списков.

```java
public Path export(List<Member> members,
                   List<TrainingRegistration> registrations, Path output)
```
Принимает готовые данные и путь назначения; возвращает путь созданного файла.
Экспортер не ходит в БД, данные ему предоставляет UI через сервисы.

`Files.createDirectories(output.getParent())` создает папку exports, если ее еще нет.
`new XSSFWorkbook()` создает книгу Excel формата XLSX. try-with-resources закрывает
книгу после работы.

Лист участников:

1. `createSheet("Участники")` создает вкладку.
2. Массив `memberHeaders` задает заголовки колонок.
3. `createRow(0)` создает первую строку заголовка.
4. Цикл по индексам записывает каждый заголовок в ячейку.
5. Второй цикл идет по участникам, создает строку `rowIndex + 1` и записывает ID,
   ФИО, телефон, email, абонемент и активность в отдельные ячейки.
6. `autoSizeColumn` подбирает ширину колонки по содержимому.

Лист записей создается аналогично. Каждая запись переносится в строку: ID, участник,
тренировка, тренер, дата, время, минуты, статус, категория. Дата и время записываются
текстовым представлением `toString()`.

`Files.newOutputStream(output)` открывает файл для записи. `workbook.write(stream)`
записывает XLSX в поток. Оба ресурса закрываются автоматически.

При IOException метод выбрасывает IllegalStateException с сообщением и исходной
причиной. При успехе возвращает `output`.

# 9. SQL-схема

Файл: `database/schema.sql`

Таблица members:

- `CREATE TABLE IF NOT EXISTS` создает таблицу только если ее еще нет;
- `BIGSERIAL PRIMARY KEY` - автоматически увеличиваемый ID и первичный ключ;
- `VARCHAR(n)` - строка с максимальной длиной;
- `NOT NULL` запрещает отсутствие значения;
- `UNIQUE` запрещает повтор телефона или email;
- `CHECK (...)` ограничивает membership_type допустимыми enum-значениями;
- `BOOLEAN DEFAULT TRUE` задает активность по умолчанию.

Таблица training_registrations:

- `member_id BIGINT NOT NULL REFERENCES members(id)` - обязательная ссылка на участника;
- `ON DELETE RESTRICT` запрещает удалить участника, пока есть связанные записи;
- дата, время, длительность, статус, категория и дата создания описывают тренировку;
- CHECK длительности ограничивает значение от 30 до 240;
- CHECK статуса ограничивает четырьмя допустимыми статусами;
- `DEFAULT CURRENT_TIMESTAMP` автоматически ставит время создания.

```sql
CREATE UNIQUE INDEX ...
ON training_registrations (member_id, training_date, start_time);
```
Уникальный составной индекс не допускает повторную запись одного участника на тот же
слот. Это защита на уровне БД, даже если проверку Java обошли.

`INSERT INTO members ... VALUES ... ON CONFLICT DO NOTHING` добавляет стартовых
участников и игнорирует конфликт уникальности, например повторный email.

Большой INSERT записей использует `SELECT` и `JOIN (VALUES ...)`: тестовые строки
содержат email участника, тренировки, дату, время и прочие данные. JOIN находит ID
участника по email. `WHERE NOT EXISTS` добавляет эти тестовые записи только если
таблица записей пустая.

# 10. Maven и Docker

## pom.xml

- `<groupId>` и `<artifactId>` задают Maven-координаты проекта.
- `<version>` задает версию.
- `maven.compiler.source/target=17` означает компиляцию для Java 17.
- `project.build.sourceEncoding=UTF-8` задает кодировку исходников.
- dependency PostgreSQL добавляет JDBC-драйвер.
- dependency poi-ooxml добавляет создание XLSX.
- exec-maven-plugin позволяет запускать приложение Maven-командой.
- `<mainClass>ru.sportclub.Main</mainClass>` задает точку входа.

## Dockerfile

```dockerfile
FROM maven:3.9.11-eclipse-temurin-17 AS build
```
Первый этап сборки содержит Maven и JDK 17.

```dockerfile
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
```
Задает папку сборки, копирует Maven-конфигурацию и заранее скачивает зависимости.
`-q` уменьшает вывод.

```dockerfile
COPY src ./src
RUN mvn -q -DskipTests package
RUN mvn -q dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency
```
Копирует исходники, собирает JAR/классы без тестов и отдельно складывает runtime-
зависимости в папку target/dependency.

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/classes ./target/classes
COPY --from=build /app/target/dependency ./target/dependency
ENTRYPOINT ["java", "-cp", "target/classes:target/dependency/*", "ru.sportclub.Main"]
```
Второй этап содержит только JRE, а не компилятор. Копируются классы и библиотеки из
этапа build. ENTRYPOINT запускает Main с classpath классов и зависимостей.

## docker-compose.yml

Сервис `postgres` берет готовый образ postgres:16-alpine, создает базу и пользователя,
публикует порт контейнера 5432 на порт компьютера 5433, сохраняет данные в volume и
монтирует schema.sql для первого запуска пустой БД.

Healthcheck вызывает `pg_isready`; Compose считает БД готовой, когда проверка успешна.
Сервис `app` собирается из Dockerfile, ждет `service_healthy`, получает параметры БД
через environment. Внутри сети хост БД называется `postgres`, порт 5432.
`stdin_open: true` и `tty: true` сохраняют интерактивный ввод в контейнере.
Именованный volume `sportclub-postgres-data` хранит данные независимо от жизненного
цикла контейнера; `docker compose down -v` удаляет и volume.

# 11. Что полезно уметь написать от руки

## CRUD-интерфейс

Вопрос: как описать общий контракт репозитория?

```java
public interface CrudRepository<T> {
    T save(T entity);
    List<T> findAll();
    Optional<T> findById(long id);
    T update(T entity);
    void deleteById(long id);
}
```

Объяснение: `T` делает контракт универсальным; Optional отражает возможное отсутствие
результата; методы соответствуют Create, Read, Update, Delete.

## Валидация

Вопрос: как запретить пустое название тренировки?

```java
if (registration.getTrainingName() == null
        || registration.getTrainingName().isBlank()) {
    throw new BusinessException("Название тренировки обязательно");
}
```

`||` - ИЛИ. Первая часть защищает от null, вторая проверяет пустую строку/пробелы.
При true выбрасывается бизнес-ошибка, сохранение не выполняется.

## Параметризованный поиск JDBC

```java
String sql = "SELECT id FROM members WHERE id = ?";
try (Connection connection = database.getConnection();
     PreparedStatement statement = connection.prepareStatement(sql)) {
    statement.setLong(1, id);
    try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
            return Optional.of(map(result));
        }
        return Optional.empty();
    }
}
```

Первый `?` заполняется ID. SQL-код и данные передаются отдельно. ResultSet содержит
результат SELECT. `next()` проверяет наличие первой строки. try-with-resources закрывает
ресурсы автоматически.

## Поиск Stream API

```java
return findAll().stream()
        .filter(item -> item.getTrainerName().toLowerCase().contains(query.toLowerCase()))
        .toList();
```

Получаем записи, создаем поток, оставляем те, чье имя тренера содержит запрос без
учета регистра, собираем в список.

## Статистика группировкой

```java
return findAll().stream().collect(Collectors.groupingBy(
        item -> item.getStatus().name(), Collectors.counting()));
```

Ключ Map - название статуса, значение - число записей с таким статусом.

## Подключение объекта к слою

```java
MemberService service = new MemberService(memberRepository);
```

Сервис получает репозиторий через конструктор. Он не создает БД сам, поэтому его
зависимость видна явно и может быть заменена другой реализацией интерфейса.

# 12. Вопросы преподавателя и короткие ответы

**Почему SQL не в меню?**
Меню отвечает за ввод/вывод. SQL хранится в Repository, правила - в Service. Так
слои имеют отдельные обязанности.

**Зачем PreparedStatement?**
Параметры передаются отдельно от SQL. Это безопаснее конкатенации строк и корректно
обрабатывает кавычки и типы значений.

**Чем Statement отличается от PreparedStatement?**
Statement исполняет готовую SQL-строку. PreparedStatement заранее готовит запрос с
параметрами `?`, которые затем задаются типизированными методами.

**Зачем интерфейс CrudRepository?**
Он задает общий контракт операций и позволяет сервису зависеть от интерфейса, а не от
конкретной реализации. Это пример абстракции и полиморфизма.

**Почему одновременно есть проверка слота в Java и уникальный индекс SQL?**
Service заранее показывает понятную ошибку пользователю; уникальный индекс остается
последней гарантией целостности на стороне БД.

**Что будет при удалении участника с записями?**
PostgreSQL отклонит удаление из-за внешнего ключа ON DELETE RESTRICT. Сначала нужно
удалить связанные записи.

**Где реально хранится Docker-БД?**
В именованном Docker volume `sportclub-postgres-data`, не внутри пересоздаваемого
контейнера PostgreSQL.

**Почему Docker URL содержит postgres, а не localhost?**
Внутри Docker Compose `postgres` - DNS-имя сервиса. `localhost` внутри app-контейнера
указывал бы на сам app-контейнер.

**Что делает `docker compose down -v`?**
Останавливает и удаляет контейнеры и также удаляет volume. Данные БД будут потеряны.

# 13. План подготовки

1. Прочитать разделы 1-4 и без подсказки нарисовать цепочку вызовов.
2. Написать по памяти CRUD-интерфейс и объяснить `T` и `Optional`.
3. Написать один метод Repository: SQL, PreparedStatement, ResultSet, try-with-resources.
4. Написать одну бизнес-проверку и объяснить, почему она находится в Service.
5. Повторить модель, enum, JOIN, внешний ключ и уникальный индекс.
6. Попросить кого-нибудь выбрать случайный раздел, закрыть документ и объяснить
   фрагмент своими словами.
7. В конце проговорить один полный сценарий: пользователь создает запись -> UI ->
   Service validation -> Repository INSERT -> PostgreSQL возвращает ID -> UI.

Главная цель - понимать поток данных. Если случайный фрагмент забылся, восстанови
его назначение: кто вызывает этот класс, что он получает, что возвращает и какие
ошибки может передать выше.
