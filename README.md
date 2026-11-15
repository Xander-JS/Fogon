# POS multi-sede para restaurantes · Esqueleto funcional

Arquitectura de Software · Proyecto integrador · UCC Pasto

Arquitectura **orientada a eventos con nodo local por sede**, sincronización asíncrona hacia la nube
y núcleo **hexagonal** (puertos y adaptadores) en `pos-sede`.

| Servicio | Rol | Driver que materializa |
|---|---|---|
| `pos-sede` | API de pedidos. Publica `pedido.confirmado` y `pedido.anulado` en RabbitMQ | Auditabilidad, modificabilidad |
| `cocina` | Consumidor: muestra las comandas | Disponibilidad |
| `sincronizador` | Consumidor: guarda en su outbox y envía a la nube cuando hay conexión | Disponibilidad |
| `facturacion` | Consumidor: emite la factura a través del puerto `FacturadorElectronico` | Modificabilidad |
| `central-nube` | Recibe eventos de las sedes (idempotente) y genera reportes | (se degrada sin red) |

Cada servicio tiene su propia base de datos. Ningún servicio comparte clases con otro: el contrato es el JSON del evento.

## Requisitos

Solo **Docker Desktop**. No hace falta tener Java ni Maven instalados.

## Levantar todo (un comando)

```bash
docker compose up --build
```

La primera vez tarda varios minutos porque descarga las dependencias de Maven.

- API de la sede: http://localhost:8180/pedidos
- Nube: http://localhost:8181/eventos y http://localhost:8181/reportes/ventas
- Consola de RabbitMQ: http://localhost:15673 (usuario `pos`, clave `pos`)

Las peticiones de prueba están en `pruebas.http` (se ejecutan directo desde IntelliJ o VS Code con la extensión REST Client).

## Guion de la demo

En una terminal aparte, dejen los logs a la vista:

```bash
docker compose logs -f cocina sincronizador facturacion central-nube
```

**1. Flujo normal.** Creen un pedido (`pruebas.http`, paso 1). Cocina muestra la comanda, facturación emite el CUFE,
el sincronizador lo envía y la nube lo registra.

**2. Se cae la conexión con la nube.**

```bash
docker compose stop central-nube
```

Creen tres pedidos más. Cocina y facturación siguen funcionando. El sincronizador muestra "Sin conexión con la nube… en espera".

**3. Vuelve la conexión.**

```bash
docker compose start central-nube
```

En pocos segundos el sincronizador envía los pendientes, en orden. Consulten `/reportes/ventas`: están todos y ninguno duplicado.

**4. Auditoría.** Anulen un pedido (paso 3) y revisen `/auditoria` y `/auditoria/verificar`.
Intenten borrar un pedido o alterar la auditoría directamente en la base de datos:

```bash
docker compose exec db-sede psql -U pos -d sede -c "delete from pedidos;"
docker compose exec db-sede psql -U pos -d sede -c "update auditoria set detalle = 'nada';"
```

La base de datos lo rechaza: las tablas son inmutables.

**5. Aislamiento de fallas: proveedor de facturación lento.**

```bash
FACTURACION_DEMORA_MS=15000 docker compose up -d facturacion
```

(En PowerShell: `$env:FACTURACION_DEMORA_MS=15000; docker compose up -d facturacion`)

Creen un pedido: la API responde de inmediato y cocina recibe la comanda. Solo la factura se demora.

## Apagar

```bash
docker compose down        # conserva los datos
docker compose down -v     # borra también los datos
```

## Cómo integrar este código en los proyectos generados con Spring Initializr

En cada una de las 5 carpetas:

1. **Conserven su `pom.xml`**.
2. **Borren `src/main` y `src/test`** del proyecto generado y copien el `src` de esta entrega.
3. Copien el `Dockerfile`.

Si dejan la clase `*Application` que generó Initializr, habrá dos clases `main` y Maven no podrá empaquetar.
