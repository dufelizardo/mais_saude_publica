#!/usr/bin/env bash
# Runs this suite with all Robot Framework reports written to its own results/ folder,
# instead of wherever the command happens to be invoked from.
here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
robot --outputdir "$here/results" "$@" "$here/test"
