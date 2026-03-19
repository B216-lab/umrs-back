# 2. Use WebFlux

Date: 2025-08-15

## Status

Rejected

## Context

It was unclear how complex the backend operations would be, which made WebFlux seem appealing for its high throughput compared to the Servlet stack.

## Decision

Use Spring MVC. WebFlux is unnecessary for now. What matters initially is development speed, reliability, and scalability prospects.

## Consequences

We may need to rewrite the slowest parts of the code later (WebFlux can be used alongside MVC) if they ever emerge.
