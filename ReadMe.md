# UMRS backend

![Java Version](https://img.shields.io/badge/Java-21-green.svg)
![Spring Boot Version](https://img.shields.io/badge/Spring%20Boot-4-indigo.svg)
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
## Agentic development
Every new session should start with something like: 

> Read AGENTS.md and confirm you understand the project constraints before doing anything

Recommend to follow prompt structure:
- GOAL
- CONSTRAINTS
- FORMAT
- FAILURE CONDITIONS 

## 📝 License

This project is licensed under the GPL-3.0 license - see the [LICENSE](./LICENSE) file for details.
