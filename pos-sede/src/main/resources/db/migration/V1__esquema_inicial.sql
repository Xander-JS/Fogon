create table pedidos (
    id               uuid primary key,
    sede             varchar(50)    not null,
    mesa             varchar(20)    not null,
    mesero           varchar(100)   not null,
    descripcion      text           not null,
    total            numeric(12, 2) not null,
    estado           varchar(20)    not null,
    creado           timestamptz    not null,
    anulado_por      varchar(100),
    motivo_anulacion text
);

create table auditoria (
    id            bigserial primary key,
    momento       timestamptz  not null,
    usuario       varchar(100) not null,
    accion        varchar(50)  not null,
    detalle       text         not null,
    hash_anterior varchar(64)  not null,
    hash          varchar(64)  not null
);

-- Inmutabilidad a nivel de base de datos: nadie puede borrar pedidos ni tocar la auditoría.
create function bloquear_modificacion() returns trigger as $$
begin
    raise exception 'La tabla % es inmutable: % no está permitido', TG_TABLE_NAME, TG_OP;
end;
$$ language plpgsql;

create trigger auditoria_inmutable before update or delete on auditoria
    for each row execute function bloquear_modificacion();

create trigger pedidos_sin_borrado before delete on pedidos
    for each row execute function bloquear_modificacion();
