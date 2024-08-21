#!/bin/sh

set -e

TEMP="$(mktemp -d)"
trap 'cd /; rm -rf "$TEMP"' EXIT
cd "$TEMP"
cat >file.tex
xelatex file.tex
cat file.pdf
