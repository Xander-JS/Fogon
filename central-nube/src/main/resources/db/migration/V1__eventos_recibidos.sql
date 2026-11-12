create table eventos_recibidos (
    evento_id   uuid primary key,
    tipo        varchar(20)    not null,
    pedido_id   uuid           not null,
    sede        varchar(50)    not null,
    mesa        varchar(20),
    total       numeric(12, 2) not null,
    usuario     varchar(100),
    motivo      text,
    momento     timestamptz    not null,
    recibido_en timestamptz    not null default now()
);
