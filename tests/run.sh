#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
classes=$(mktemp -d)
trap 'rm -rf "$classes"' EXIT
javac --release 8 -Xlint:-options -cp libs/gson-2.2.4.jar -d "$classes" src/com/hex/core/*.java tests/com/hex/core/*.java
java -cp "$classes:libs/gson-2.2.4.jar" com.hex.core.WinningChainTest
