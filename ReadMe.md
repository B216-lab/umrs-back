# UMRS backend

![Java Version](https://img.shields.io/badge/Java-25-green.svg)
![Spring Boot Version](https://img.shields.io/badge/Spring%20Boot-4-indigo.svg)
![Postgres Version](https://img.shields.io/badge/PostgreSQL-18.3.6-blue.svg)

## OpenAPI and Swagger (development)

Swagger and OpenAPI are exposed only when the `development` profile is active.

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON (group `v1`): `http://localhost:8081/v3/api-docs/v1`
- OpenAPI YAML (group `v1`): `http://localhost:8081/v3/api-docs.yaml?group=v1`

### Run locally with development profile

```bash
SPRING_PROFILES_ACTIVE=development ./gradlew bootRun
```

### Export OpenAPI files for frontend/mock servers

Run the dedicated export task:

```bash
./gradlew exportOpenApi
```

Generated artifacts:

- `build/openapi/openapi.json`
- `build/openapi/openapi.yaml`

Frontend teams can import either file into tools like Prism, WireMock, or other OpenAPI-compatible mock server generators.


## Contributing

Look at [contributing.md](https://github.com/B216-lab/.github/blob/main/.github/.github/CONTRIBUTING.md)

## 📝 License

This project is licensed under the GPL-3.0 license - see the [LICENSE](./LICENSE) file for details.
