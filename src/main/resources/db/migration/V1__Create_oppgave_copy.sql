CREATE SCHEMA oppgave;

CREATE TABLE oppgave.oppgave
(
    id                       BIGINT                NOT NULL,
    versjon                  INT                   NOT NULL,
    journalpostid            VARCHAR(40),
    saksreferanse            VARCHAR(40),
    mappe_id                 BIGINT,
    status_id                BIGINT      DEFAULT 1 NOT NULL,
    tildelt_enhetsnr         VARCHAR(40)           NOT NULL,
    opprettet_av_enhetsnr    VARCHAR(40),
    endret_av_enhetsnr       VARCHAR(40),
    tema                     VARCHAR(255),
    temagruppe               VARCHAR(255),
    behandlingstema          VARCHAR(255),
    oppgavetype              VARCHAR(255)          NOT NULL,
    behandlingstype          VARCHAR(255),
    prioritet                VARCHAR(4)            NOT NULL,
    tilordnet_ressurs        VARCHAR(7),
    beskrivelse              TEXT,
    frist_ferdigstillelse    DATE                  NOT NULL,
    aktiv_dato               DATE                  NOT NULL,
    opprettet_av             VARCHAR(40)           NOT NULL,
    endret_av                VARCHAR(40),
    opprettet_tidspunkt      TIMESTAMP             NOT NULL,
    endret_tidspunkt         TIMESTAMP,
    ferdigstilt_tidspunkt    TIMESTAMP,
    behandles_av_applikasjon VARCHAR(40) DEFAULT NULL,
    journalpostkilde         VARCHAR(40) DEFAULT NULL,
    ident_id                 BIGINT      DEFAULT NULL,

    CONSTRAINT pk_oppgave PRIMARY KEY (id)
);

CREATE TABLE oppgave.oppgaveversjon
(
    id                       BIGINT                NOT NULL,
    versjon                  INT                   NOT NULL,
    journalpostid            VARCHAR(40),
    saksreferanse            VARCHAR(40),
    mappe_id                 BIGINT,
    status_id                BIGINT      DEFAULT 1 NOT NULL,
    tildelt_enhetsnr         VARCHAR(40)           NOT NULL,
    opprettet_av_enhetsnr    VARCHAR(40),
    endret_av_enhetsnr       VARCHAR(40),
    tema                     VARCHAR(255),
    temagruppe               VARCHAR(255),
    behandlingstema          VARCHAR(255),
    oppgavetype              VARCHAR(255)          NOT NULL,
    behandlingstype          VARCHAR(255),
    prioritet                VARCHAR(4)            NOT NULL,
    tilordnet_ressurs        VARCHAR(7),
    beskrivelse              TEXT,
    frist_ferdigstillelse    DATE                  NOT NULL,
    aktiv_dato               DATE                  NOT NULL,
    opprettet_av             VARCHAR(40)           NOT NULL,
    endret_av                VARCHAR(40),
    opprettet_tidspunkt      TIMESTAMP             NOT NULL,
    endret_tidspunkt         TIMESTAMP,
    ferdigstilt_tidspunkt    TIMESTAMP,
    behandles_av_applikasjon VARCHAR(40) DEFAULT NULL,
    journalpostkilde         VARCHAR(40) DEFAULT NULL,
    ident_id                 BIGINT      DEFAULT NULL,

    CONSTRAINT pk_oppgaveversjon PRIMARY KEY (id, versjon)
);

CREATE TABLE oppgave.status
(
    id       BIGINT      NOT NULL,
    navn     VARCHAR(40) NOT NULL,
    kategori VARCHAR(40) NOT NULL,

    CONSTRAINT pk_status PRIMARY KEY (id),
    CONSTRAINT unique_status_navn UNIQUE (navn)
);

INSERT INTO oppgave.status (id, navn, kategori)
VALUES (1, 'OPPRETTET', 'AAPEN');
INSERT INTO oppgave.status (id, navn, kategori)
VALUES (2, 'AAPNET', 'AAPEN');
INSERT INTO oppgave.status (id, navn, kategori)
VALUES (3, 'UNDER_BEHANDLING', 'AAPEN');
INSERT INTO oppgave.status (id, navn, kategori)
VALUES (4, 'FERDIGSTILT', 'AVSLUTTET');
INSERT INTO oppgave.status (id, navn, kategori)
VALUES (5, 'FEILREGISTRERT', 'AVSLUTTET');

ALTER TABLE oppgave.oppgave
    ADD CONSTRAINT fk_oppgave_status_id FOREIGN KEY (status_id) REFERENCES oppgave.status (id);
ALTER TABLE oppgave.oppgaveversjon
    ADD CONSTRAINT fk_oppgaveversjon_status_id FOREIGN KEY (status_id) REFERENCES oppgave.status (id);


CREATE TABLE oppgave.metadata
(
    id         BIGINT       NOT NULL,
    oppgave_id BIGINT       NOT NULL,
    nokkel     VARCHAR(40)  NOT NULL,
    verdi      VARCHAR(255) NOT NULL,

    CONSTRAINT pk_metadata PRIMARY KEY (id),
    CONSTRAINT fk_metadata_oppgave_id FOREIGN KEY (oppgave_id) REFERENCES oppgave.oppgave (id),
    CONSTRAINT unique_nokkel_per_oppgave UNIQUE (oppgave_id, nokkel)
);

CREATE SEQUENCE oppgave.metadata_seq;

CREATE TABLE oppgave.versjonmetadata
(
    id              BIGINT       NOT NULL,
    oppgave_id      BIGINT       NOT NULL,
    oppgave_versjon INT          NOT NULL,
    nokkel          VARCHAR(40)  NOT NULL,
    verdi           VARCHAR(255) NOT NULL,

    CONSTRAINT pk_versjonmetadata PRIMARY KEY (id),
    CONSTRAINT fk_versjonmetadata_oppgave_id FOREIGN KEY (oppgave_id, oppgave_versjon) REFERENCES oppgave.oppgaveversjon (id, versjon),
    CONSTRAINT unique_nokkel_per_oppgaveversjon UNIQUE (oppgave_id, oppgave_versjon, nokkel)
);

CREATE SEQUENCE oppgave.versjonmetadata_seq;

CREATE TABLE oppgave.ident
(
    id                 BIGINT       NOT NULL,
    TYPE               VARCHAR(255) NOT NULL,
    verdi              VARCHAR(255) NOT NULL,
    folkeregisterident VARCHAR(20),
    registrert_dato    DATE,
    CONSTRAINT pk_ident PRIMARY KEY (id),

    CONSTRAINT unique_ident_verdi_fnr UNIQUE (verdi, folkeregisterident)
);

CREATE SEQUENCE oppgave.ident_seq;

ALTER TABLE oppgave.oppgave
    ADD CONSTRAINT fk_oppgave_ident_id FOREIGN KEY (ident_id) REFERENCES oppgave.ident (id);
ALTER TABLE oppgave.oppgaveversjon
    ADD CONSTRAINT fk_oppgaveversjon_ident_id FOREIGN KEY (ident_id) REFERENCES oppgave.ident (id);

-- Oppgave indexer for FK --
CREATE INDEX idx_oppgave_ident_id
    ON oppgave.oppgave (ident_id);

CREATE INDEX idx_oppgave_status_id
    ON oppgave.oppgave (status_id);

CREATE INDEX idx_oppgave_mappe_id
    ON oppgave.oppgave (mappe_id);

-- Metadata indexer for FK
CREATE INDEX idx_metadata_oppgave_id
    ON oppgave.metadata (oppgave_id);

-- Andre indexer for oppgave
CREATE INDEX idx_oppgave_tildelt_enhetsnr
    ON oppgave.oppgave (tildelt_enhetsnr);

CREATE INDEX idx_oppgave_tilordnet_ressurs
    ON oppgave.oppgave (tilordnet_ressurs);

CREATE INDEX idx_oppgave_behandlingstema
    ON oppgave.oppgave (behandlingstema);

CREATE INDEX idx_oppgave_behandlingstype
    ON oppgave.oppgave (behandlingstype);

CREATE INDEX idx_oppgave_tema
    ON oppgave.oppgave (tema);

CREATE INDEX idx_oppgave_oppgavetype
    ON oppgave.oppgave (oppgavetype);

CREATE INDEX idx_oppgave_aktiv_dato
    ON oppgave.oppgave (aktiv_dato);

CREATE INDEX idx_oppgave_frist_dato
    ON oppgave.oppgave (frist_ferdigstillelse);

CREATE INDEX idx_oppgave_journalpost_id
    ON oppgave.oppgave (journalpostid);

CREATE INDEX idx_oppgave_oppr_tidspunkt
    ON oppgave.oppgave (opprettet_tidspunkt);

CREATE INDEX idx_oppgave_opprettet_av
    ON oppgave.oppgave (opprettet_av);

-- Indexer for oppgaveversjon

CREATE INDEX idx_oppgaveversj_ident_id
    ON oppgave.oppgaveversjon (ident_id);

CREATE INDEX idx_oppgaveversj_status_id
    ON oppgave.oppgaveversjon (status_id);

CREATE INDEX idx_oppgaveversj_mappe_id
    ON oppgave.oppgaveversjon (mappe_id);

CREATE INDEX idx_versjonmetadata_oppgave_id
    ON oppgave.versjonmetadata (oppgave_id, oppgave_versjon);

CREATE INDEX idx_oppgaveversj_tildelt_enhetsnr
    ON oppgave.oppgaveversjon (tildelt_enhetsnr);

CREATE INDEX idx_oppgaveversj_tilordnet_ressurs
    ON oppgave.oppgaveversjon (tilordnet_ressurs);

CREATE INDEX idx_oppgaveversj_behandlingstema
    ON oppgave.oppgaveversjon (behandlingstema);

CREATE INDEX idx_oppgaveversj_behandlingstype
    ON oppgave.oppgaveversjon (behandlingstype);

CREATE INDEX idx_oppgaveversj_tema
    ON oppgave.oppgaveversjon (tema);

CREATE INDEX idx_oppgaveversj_oppgavetype
    ON oppgave.oppgaveversjon (oppgavetype);

CREATE INDEX idx_oppgaveversj_aktiv_dato
    ON oppgave.oppgaveversjon (aktiv_dato);

CREATE INDEX idx_oppgaveversj_frist_dato
    ON oppgave.oppgaveversjon (frist_ferdigstillelse);

CREATE INDEX idx_oppgaveversj_journalpost_id
    ON oppgave.oppgaveversjon (journalpostid);

CREATE INDEX idx_oppgaveversj_oppr_tidspunkt
    ON oppgave.oppgaveversjon (opprettet_tidspunkt);

CREATE INDEX idx_oppgaveversj_opprettet_av
    ON oppgave.oppgaveversjon (opprettet_av);