CREATE TABLE oppgave.versjonident
(
    id                 BIGINT       NOT NULL,
    TYPE               VARCHAR(255) NOT NULL,
    verdi              VARCHAR(255) NOT NULL,
    folkeregisterident VARCHAR(20),
    registrert_dato    DATE,
    CONSTRAINT pk_versjonident PRIMARY KEY (id),

    CONSTRAINT unique_versjonident_verdi_fnr UNIQUE (verdi, folkeregisterident)
);

CREATE SEQUENCE oppgave.versjonident_seq;

ALTER TABLE oppgave.oppgaveversjon
    DROP CONSTRAINT fk_oppgaveversjon_ident_id;
ALTER TABLE oppgave.oppgaveversjon
    ADD CONSTRAINT fk_oppgaveversjon_versjonident_id FOREIGN KEY (ident_id) REFERENCES oppgave.versjonident (id);