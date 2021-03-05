CREATE TYPE endringstype AS ENUM ('FEIL', 'VARSEL');

CREATE TABLE klage.endring(
    id                  UUID PRIMARY KEY,
    saksbehandler       TEXT NOT NULL,
    type                endringstype NOT NULL,
    melding             TEXT NOT NULL,
    dato_lest           TIMESTAMP WITH TIME ZONE,
    created             TIMESTAMP WITH TIME ZONE NOT NULL
);
