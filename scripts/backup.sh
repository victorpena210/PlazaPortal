#!/usr/bin/env bash
# Run with a MySQL client installed. Uses environment variables, never command-line passwords.
set -euo pipefail
umask 077
: "${BACKUP_DB_HOST:?Set BACKUP_DB_HOST}"
: "${BACKUP_DB_NAME:?Set BACKUP_DB_NAME}"
: "${DB_USERNAME:?Set DB_USERNAME}"
: "${DB_PASSWORD:?Set DB_PASSWORD}"
backup_destination="${BACKUP_DIR:-./backups}/$(date -u +%Y%m%dT%H%M%SZ)"
backup_options="$(mktemp)"
trap 'rm -f "$backup_options"' EXIT
mkdir -p "$backup_destination"
python3 - "$backup_options" <<'PY'
import os,sys
def quote(value):
    return '"'+value.replace('\\','\\\\').replace('"','\\"').replace('\n','\\n').replace('\r','\\r')+'"'
with open(sys.argv[1],'w') as out:
    out.write('[client]\n')
    for key,value in [('host',os.environ['BACKUP_DB_HOST']),('port',os.environ.get('BACKUP_DB_PORT','3306')),('user',os.environ['DB_USERNAME']),('password',os.environ['DB_PASSWORD']),('ssl-mode',os.environ.get('BACKUP_SSL_MODE','REQUIRED'))]:
        out.write(key+'='+quote(value)+'\n')
PY
mysqldump --defaults-extra-file="$backup_options" --single-transaction \
  --routines --triggers --no-tablespaces --set-gtid-purged=OFF \
  "$BACKUP_DB_NAME" > "$backup_destination/database.sql"
if [ -d "${UPLOAD_DIR:-./data/uploads}" ]; then
  tar -czf "$backup_destination/uploads.tar.gz" -C "${UPLOAD_DIR:-./data/uploads}" .
fi
printf 'Backup created: %s\n' "$backup_destination"
