# SQLi demo (курсовая)

Небольшое веб‑приложение на Spring Boot, в котором можно проверить SQL‑инъекции и посмотреть, как они устраняются.

Что есть в приложении:

- логин (unsafe / safe Prepared / safe ORM)
- поиск пользователей (unsafe / safe Prepared / safe ORM)
- простая валидация ввода (не вместо параметров, а как доп. слой)

## Стек

- Java 21 (подойдёт 17+, но в проекте выставлено 21)
- Spring Boot 3
- Maven
- H2 in‑memory
- Thymeleaf
- Spring Data JPA + JDBC
- Bean Validation (jakarta.validation)

## Как запустить (IntelliJ IDEA)

1) Открой проект папкой:

`c:\Users\alian\Desktop\kurs\sqli-demo\sqli-demo`

2) Проверь JDK (Project SDK) = **21**.

3) Дождись, пока Maven подтянет зависимости.

4) Запусти `ru.kurs.sqlidemo.SqliDemoApplication` (зелёная кнопка Run).

## Как запустить (PowerShell)

```powershell
cd C:\Users\alian\Desktop\kurs\sqli-demo\sqli-demo
.\mvnw.cmd clean spring-boot:run
```

Открыть:

- **UI**: `http://localhost:8080/`

## Данные

Тестовые пользователи (см. `src/main/resources/data.sql`):

- `admin / admin123`
- `alice / alice123`
- `bob / bob123`
- `charlie / charlie123`

## Payload’ы для демонстрации

### 1) Login bypass (unsafe)

Вставить в поле **Username**, пароль можно любой:

- **`' OR '1'='1' --`**

Ожидаемо:

- Login (unsafe): может залогинить без нормального пароля
- Login (safe / safe ORM): не должен залогинить

### 2) UNION‑SQLi в поиске (unsafe)

Вставить в поле **Search query (q)**:

- **`%' UNION SELECT 1, username, password, secret_note FROM users --`**

Ожидаемо:

- Search (unsafe): можно получить `password` и `secret_note`
- Search (safe Prepared / safe ORM): UNION не сработает

## Как это связано с планом

По идее, в работе нужно показать “до/после”, поэтому сделано так:

- параметризованные запросы: `UserJdbcDao` (safe Prepared)
- ORM: `UserRepository` + `UserService` (safe ORM)
- валидация: ограничения в `DemoController` (длина, и для `q` white‑list)

Наименьшие привилегии для БД: в реальных проектах это важно, но в учебном PoC на H2 я это отдельно не настраивал.


