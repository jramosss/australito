# Australito - Sistema Bimonetario de Gestión de Gastos

Sistema de escritorio desarrollado en Java SE y MySQL bajo arquitectura Modelo-Vista-Controlador (MVC).

## Descripción
Australito resuelve la problemática de la pérdida de referencia del poder adquisitivo en la economía bimonetaria argentina, automatizando el registro de gastos personales y calculando de forma instantánea su equivalencia en USD mediante la cotización del Dólar Blue (vía DolarAPI).

## Arquitectura y Componentes
* **Lenguaje:** Java SE 17 LTS
* **Interfaz de Usuario:** Desktop (JavaFX / Swing)
* **Base de Datos:** MySQL Server 8+ conectado mediante JDBC
* **Arquitectura:** Modelo-Vista-Controlador (MVC) y Data Access Object (DAO)
* **Servicio Externo:** API REST de DolarAPI (HTTPS / JSON)
