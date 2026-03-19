# 3. Use Direct Connection Between Frontend and DB

Date: 2025-09-12

## Status

Rejected

## Context

The idea was to connect the frontend directly to the database via a connection string to speed up development and avoid writing REST endpoints for every small, repetitive task. Yes, it went that far—but if others use it, why not try it here first?

## Decision

Do not use a direct connection between the frontend and the database via "database security rules" or similar, due to CVE-2024-45489 and the growing maintenance burden of these rules as the DB schema grows (see https://youtu.be/2zcN2aQsUdc?si=80NfsuOA2KI770CH).

## Consequences

Build the backend fully from the start, investing somewhat more time upfront.
