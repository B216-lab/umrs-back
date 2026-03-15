# AGENTS.md

> Reference: [java.evolved](https://javaevolved.github.io/) — side-by-side comparisons of old Java
> patterns vs clean modern replacements (Java 9–25). Consult before writing any boilerplate.

## 1. Git

- Use conventional commit messages; title ≤ 100 characters
- Never install a new dependency without asking first
- Never modify the database schema without showing the migration plan

## 2. Java 25 Style

- Target: **Java 25 LTS**. Use modern idioms — never write pre-Java 17 patterns when a modern
  equivalent exists
- **Records** for all immutable data carriers: DTOs, value objects, events, projections.
  Never write a POJO with getters/setters for data that does not need mutability
- **Sealed interfaces/classes** to model closed domain hierarchies; rely on exhaustive `switch` for
  dispatch — never use `if-else` chains on type
- **Pattern matching** (`instanceof`, `switch`, record patterns) — eliminate manual casting and
  accessor chains. Use guarded patterns (`when` clause) instead of nested `if` inside a branch
- **Switch expressions** — prefer expression form (`->`) over fallthrough statements
- **Text blocks** (`"""`) for multiline strings: SQL, JSON, templates. Never concatenate multiline
  strings
- **`var`** only when the type is obvious from the right-hand side (constructor, literal, factory
  method). Never use `var` when the inferred type is ambiguous
- **`Optional`** for return types that may be absent. Never use `Optional` as a field, parameter, or
  collection element
- **Unnamed variables** (`_`) for unused lambda params and catch bindings
- **Sequenced collections** (`SequencedCollection`, `SequencedMap`) — use `getFirst()` / `getLast()`
  instead of index arithmetic
- **Immutable collection factories** — `List.of()`, `Set.of()`, `Map.of()`. Never return a mutable
  collection from a public method when the caller should not mutate it
- **Stream `.toList()`** instead of `.collect(Collectors.toList())`
- **`String` helpers** — `isBlank()`, `strip()`, `repeat()`, `formatted()` — never hand-roll these
- **Structured concurrency** (`StructuredTaskScope`) and **scoped values** for concurrent tasks —
  never spawn unstructured threads
- **Flexible constructor bodies** — initialize fields before `super()` / `this()` when it
  simplifies logic
- Cognitive complexity per method ≤ 15. Extract helper methods when approaching the limit
- Prefer early returns; avoid unnecessary `else`
- No magic numbers or strings — extract constants or enums
- No wildcard imports (`*`). Keep imports explicit and sorted

## 3. Spring Boot

### Architecture

- **Feature-based packages**: `features/<domain>/controller|service|repository|domain|dto|model`
- Controllers hold zero business logic — delegate to services
- Only `@Service` classes carry `@Transactional` at the class level
- Repositories are interfaces extending Spring Data JPA interfaces
- Domain entities live in `domain/`; enums and value types in `model/`; DTOs in `dto/`

### Dependency Injection

- **Constructor injection only** in production code. Annotate with `@RequiredArgsConstructor`
  (Lombok) when all dependencies are `final` fields
- `@Autowired` field injection is allowed **only** in test classes
- Avoid circular dependencies. If detected — refactor, do not use `@Lazy` or `@Order`

### Configuration

- `@ConfigurationProperties` for 2+ related properties — never scatter multiple `@Value` fields
- Profile-specific YAML files: `application-{profile}.yaml`
- Use `true`/`false` in YAML — never `yes`/`no`
- Secrets come from environment variables; never hardcode them

### Validation & Error Handling

- `@Validated` / `@Valid` on controller parameters. Use Bean Validation annotations on DTOs
- Custom domain exceptions extending `RuntimeException`
- Global error handler via `@ControllerAdvice` + `@ExceptionHandler`
- Map exceptions to proper HTTP status codes; return a consistent error body

### Persistence

- **Flyway** manages all schema changes. Never alter the database manually
- JPA entity classes in `domain/` — reference tables suffixed `Ref` (e.g. `MovementTypeRef`)
- Prefer JPQL or derived queries; drop to native SQL only when JPA cannot express the query
- Always set fetch type explicitly — avoid N+1 issues

### Testing

- JUnit 5 + Mockito for unit tests
- `@SpringBootTest` for integration tests; `@WebMvcTest` for controller-layer tests
- Test method names: `should_<expected>_when_<condition>` (snake_case)
- Structure test body as `// given … // when … // then`
- No reflection hacks. No business logic in tests
- Contract tests use Pact; HTTP stubs use WireMock

### Logging

- `@Slf4j` (Lombok) — never instantiate loggers manually
- Placeholders (`{}`) — never string concatenation
- Template: `log.info("[ModuleName] ACTION: field: {}", value)`
- Never log secrets, tokens, or passwords

### Lombok

- `@RequiredArgsConstructor` for DI
- `@Slf4j` for logging
- `@Builder(setterPrefix = "with")` for complex construction
- `@Getter` / `@Setter` only on mutable JPA entities — prefer records for everything else
- Never use `@Data` (it implies `@EqualsAndHashCode` on entities, which breaks JPA)

## 4. Build

- **Gradle Kotlin DSL** (`build.gradle.kts`)
- Run tests: `./gradlew test`
- Run app: `./gradlew bootRun`
- DB migrations: Flyway runs automatically on startup
