# Releases

Stable versions are published automatically when a **version tag** is pushed to this repository.

## Creating a release

1. Merge your work to `main`.
2. Create an annotated or lightweight tag on a commit that is already on `main`:
   ```bash
   git fetch origin main
   git checkout main
   git pull
   git tag v1.2.3
   git push origin v1.2.3
   ```

The [Release workflow](../.github/workflows/release.yml) then:

- Confirms the tagged commit is in the history of `main` (releases must be cut from `main`).
- Runs the same Docker stack **smoke test** used in PR validation (`reusable-docker-smoke`).
- Exports **OpenAPI** (`openapi.json` and `openapi.yaml`) and attaches them to the GitHub Release.
- Builds and pushes a **Docker image** to GHCR with labels and image tags derived from the git tag; the JAR is built with matching `appVersion`.

Restricting who can create tags or run workflows is done in **repository or organization settings** (rulesets, branch/tag protection, environment protection), not in this workflow.

## Manual Docker image (without a tag release)

Use the [Publish Docker Image workflow](../.github/workflows/publish-docker.yml) (`workflow_dispatch`) to push an image manually. Prefer tag-based releases for production artifacts.

## Integration tests

Database-backed integration tests are disabled by default. Run with a reachable PostgreSQL (for example the stack from `ci-compose.yml`) and:

```bash
export UMRS_RUN_INTEGRATION_TESTS=true
./gradlew test
```
