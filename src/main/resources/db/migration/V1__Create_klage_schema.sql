DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'cloudsqliamuser')
        THEN
            GRANT USAGE ON SCHEMA public TO cloudsqliamuser;
            GRANT USAGE ON SCHEMA klage TO cloudsqliamuser;
            GRANT SELECT ON ALL TABLES IN SCHEMA public TO cloudsqliamuser;
            GRANT SELECT ON ALL TABLES IN SCHEMA klage TO cloudsqliamuser;
            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO cloudsqliamuser;
            ALTER DEFAULT PRIVILEGES IN SCHEMA klage GRANT SELECT ON TABLES TO cloudsqliamuser;
        END IF;
    END
$$;

CREATE TABLE klage.mottak
(
    id                             UUID PRIMARY KEY,
    versjon                        BIGINT                   NOT NULL,
    tema_id                        TEXT                     NOT NULL,
    type_id                        TEXT                     NOT NULL,
    klager_type                    TEXT,
    klager_value                   TEXT,
    klager_prosessfullmektig_type  TEXT,
    klager_prosessfullmektig_value TEXT,
    klager_skal_motta_kopi         BOOLEAN,
    saken_gjelder_type             TEXT,
    saken_gjelder_value            TEXT,
    saken_gjelder_skal_motta_kopi  BOOLEAN,
    sak_fagsystem                  TEXT,
    sak_fagsak_id                  TEXT,
    kilde_referanse                TEXT                     NOT NULL,
    dvh_referanse                  TEXT,
    innsyn_url                     TEXT,
    avsender_saksbehandlerident    TEXT,
    avsender_enhet                 TEXT,
    dato_innsendt                  DATE,
    dato_mottatt_foersteinstans    DATE,
    dato_oversendt_klageinstans    TIMESTAMP WITH TIME ZONE NOT NULL,
    dato_frist_fra_foersteinstans  DATE,
    kildesystem                    TEXT                     NOT NULL,
    kommentar                      TEXT,
    created                        TIMESTAMP WITH TIME ZONE NOT NULL,
    modified                       TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE klage.mottak_dokument
(
    id             UUID PRIMARY KEY,
    mottak_id      UUID NOT NULL,
    type           TEXT NOT NULL, -- Må bli begrenset i kode til "BRUKERS_KLAGE", "OPPRINNELIG_VEDTAK", "OVERSENDELSESBREV", "ANNET"
    journalpost_id TEXT NOT NULL,
    CONSTRAINT fk_dokument_mottak
        FOREIGN KEY (mottak_id)
            REFERENCES klage.mottak (id)
);

CREATE TABLE klage.mottak_hjemmel
(
    id        UUID PRIMARY KEY,
    mottak_id UUID NOT NULL,
    lov       TEXT NOT NULL, -- Enum i koden
    kapittel  INTEGER,
    paragraf  INTEGER,
    CONSTRAINT fk_hjemmel_mottak
        FOREIGN KEY (mottak_id)
            REFERENCES klage.mottak (id)
);

CREATE TABLE klage.kvalitetsvurdering
(
    id                          UUID PRIMARY KEY,
    eoes_id                     TEXT,
    raadfoert_med_lege_id       TEXT,
    intern_vurdering            TEXT,
    send_tilbakemelding         BOOLEAN,
    tilbakemelding              TEXT,
    mottaker_saksbehandlerident TEXT,
    mottaker_enhet              TEXT,
    created                     TIMESTAMP WITH TIME ZONE NOT NULL,
    modified                    TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE klage.klagebehandling
(
    id                                         UUID PRIMARY KEY,
    versjon                                    BIGINT                   NOT NULL,
    klager_type                                TEXT,
    klager_value                               TEXT,
    klager_prosessfullmektig_type              TEXT,
    klager_prosessfullmektig_value             TEXT,
    klager_skal_motta_kopi                     BOOLEAN,
    saken_gjelder_type                         TEXT,
    saken_gjelder_value                        TEXT,
    saken_gjelder_skal_motta_kopi              BOOLEAN,
    tema_id                                    TEXT                     NOT NULL,
    type_id                                    TEXT                     NOT NULL,
    referanse_id                               TEXT,
    sak_fagsystem                              TEXT,
    sak_fagsak_id                              TEXT,
    dato_innsendt                              DATE,
    dato_mottatt_foersteinstans                DATE,
    dato_mottatt_klageinstans                  TIMESTAMP WITH TIME ZONE NOT NULL,
    dato_behandling_tildelt                    TIMESTAMP WITH TIME ZONE,
    dato_behandling_avsluttet                  TIMESTAMP WITH TIME ZONE,
    dato_behandling_avsluttet_av_saksbehandler TIMESTAMP WITH TIME ZONE,
    frist                                      DATE,
    tildelt_saksbehandlerident                 TEXT,
    medunderskriverident                       TEXT,
    tildelt_enhet                              TEXT,
    avsender_enhet_foersteinstans              TEXT,
    avsender_saksbehandlerident_foersteinstans TEXT,
    mottak_id                                  UUID                     NOT NULL,
    kvalitetsvurdering_id                      UUID,
    kildesystem                                TEXT                     NOT NULL,
    kommentar_fra_foersteinstans               TEXT,
    created                                    TIMESTAMP WITH TIME ZONE NOT NULL,
    modified                                   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_behandling_kvalitetsvurdering
        FOREIGN KEY (kvalitetsvurdering_id)
            REFERENCES klage.kvalitetsvurdering (id),
    CONSTRAINT fk_behandling_mottak
        FOREIGN KEY (mottak_id)
            REFERENCES klage.mottak (id)
);

CREATE TABLE klage.vedtak
(
    id                  UUID PRIMARY KEY,
    utfall_id           TEXT,
    grunn_id            TEXT,
    klagebehandling_id  UUID                     NOT NULL,
    journalpost_id      TEXT,
    modified            TIMESTAMP WITH TIME ZONE NOT NULL,
    created             TIMESTAMP WITH TIME ZONE NOT NULL,
    ferdigstilt_i_joark TIMESTAMP WITH TIME ZONE,
    ferdig_distribuert  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_vedtak_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);

CREATE TABLE klage.vedtaksadresse
(
    id            UUID PRIMARY KEY,
    vedtak_id     UUID NOT NULL,
    adressetype   TEXT NOT NULL, -- Må begrenses i kode til 'NORSK', 'UTENLANDSK'
    adresselinje1 TEXT,
    adresselinje2 TEXT,
    adresselinje3 TEXT,
    postnummer    TEXT,
    poststed      TEXT,
    land          TEXT
);

CREATE TABLE klage.brevmottaker
(
    id                UUID PRIMARY KEY,
    vedtak_id         UUID NOT NULL,
    mottaker_type     TEXT,
    mottaker_value    TEXT,
    rolle_id          TEXT,
    journalpost_id    TEXT,
    dokdist_referanse UUID,
    CONSTRAINT fk_brevmottaker_vedtak
        FOREIGN KEY (vedtak_id)
            REFERENCES klage.vedtak (id)
);

CREATE TABLE klage.klagebehandling_hjemmel
(
    id                 TEXT NOT NULL,
    klagebehandling_id UUID NOT NULL,
    PRIMARY KEY (id, klagebehandling_id),
    CONSTRAINT fk_hjemmel_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);

CREATE TABLE klage.saksdokument
(
    id                 UUID PRIMARY KEY,
    klagebehandling_id UUID NOT NULL,
    journalpost_id     TEXT,
    dokument_info_id   TEXT,
    CONSTRAINT fk_saksdokument_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);

CREATE TABLE klage.vedtak_hjemmel
(
    id        TEXT NOT NULL,
    vedtak_id UUID NOT NULL,
    PRIMARY KEY (id, vedtak_id),
    CONSTRAINT fk_hjemmel_vedtak
        FOREIGN KEY (vedtak_id)
            REFERENCES klage.vedtak (id)
);

CREATE TABLE klage.endringslogginnslag
(
    id                 UUID PRIMARY KEY,
    klagebehandling_id UUID                     NOT NULL,
    saksbehandlerident TEXT,
    kilde              TEXT                     NOT NULL,
    handling           TEXT                     NOT NULL,
    felt               TEXT                     NOT NULL,
    fraverdi           TEXT,
    tilverdi           TEXT,
    tidspunkt          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_endringslogginnslag_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);

CREATE TABLE klage.vedtaksbrev
(
    id                 UUID PRIMARY KEY,
    klagebehandling_id UUID,
    brev_mal           TEXT,

    CONSTRAINT fk_vedtaksbrev_klagebehandling
        FOREIGN KEY (klagebehandling_id)
            REFERENCES klage.klagebehandling (id)
);

CREATE TABLE klage.brevelement
(
    id                 UUID PRIMARY KEY,
    brev_id            UUID    NOT NULL,
    key                VARCHAR NOT NULL,
    display_text       TEXT,
    content            TEXT,
    element_input_type TEXT,

    CONSTRAINT unique_element_brev_id_key UNIQUE (brev_id, key),
    CONSTRAINT fk_brevelement_brev_id FOREIGN KEY (brev_id) REFERENCES klage.vedtaksbrev (id)
);

CREATE TABLE klage.brevutsending
(
    id      UUID PRIMARY KEY,
    status  TEXT NOT NULL DEFAULT 'IKKE_SENDT',
    melding TEXT
);

CREATE TABLE klage.kafka_vedtak_event
(
    id                    UUID PRIMARY KEY,
    kilde_referanse       TEXT NOT NULL,
    kilde                 TEXT NOT NULL,
    utfall_id             TEXT NOT NULL,
    vedtaksbrev_referanse TEXT,
    kabal_referanse       TEXT NOT NULL,
    status_id             TEXT NOT NULL DEFAULT '1',
    melding               TEXT
);
