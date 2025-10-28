# UMRS backend
![Java Version](https://img.shields.io/badge/Java-21-green.svg)
![Spring Boot Version](https://img.shields.io/badge/Spring%20Boot-3.5.7-indigo.svg)
![Postgres Version](https://img.shields.io/badge/PostgreSQL-18.3.6-blue.svg)
![Gradle version](https://img.shields.io/badge/Gradle-18.3.6-orange.svg)

## Новый API

- `http://localhost:8080/redoc-v1.html` - [Redoc](https://redocly.com/redoc)
- `http://localhost:8080/docs/v1` - [Open API](https://swagger.io/specification/) JSON

## Legacy API пока не переехали

- `http://localhost:8080/redoc-v0.html` - [Redoc](https://redocly.com/redoc)
- `http://localhost:8080/docs/v0` - [Open API](https://swagger.io/specification/) JSON

# Решения

- `Spring MVC` (в `WebFlux` точно нет необходимости в начале сейчас, важна скорость разработки, надежность и перспектива
  масштабирования)
- Не использовать прямое соединение между фронт-эндом и бэк-эндом, используя эти "database security rules" или что-то в
  этом роде, из-за CVE-2024-45489 и растущей сложности поддержки этих правил по мере роста схемы
  БД (см.(https://youtu.be/2zcN2aQsUdc?si=80NfsuOA2KI770CH)).

# Почему не supabase (было в планах)

- Supabase - это fauxpen source на самом деле в какой-то незначительной (зависит от случая) мере.
- В ошибках, в репозиториях этого Supabase, можно найти немало недовольных политикой работы с open source community
- Supabase обновляется регулярно, не сильно обращая внимания на документирование для self-hosted, а его инфраструктура
  не самая простая и поддерживать её, конфигурировать или справляться с появляющимися по тем или иным причинам
  ошибками - сложная задача, подсильная или прям нормальным специалистм, или разработчикам supabase (субъективно)
- Немало неприятных ограничений в self-hosted версии. Самым критичным является запрет создания более одного проекта в
  одном instance'е Supabase, но помимо этого, там ещё хватает неудобств разного рода.

