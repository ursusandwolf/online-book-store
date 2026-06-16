# Project Context: Online Book Store

## Current State
The project has successfully implemented a robust Security module based on Spring Security 6.
- **Authentication**: `User` entity implements `UserDetails`. Authentication is email-based via `CustomUserDetailsService`.
- **RBAC**: Full Role-Based Access Control implemented with `USER` and `ADMIN` roles.
- **API Security**: All endpoints are secured. Public access is granted to registration, Swagger, and error endpoints.
- **Data Integrity**: Liquibase migrations are idempotent (using preConditions) and maintain strict schema validation compatible with Hibernate.
- **Code Quality**: 0 Checkstyle violations. Code follows Mate Academy style guides.

## Recent Changes
- Improved exception message in `UserServiceImpl` for missing default role.
- Fixed Liquibase migration errors by adding `preConditions` to changelog `04`.
- Resolved Hibernate validation issues for Enum fields using `columnDefinition = "varchar"`.
- Corrected all Checkstyle violations (import order, line length).
- Verified `UserServiceImpl` properly hashes passwords and assigns the default `USER` role.
- Updated API paths to include `/api` prefix to match project requirements.

## Pending Items
- **Testing**: While the core logic is implemented and verified manually, adding automated integration tests for all Use Cases is recommended for the next phase.
- **JWT Implementation**: The current setup uses Basic Auth. Transitioning to JWT would be a logical next step for a production-ready API.
- **Naming Conventions**: Adopted plural naming for join tables (e.g., `users_roles` instead of `user_roles`) for consistency with base tables.
- **Soft Delete**: Implemented `is_deleted` logic for both `Book` and `User` entities using Hibernate `@SQLDelete` and `@Where` annotations.
