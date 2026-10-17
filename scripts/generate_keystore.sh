#!/usr/bin/env bash
# Makes the upload keystore for Suraksha Saathi, once.
#
#   bash scripts/generate_keystore.sh
#
# It writes release-keystore.jks and keystore.properties in the project root.
# BOTH are git-ignored. Never commit them, never share them, and back them up
# in at least three places. If you lose this file you cannot update the app on
# Play under the same listing.
set -euo pipefail

ALIAS="${1:-bharat-suraksha}"
OUT="release-keystore.jks"

if [ -f "$OUT" ]; then
  echo "release-keystore.jks already exists. Move it away first if you want a new one."
  exit 1
fi

read -r -s -p "Choose a keystore password: " STORE_PW; echo
read -r -s -p "Repeat it: " STORE_PW2; echo
[ "$STORE_PW" = "$STORE_PW2" ] || { echo "Passwords do not match."; exit 1; }
[ ${#STORE_PW} -ge 6 ] || { echo "Use at least 6 characters."; exit 1; }

keytool -genkeypair -v \
  -keystore "$OUT" \
  -alias "$ALIAS" \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -storepass "$STORE_PW" -keypass "$STORE_PW" \
  -dname "CN=Suraksha Saathi, OU=Release, O=Roy Limited, L=Dehradun, ST=Uttarakhand, C=IN"

cat > keystore.properties <<PROPS
storeFile=../$OUT
storePassword=$STORE_PW
keyAlias=$ALIAS
keyPassword=$STORE_PW
PROPS

echo
echo "Created $OUT and keystore.properties."
echo "NOW: copy both to three safe places (a password manager, an offline USB"
echo "drive, and a sealed envelope). Then enrol in Play App Signing."
