create table outbox (
    evento_id   uuid primary key,
    routing_key varchar(50) not null,
    payload     text        not null,
    recibido    timestamptz not null default now(),
    enviado_en  timestamptz
);
