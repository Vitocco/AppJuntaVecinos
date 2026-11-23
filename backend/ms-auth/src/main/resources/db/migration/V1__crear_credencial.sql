-- Los usuarios iniciales los crea DataLoader (necesita BCrypt para el password)
CREATE TABLE credencial (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    email          VARCHAR(120) NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    rol            VARCHAR(20)  NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_credencial_email UNIQUE (email)
);
