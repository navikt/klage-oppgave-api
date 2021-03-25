CREATE TABLE klage.vedtaksbrev
(
    id       UUID PRIMARY KEY,
    klagebehandling_id UUID,
    brev_mal TEXT

--     CONSTRAINT fk_vedtaksbrev_klagebehandling
--         FOREIGN KEY (klagebehandling_id)
--             REFERENCES klage.klagebehandling (id)
);

CREATE TABLE klage.brevelement
(
    id                 UUID PRIMARY KEY ,
    brev_id            UUID NOT NULL,
    key                VARCHAR NOT NULL,
    display_text       TEXT,
    content            TEXT,
    element_input_type TEXT,

    CONSTRAINT unique_element_brev_id_key UNIQUE (brev_id, key),
    CONSTRAINT fk_brevelement_brev_id FOREIGN KEY (brev_id) REFERENCES klage.vedtaksbrev (id)
);

