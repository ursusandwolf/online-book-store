# Code review and maintenance

Reviewed the REST controllers, business services, security, persistence, migrations,
container build and verification workflow.

## Findings addressed

| Priority | Finding | Fix |
| --- | --- | --- |
| High | Database password and JWT signing key had public defaults | Require external credentials; keep test credentials in test resources |
| High | JWTs without expiration could throw; deleted users and malformed tokens could become server errors | Validate required claims, reject inactive users and return HTTP 401 |
| High | Authentication failures were not mapped to HTTP 401 | Return a generic credential error without internal details |
| High | Concurrent cart writes could lose quantities; checkout could duplicate an order | Lock the owning user row for cart writes and checkout; add MySQL concurrency tests |
| High | The role migration inserted two values into the same column | Use separate INSERTs; accept the exact previous Liquibase checksum |
| High | The historical administrator was seeded with a fixed password hash | Disable only the unchanged legacy account through a new migration; document explicit admin provisioning |
| High | Docker copied a plain JAR without Spring Boot dependencies | Add Spring Boot repackaging; check archive layout in CI |
| Medium | Hibernate recreated the test schema, hiding migration/mapping errors | Validate Liquibase's schema; explicitly map the order status to VARCHAR |
| Medium | A collection fetch graph made order pagination happen in memory | Remove collection fetching from the paginated repository method |
| Medium | Large pagination offsets narrowed to negative integers | Clamp the long offset before casting; support unpaged requests |
| Medium | Adding quantities could overflow into a negative integer | Compute in a long and reject overflow before changing the item |
| Medium | Books could reference missing or deleted categories | Resolve managed, active categories in the service; validate category IDs |
| Medium | Decimal prices could be rounded or exceed the database column | Validate DECIMAL(12,2) precision before persistence |
| Medium | Missing category browsing/deletion and book deletion contradicted documented 404 responses | Check existence and return the domain not-found error |
| Medium | Database conflicts could expose server errors | Return a generic HTTP 409 response |
| Medium | Docker healthcheck used the wrong variable and published an unused debug port | Expand the password inside the container; remove debug mapping; bind MySQL to loopback |
| Low | Build style checks fetched a mutable remote configuration | Use the repository's Checkstyle file and copy it into the build image |
| Low | Documentation lacked a working launch path and configuration details | Rewrite README with actual routes, commands, admin setup and project scope |

## Verification

Run `mvn verify` with Docker available for the full suite. Tests include real MySQL migrations,
registration → login → JWT-authenticated cart, concurrent checkout, concurrent cart additions,
service behavior, price/category validation, token rejection and large pagination offsets.

For checks without Docker, use the subset documented in [README](../README.md#testing).
GitHub CI runs the full suite and checks executable Spring Boot packaging.

## Current boundaries

Order-item pagination sorts and slices an individual order in memory. Order history uses
database pagination; mapping lazy order items can still issue additional queries.
The status endpoint accepts all supported enum values without a transition policy.
The local Compose configuration does not provide HTTPS or database TLS.
Payments, inventory and refresh tokens are not implemented.

The corrected role seed explicitly accepts its previous version-9 checksum, rather than
disabling checksum validation globally. Existing databases skip already applied seeds.
The legacy administrator migration preserves accounts whose original password hash was changed.
