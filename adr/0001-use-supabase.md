# 1. Use Supabase

Date: 2025-08-12

## Status

Rejected

## Context

Before starting the backend project, there was a proposal to simply use backend-as-a-service by self-hosting Supabase.

## Decision

- Supabase is fauxpen source to some extent (varies by use case).
- In the issue trackers and repositories of Supabase, one can find many complaints about its open source community policy.
- Supabase is updated regularly, without much attention to documenting the self-hosted setup. Its infrastructure is not the simplest, and maintaining, configuring, or troubleshooting errors that arise for various reasons is a complex task—feasible only for experienced specialists or Supabase developers (subjectively).
- Many unwelcome limitations in the self-hosted version. The most critical is the prohibition of creating more than one project per Supabase instance, but beyond that, there are plenty of other inconveniences.

## Consequences

This entire project
