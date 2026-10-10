-- Creación de la base de datos para Australito
CREATE DATABASE IF NOT EXISTS australito_db;
USE australito_db;

-- Tabla de Cuentas (Orígenes de fondos)
CREATE TABLE CUENTAS (
    id_cuenta INT AUTO_INCREMENT PRIMARY KEY,
    denominacion VARCHAR(50) NOT NULL,
    tipo VARCHAR(30) NOT NULL
) ENGINE=InnoDB;

-- Tabla de Categorías (Rubros de gasto)
CREATE TABLE CATEGORIAS (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    activo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

-- Tabla de Cotizaciones Históricas (Snapshot y registro de tasas en fecha exacta)
CREATE TABLE COTIZACION_HISTORICA (
    id_cotizacion INT AUTO_INCREMENT PRIMARY KEY,
    valor_venta DECIMAL(10,2) NOT NULL,
    valor_compra DECIMAL(10,2) NOT NULL,
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Tabla Transaccional Principal
CREATE TABLE GASTOS (
    id_gasto INT AUTO_INCREMENT PRIMARY KEY,
    monto_ars DECIMAL(12,2) NOT NULL,
    monto_usd DECIMAL(12,2) NOT NULL,
    fecha_gasto DATETIME DEFAULT CURRENT_TIMESTAMP,
    descripcion VARCHAR(255),
    id_categoria INT NOT NULL,
    id_cuenta INT NOT NULL,
    id_cotizacion INT NOT NULL,
    CONSTRAINT fk_gasto_categoria FOREIGN KEY (id_categoria) 
        REFERENCES CATEGORIAS(id_categoria) 
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_gasto_cuenta FOREIGN KEY (id_cuenta) 
        REFERENCES CUENTAS(id_cuenta) 
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_gasto_cotizacion FOREIGN KEY (id_cotizacion) 
        REFERENCES COTIZACION_HISTORICA(id_cotizacion) 
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;
