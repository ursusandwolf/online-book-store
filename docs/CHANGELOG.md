# Changelog

## [Unreleased] - 2026-06-16

### Changed
- Improved error message in `UserServiceImpl` to include `RoleName.USER` when default role is not found.
- Implemented soft delete for `User` entity, adding `is_deleted` column to `users` table via Liquibase.

## [Unreleased] - 2026-06-14

### Added
- `Role` entity and `RoleName` enum for RBAC.
- `RoleRepository` interface.
- `CustomUserDetailsService` for Spring Security integration.
- Liquibase changelog `04-create-roles-and-users-roles-tables.yaml` for roles schema.

### Fixed
- Liquibase migration failure "Table already exists" by adding idempotency checks.
- Hibernate `SchemaManagementException` for Role entity enum column.
- Checkstyle violations in imports and line lengths.
- API endpoint paths alignment with security requirements.
