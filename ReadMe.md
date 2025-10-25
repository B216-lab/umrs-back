# UMRS backend

# Решения
- `Spring MVC` (в `WebFlux` точно нет необходимости в начале сейчас, важна скорость разработки и надежность)
- Don't use direct connection between front-end and back-end using these sweet "database security rules" or that kind of stuff, because of CVE-2024-45489 and increasing complexity of supporting these rules as DB's schema is growing ([look](https://youtu.be/2zcN2aQsUdc?si=80NfsuOA2KI770CH))

# Почему не supabase (было в планах)

- Supabase - это fauxpen source на самом деле в какой-то незначительной (зависит от случая) мере.
- В ошибках, в репозиториях этого Supabase, можно найти немало недовольных политикой работы с open source community
- Supabase обновляется регулярно, не сильно обращая внимания на документирование для self-hosted, а его инфраструктура не самая простая и поддерживать её, конфигурировать или справляться с появляющимися по тем или иным причинам ошибками - сложная задача, подсильная или прям нормальным специалистм, или разработчикам supabase (субъективно)
- Немало неприятных ограничений в self-hosted версии. Самым критичным является запрет создания более одного проекта в одном instance'е Supabase, но помимо этого, там ещё хватает неудобств разного рода.

