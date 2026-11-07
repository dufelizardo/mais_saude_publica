-- Leitos autorizados por capacidade da unidade (ADR-0171). Só inclusão: o registro mais recente vale. Sem registro, não há limite.
-- A permissão LEITO.AUTORIZAR nasce pelo catálogo de acesso. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_limite_leitos (
    quantidade integer NOT NULL,
    registrado_em timestamp(6) without time zone NOT NULL,
    capacidade_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo character varying(500) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_limite_leitos_quantidade_check CHECK (quantidade >= 0)
);
ALTER TABLE ONLY public.tb_limite_leitos ADD CONSTRAINT tb_limite_leitos_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_limite_leitos_unidade ON public.tb_limite_leitos USING btree (unidade_id, capacidade_id, registrado_em);
ALTER TABLE ONLY public.tb_limite_leitos ADD CONSTRAINT fk_limite_leitos_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_limite_leitos ADD CONSTRAINT fk_limite_leitos_capacidade FOREIGN KEY (capacidade_id) REFERENCES public.tb_capacidade_assistencial(uuid);
