# SQLi demo (PoC): уязвимо vs безопасно

Spring Boot PoC для демонстрации SQL‑инъекции и её устранения.

Что показано:

- **Логин (обход аутентификации)**:
  - unsafe: конкатенация SQL
  - safe (Prepared): параметризованный запрос (PreparedStatement/JdbcTemplate)
  - safe (ORM): доступ через Spring Data JPA (репозиторий), без конкатенации SQL
- **Поиск (UNION‑SQLi + утечка данных)**:
  - unsafe: `LIKE '%<input>%'` через конкатенацию
  - safe (Prepared): `LIKE ?` через параметр
  - safe (ORM): `findByUsernameContainingIgnoreCase(q)` (параметризовано)
- **Валидация ввода (Defense in Depth)**:
  - ограничения по длине
  - white‑list по символам для `q` (буквы/цифры/пробел/_/-)

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

- **Login (unsafe)**: успешно (подставится первый попавшийся пользователь)
- **Login (safe)**: неуспешно

### 2) UNION‑SQLi в поиске (unsafe)

Вставить в поле **Search query (q)**:

- **`%' UNION SELECT 1, username, password, secret_note FROM users --`**

Ожидаемо:

- **Search (unsafe)**: вернёт строки с `password` и `secret_note` (утечка)
- **Search (safe Prepared / safe ORM)**: UNION не сработает (ввод интерпретируется как данные)

## Как это ложится на план курсовой

- **1.3 Методы защиты**:
  - параметризованные запросы → `UserJdbcDao.loginSafePrepared()` / `UserJdbcDao.searchSafe()`
  - ORM → `UserService` + `UserRepository` (Spring Data JPA)
  - валидация → аннотации `@Size/@Pattern/@NotBlank` в `DemoController`
  - наименьшие привилегии → рекомендуется как конфигурационная мера (для H2 PoC ограниченно демонстрируемо)

- **Глава 3 (реализация и тестирование)**:
  - 3.2 безопасное взаимодействие с БД → сравнение `/login/unsafe` vs `/login/safe` vs `/login/safe-orm`; `/search/unsafe` vs `/search/safe` vs `/search/safe-orm`
  - 3.3 PenTest → ввод payload’ов из раздела выше и проверка ожидаемого результата
  - 3.4 Эффективность → unsafe ломается (bypass/UNION), safe‑варианты не ломаются


