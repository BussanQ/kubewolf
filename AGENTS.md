# Repository Guidelines

## Project Structure & Module Organization

This is a single-module Spring Boot application. Java sources live under `src/main/java/com/bussanq/kubewolf/`: `api` contains REST controllers, services, and database models; `ai` handles inference deployment and Kubernetes events; `common` provides infrastructure utilities; `web` handles pages and view configuration.

Resources live in `src/main/resources/`: `templates` contains JFinal Enjoy pages, `static` contains browser assets, `sql` contains query templates, and `k8s` contains Kubernetes resource templates. Infrastructure scripts are in `scripts/` and `sealosbuild/`. See `docs/fix.md` for known gaps. No test source tree currently exists; add tests under `src/test/java/` mirroring production packages.

## Build, Test, and Development Commands

Install JDK 23 and Maven; no Maven Wrapper is included.

- `mvn -DskipTests compile`: compile production sources.
- `mvn -DskipTests package`: build the executable JAR under `target/`.
- `mvn spring-boot:run`: run locally after configuring MySQL and Kubernetes.
- `mvn -Dtest=ServeServiceTest test`: run a specific test class once test dependencies and tests exist.

`scripts/install.sh` provisions cluster infrastructure through Sealos; it is not a local development startup script.

## Coding Style & Naming Conventions

Use four-space indentation for new handwritten Java code, preserving existing formatting in touched files. Use PascalCase for classes, camelCase for methods and fields, and lowercase package names. Follow existing service names such as `ServeService` and controller names such as `ServingC`.

Keep controllers thin and business logic in services. Preserve snake_case database mappings and consistent request fields across Java, JavaScript, and templates. Treat generated `Base*` models and `_MappingKit` as generator-owned. No formatter or lint configuration is currently provided.

## Testing Guidelines

No test framework dependency or coverage threshold is configured. Add appropriate test-scoped dependencies when introducing tests; name classes `*Test`. Run only necessary tests for affected modules, not the full suite. Focus on API contracts, framework selection, deployment parameters, and cleanup failures. Mock Kubernetes and gateway calls for unit tests; report unavailable integration dependencies explicitly.

## Commit & Pull Request Guidelines

History uses short subjects such as `update kubewolf`; no formal commit format is established. Use concise Chinese commit messages for new commits. Do not create commits unless explicitly requested.

PRs should explain the problem, behavior change, focused validation, and configuration impacts. Link relevant issues and include screenshots for UI changes.

## Security & Configuration

Use environment overrides for database credentials and One-API tokens. Configure `k8s.config` and `k8s.namespace` explicitly; the checked-in kubeconfig path is machine-specific. Never commit credentials or kubeconfig contents. Database initialization scripts are currently missing.
