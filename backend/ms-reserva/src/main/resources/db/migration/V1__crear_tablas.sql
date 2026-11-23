CREATE TABLE sede (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    ubicacion   VARCHAR(200),
    capacidad   INT          NOT NULL,
    precio      INT          NOT NULL,   -- CLP por evento
    activa      BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
);

-- Fotos de cada sede (@ElementCollection). orden 0 = portada
CREATE TABLE sede_foto (
    sede_id BIGINT       NOT NULL,
    orden   INT          NOT NULL,
    ruta    VARCHAR(200) NOT NULL,
    PRIMARY KEY (sede_id, orden),
    CONSTRAINT fk_sede_foto_sede FOREIGN KEY (sede_id) REFERENCES sede (id)
);

CREATE TABLE reserva (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    usuario_id          BIGINT       NOT NULL,
    usuario_nombre      VARCHAR(100) NOT NULL,
    sede_id             BIGINT       NOT NULL,
    fecha               DATE         NOT NULL,
    hora_inicio         TIME         NOT NULL,
    hora_fin            TIME         NOT NULL,
    motivo              VARCHAR(255) NOT NULL,
    asistentes          INT          NOT NULL,
    estado              VARCHAR(20)  NOT NULL,
    pagada              BOOLEAN      NOT NULL DEFAULT FALSE,
    observacion         VARCHAR(255),
    fecha_creacion      DATETIME(6)  NOT NULL,
    fecha_actualizacion DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_reserva_sede FOREIGN KEY (sede_id) REFERENCES sede (id),
    INDEX idx_reserva_sede_fecha (sede_id, fecha)
);

CREATE TABLE historial_reserva (
    id                     BIGINT      NOT NULL AUTO_INCREMENT,
    reserva_id             BIGINT      NOT NULL,
    estado_anterior        VARCHAR(20),
    estado_nuevo           VARCHAR(20) NOT NULL,
    usuario_responsable_id BIGINT      NOT NULL,
    observacion            VARCHAR(255),
    fecha_cambio           DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_historial_reserva FOREIGN KEY (reserva_id) REFERENCES reserva (id)
);
