# UMRS backend

![Java Version](https://img.shields.io/badge/Java-21-green.svg)
![Spring Boot Version](https://img.shields.io/badge/Spring%20Boot-3.5.7-indigo.svg)
![Postgres Version](https://img.shields.io/badge/PostgreSQL-18.3.6-blue.svg)
![Gradle version](https://img.shields.io/badge/Gradle-18.3.6-orange.svg)

## 🔧 Development

```bash
docker compose -f compose-dev.yaml up -d
./gradlew bootRun
```

or using devcontainer:

```bash
devcontainer up
```

## 📖 Usage

- `http://localhost:8081/redoc-v1.html` - [Redoc](https://redocly.com/redoc)
- `http://localhost:8081/docs/v1` - [Open API](https://swagger.io/specification/) JSON
- `http://localhost:8025` - MailPit

## 📝 License

This project is licensed under the GNUv3 License - see the LICENSE file for details.
