#!/bin/bash
# Se ejecuta solo la primera vez que se crea el volumen de MySQL.
# Crea una base por microservicio y da permisos al usuario de la app (DB_USER).
mysql -uroot -p"$MYSQL_ROOT_PASSWORD" <<EOSQL
CREATE DATABASE IF NOT EXISTS db_auth    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_usuario CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_reserva CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_finanza CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON db_auth.*    TO '$MYSQL_USER'@'%';
GRANT ALL PRIVILEGES ON db_usuario.* TO '$MYSQL_USER'@'%';
GRANT ALL PRIVILEGES ON db_reserva.* TO '$MYSQL_USER'@'%';
GRANT ALL PRIVILEGES ON db_finanza.* TO '$MYSQL_USER'@'%';
FLUSH PRIVILEGES;
EOSQL
