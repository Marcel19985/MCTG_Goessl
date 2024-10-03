Als Datenbank wird PostgreSQL als Docker verwendet. Um Datenbank zu starten:
1. Docker Desktop öffnen
Im Terminal (egal welches Verzeichnis):
2. docker start mctg-postgres
3. Überprüfen, ob Docker Container läuft:
docker ps im Terminal

Um mit Datenbank zu verbinden, um Änderungen zu machen (PostgreSQL Interaktivkonsole):
docker exec -it mctg-postgres psql -U user -d mctg_db
Um SQL Konsole zu beenden: \q

SQL Script ausführen:
docker exec -i mctg-postgres psql -U user -d mctg_db < create_db.sql
Test, ob Script ausgeführt wurde: SQL Konsole starten:
docker exec -it mctg-postgres psql -U user -d mctg_db
Z.B. Tabelle users ansehen:
\d users