# syntax=docker/dockerfile:1.4
FROM mcr.microsoft.com/devcontainers/java:1-21-bullseye

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        postgresql-client \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

RUN mkdir -p /workspace/logs

