CREATE TABLE usuario (
    id               BIGINT       NOT NULL,   -- mismo id que la credencial en ms-auth
    nombre           VARCHAR(100) NOT NULL,
    email            VARCHAR(120) NOT NULL,
    telefono         VARCHAR(20),
    numero_casa      VARCHAR(20),
    fecha_nacimiento DATE,
    recibir_avisos   BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_creacion   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email)
);
