#!/bin/bash
set -eu

# =========================
# Extract project version
# =========================

VERSION_FILE="VERSION"

if [ ! -f "$VERSION_FILE" ]; then
  echo "Error:: file '$VERSION_FILE' not found!"
  exit 1
fi

VERSION=$(cat "$VERSION_FILE" | tr -d '[:space:]')

if [ -z "$VERSION" ]; then
  echo "Error:: version is empty.!"
  exit 1
fi

TAG_NAME="v$VERSION"

if git rev-parse "$TAG_NAME" >/dev/null 2>&1; then
  echo "Tag '$TAG_NAME' already exists."
  exit 0
fi


# =========================
# Create & publish new tag
# =========================

ORIGINAL_NAME=$(git config --local user.name || echo "")
ORIGINAL_EMAIL=$(git config --local user.email || echo "")

git config --local user.name "github-actions"
git config --local user.email "github-actions@users.noreply.github.com"

git tag -a "$TAG_NAME" -m "Release $VERSION"
git push origin "$TAG_NAME"

if [ -n "$ORIGINAL_NAME" ]; then
  git config --local user.name "$ORIGINAL_NAME"
else
  git config --local --unset user.name
fi

if [ -n "$ORIGINAL_EMAIL" ]; then
  git config --local user.email "$ORIGINAL_EMAIL"
else
  git config --local --unset user.email
fi