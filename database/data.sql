USE australito_db;

-- 1. Parametrización Inicial (Insert)
-- Insertar orígenes de fondos
INSERT INTO CUENTAS (denominacion, tipo) VALUES 
('Banco Santander', 'Tarjeta Débito'), 
('Efectivo Billetera', 'Físico'),
('Mercado Pago', 'Billetera Virtual');

-- Insertar rubros comunes
INSERT INTO CATEGORIAS (nombre, activo) VALUES 
('Supermercado', 1), 
('Transporte', 1),
('Servicios', 1);

-- Simular la respuesta de DolarAPI guardando la tasa en la tabla histórica
INSERT INTO COTIZACION_HISTORICA (valor_venta, valor_compra, fecha_hora) VALUES 
(1350.00, 1330.00, NOW());

-- 2. Transaccionalidad de un Gasto (Insert)
-- El usuario gasta $25000 en el supermercado con Débito.
-- El sistema calcula USD = 25000 / 1350.00 = 18.52 vinculado a la cotización histórica
INSERT INTO GASTOS (monto_ars, monto_usd, fecha_gasto, descripcion, id_categoria, id_cuenta, id_cotizacion) 
VALUES (25000.00, 18.52, '2026-09-18 10:30:00', 'Compra Coto Semanal', 1, 1, 1);

-- Gasto adicional de transporte
INSERT INTO GASTOS (monto_ars, monto_usd, fecha_gasto, descripcion, id_categoria, id_cuenta, id_cotizacion) 
VALUES (5000.00, 3.70, '2026-09-19 08:15:00', 'Carga Tarjeta SUBE', 2, 2, 1);

-- 3. Visualización Consolidada del Histórico (Select / Join)
-- Consulta utilizada por el controlador para armar la grilla del CU-06
SELECT 
    g.id_gasto,
    DATE_FORMAT(g.fecha_gasto, '%d/%m/%Y %H:%i') AS fecha_hora, 
    c.nombre AS rubro, 
    cu.denominacion AS origen_pago, 
    g.descripcion, 
    CONCAT('$ ', FORMAT(g.monto_ars, 2, 'es_AR')) AS total_ars, 
    CONCAT('u$s ', FORMAT(g.monto_usd, 2, 'en_US')) AS total_usd,
    CONCAT('$ ', FORMAT(cot.valor_venta, 2, 'es_AR')) AS tipo_cambio_fijado
FROM GASTOS g
JOIN CATEGORIAS c ON g.id_categoria = c.id_categoria
JOIN CUENTAS cu ON g.id_cuenta = cu.id_cuenta
JOIN COTIZACION_HISTORICA cot ON g.id_cotizacion = cot.id_cotizacion
ORDER BY g.fecha_gasto DESC;

-- 4. Eliminación de un registro equivocado (Delete)
-- Borrado de un gasto mal ingresado asegurando la integridad
DELETE FROM GASTOS WHERE id_gasto = 2;
