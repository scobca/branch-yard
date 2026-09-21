#!/usr/bin/env bash
set -euo pipefail

# COMMITS_JSON передаётся через env
COUNT=$(echo "$COMMITS_JSON" | jq length)

COMMIT_LIST=$(echo "$COMMITS_JSON" | \
  jq -r '.[] | "- " + (."message" | split("\n")[0])' | \
  paste -sd '\n' -)

# Пишем в GITHUB_OUTPUT (доступен как переменная окружения)
{
  echo "count=$COUNT"
  echo "commit_list<<EOF"
  echo "$COMMIT_LIST"
  echo "EOF"
} >> "$GITHUB_OUTPUT"