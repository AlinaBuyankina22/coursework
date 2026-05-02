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

## Java: установка и проверка (Windows)

Нужен **JDK** (не только JRE). Для этого проекта удобнее **JDK 21** (в `pom.xml` указано `java.version=21`).

### Самый простой запуск из PowerShell (без ручного JAVA_HOME)

В корне проекта есть `run.ps1`: он попытается поставить **Temurin JDK 21** через `winget` (если Java не найдена), сам выставит `JAVA_HOME` **только для текущего окна PowerShell** и запустит приложение.

Две команды:

```powershell
cd C:\Users\alian\Desktop\kurs\sqli-demo\sqli-demo
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

Примечания:

- Если `winget install` попросит права администратора — откройте PowerShell **от имени администратора** и повторите.
- Если `winget` недоступен (редко, но бывает) — используйте ручную установку JDK ниже.

### Установка

1) Скачайте и установите JDK 21, например **Eclipse Temurin** (Adoptium):  
   https://adoptium.net/temurin/releases/?version=21

2) Запомните папку установки. Обычно это что-то вроде:
   - `C:\Program Files\Eclipse Adoptium\jdk-21.x.x-hotspot\`
   - или `C:\Program Files\Java\jdk-21\`

### Переменные среды (`JAVA_HOME` и `Path`)

Если при запуске `mvnw.cmd` появляется ошибка вида **“JAVA_HOME is not defined correctly”**, настройте переменные:

1) **Параметры Windows → Система → О системе → Дополнительные параметры системы → Переменные среды**
2) Создайте (или исправьте) системную переменную:
   - **Имя**: `JAVA_HOME`
   - **Значение**: путь к JDK **без** `\bin` (например `C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot`)
3) В переменной **Path** добавьте строку:
   - `%JAVA_HOME%\bin`

Закройте и снова откройте PowerShell / IDE после изменений.

### Проверка в PowerShell

```powershell
java -version
javac -version
echo $env:JAVA_HOME
where.exe java
```

Ожидаемо: `java -version` показывает **21.x**, `javac -version` тоже доступен, `where.exe java` указывает на файл внутри `%JAVA_HOME%\bin`.

Если `java` есть, а `JAVA_HOME` пустой — Maven Wrapper всё равно может ругаться: тогда либо задайте `JAVA_HOME`, либо временно (только для текущего окна):

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

## Как запустить (IntelliJ IDEA)

1) Откройте проект папкой:

`c:\Users\alian\Desktop\kurs\sqli-demo\sqli-demo`

2) Проверьте JDK (Project SDK) = **21**.

3) Дождитесь, пока Maven подтянет зависимости.

4) Запустите `ru.kurs.sqlidemo.SqliDemoApplication` (зелёная кнопка Run).

## Как запустить (PowerShell)

Если Java уже установлена и `JAVA_HOME` настроен корректно:

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

Вставьте в поле **Username**, пароль можно любой:

- **`' OR '1'='1' --`**

Ожидаемо:

- Login (unsafe): может залогинить без нормального пароля
- Login (safe / safe ORM): не должен залогинить

### 2) UNION‑SQLi в поиске (unsafe)

Вставьте в поле **Search query (q)**:

- **`%' UNION SELECT 1, username, password, secret_note FROM users --`**

Ожидаемо:

- Search (unsafe): можно получить `password` и `secret_note`
- Search (safe Prepared / safe ORM): UNION не сработает

## Как это связано с планом

По идее, в работе нужно показать “до/после”, поэтому сделано так:

- параметризованные запросы: `UserJdbcDao` (safe Prepared)
- ORM: `UserRepository` + `UserService` (safe ORM)
- валидация: ограничения в `DemoController` (длина, и для `q` white‑list)



