PGPASSWORD=secret pg_restore -U myuser -d mydatabase -h localhost -p 32779 --clean --if-exists -v road-survey_120625.dump
