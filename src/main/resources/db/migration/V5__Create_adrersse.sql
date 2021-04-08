CREATE TABLE klage.mottak_adresse
(
    id              UUID PRIMARY KEY,
    mottak_id       UUID NOT NULL,
    adressetype     TEXT NOT NULL,
    adresselinje1   TEXT,
    adresselinje2   TEXT,
    adresselinje3   TEXT,
    postnummer      VARCHAR(4),
    poststed        TEXT,
    land            VARCHAR(2),
    CONSTRAINT fk_mottak_adresse
        FOREIGN KEY (mottak_id)
            REFERENCES klage.mottak (id)
);

CREATE TYPE registreringssted AS ENUM ('FØRSTEINSTANS', 'KLAGEINSTANS');

CREATE TABLE klage.adresse
(
    id                  UUID PRIMARY KEY,
    klagebehandling_id  UUID NOT NULL,
    registrert_av       registreringssted NOT NULL,
    adressetype         TEXT NOT NULL,
    adresselinje1       TEXT,
    adresselinje2       TEXT,
    adresselinje3       TEXT,
    postnummer          VARCHAR(4),
    poststed            TEXT,
    land                VARCHAR(2),
    CONSTRAINT fk_klagebehandling_adresse
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);
