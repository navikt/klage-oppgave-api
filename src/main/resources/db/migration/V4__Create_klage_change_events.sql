CREATE TYPE endringstype AS ENUM ('FEIL', 'VARSEL');

CREATE TABLE klage.endring(
    id                      UUID PRIMARY KEY,
    saksbehandler           TEXT NOT NULL,
    type                    endringstype NOT NULL,
    melding                 TEXT NOT NULL,
    behandling_skygge_id    UUID,
    dato_lest               TIMESTAMP WITH TIME ZONE,
    created                 TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE klage.behandling_skygge(
    id          UUID PRIMARY KEY,
    hjemmel     TEXT NOT NULL,
    frist       DATE,
    tema_id     VARCHAR(3) NOT NULL
);
