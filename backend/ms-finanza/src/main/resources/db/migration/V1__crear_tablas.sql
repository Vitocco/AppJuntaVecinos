CREATE TABLE categoria_movimiento (
    id     BIGINT      NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(60) NOT NULL,
    tipo   VARCHAR(10) NOT NULL,   -- INGRESO | GASTO
    activa BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
);

CREATE TABLE movimiento_financiero (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    tipo                VARCHAR(10)  NOT NULL,
    categoria_id        BIGINT       NOT NULL,
    reserva_id          BIGINT,
    descripcion         VARCHAR(255) NOT NULL,
    monto               INT          NOT NULL,   -- siempre > 0 (CLP)
    fecha_movimiento    DATE         NOT NULL,
    usuario_registro_id BIGINT       NOT NULL,
    estado              VARCHAR(10)  NOT NULL,   -- VIGENTE | ANULADO
    fecha_creacion      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_movimiento_categoria FOREIGN KEY (categoria_id) REFERENCES categoria_movimiento (id),
    CONSTRAINT ck_movimiento_monto CHECK (monto > 0),
    INDEX idx_movimiento_fecha (fecha_movimiento)
);

CREATE TABLE pago (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    reserva_id     BIGINT       NOT NULL,
    movimiento_id  BIGINT,
    monto          INT          NOT NULL,
    fecha_pago     DATE,
    medio          VARCHAR(20)  NOT NULL,   -- WEBPAY | TRANSFERENCIA | EFECTIVO | OTRO
    referencia     VARCHAR(100),
    estado         VARCHAR(20)  NOT NULL,   -- PENDIENTE | PAGADO | RECHAZADO
    webpay_token   VARCHAR(100),
    fecha_creacion DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_pago_movimiento FOREIGN KEY (movimiento_id) REFERENCES movimiento_financiero (id),
    INDEX idx_pago_reserva (reserva_id),
    INDEX idx_pago_token (webpay_token)
);

CREATE TABLE comprobante (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    movimiento_id   BIGINT       NOT NULL,
    pago_id         BIGINT,
    nombre_archivo  VARCHAR(255) NOT NULL,
    tipo_archivo    VARCHAR(50)  NOT NULL,
    ruta_referencia VARCHAR(255) NOT NULL,
    fecha_adjunto   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comprobante_movimiento FOREIGN KEY (movimiento_id) REFERENCES movimiento_financiero (id),
    CONSTRAINT fk_comprobante_pago FOREIGN KEY (pago_id) REFERENCES pago (id)
);

CREATE TABLE informe_financiero (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    titulo               VARCHAR(150) NOT NULL,
    periodo_inicio       DATE         NOT NULL,
    periodo_fin          DATE         NOT NULL,
    observacion          VARCHAR(500),
    publicado            BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_generacion     DATETIME(6)  NOT NULL,
    usuario_generador_id BIGINT       NOT NULL,
    PRIMARY KEY (id)
);
