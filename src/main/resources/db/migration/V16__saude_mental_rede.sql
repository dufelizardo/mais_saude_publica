-- Saúde Mental, fatia SM4 (ADR-0122): o caso na rede de atenção. Ligação do caso com a solicitação da Regulação,
-- cuidado compartilhado como histórico, transferência do cuidado em duas etapas, o caso anterior no caso aberto por
-- transferência e a equipe matriciada na intervenção de apoio matricial. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_encaminhamento_saude_mental (
    vinculado_em timestamp(6) with time zone NOT NULL,
    acompanhamento_id uuid NOT NULL,
    solicitacao_id uuid NOT NULL,
    uuid uuid NOT NULL,
    registrado_por_cpf character varying(255)
);

CREATE TABLE public.tb_compartilhamento_cuidado (
    fim date,
    inicio date NOT NULL,
    acompanhamento_id uuid NOT NULL,
    equipe_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo_fim character varying(500),
    objetivo character varying(500) NOT NULL,
    registrado_por_cpf character varying(255)
);

CREATE TABLE public.tb_transferencia_cuidado (
    decidida_em timestamp(6) with time zone,
    proposta_em timestamp(6) with time zone NOT NULL,
    acompanhamento_id uuid NOT NULL,
    caso_destino_id uuid,
    unidade_destino_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo_decisao character varying(500),
    resumo character varying(2000) NOT NULL,
    decidida_por_cpf character varying(255),
    proposta_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    CONSTRAINT tb_transferencia_cuidado_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['PROPOSTA'::character varying, 'ACEITA'::character varying, 'RECUSADA'::character varying, 'CANCELADA'::character varying])::text[])))
);

ALTER TABLE public.tb_acompanhamento_saude_mental ADD COLUMN caso_anterior_id uuid;
ALTER TABLE public.tb_intervencao_saude_mental ADD COLUMN equipe_matriciada_id uuid;

ALTER TABLE ONLY public.tb_encaminhamento_saude_mental ADD CONSTRAINT tb_encaminhamento_saude_mental_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_encaminhamento_saude_mental ADD CONSTRAINT uk_encaminhamento_sm_solicitacao UNIQUE (acompanhamento_id, solicitacao_id);
ALTER TABLE ONLY public.tb_compartilhamento_cuidado ADD CONSTRAINT tb_compartilhamento_cuidado_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_transferencia_cuidado ADD CONSTRAINT tb_transferencia_cuidado_pkey PRIMARY KEY (uuid);

CREATE INDEX ix_compartilhamento_cuidado_acomp ON public.tb_compartilhamento_cuidado USING btree (acompanhamento_id, fim);
CREATE INDEX ix_transferencia_cuidado_destino ON public.tb_transferencia_cuidado USING btree (unidade_destino_id, situacao);

ALTER TABLE ONLY public.tb_encaminhamento_saude_mental
    ADD CONSTRAINT fk_encaminhamento_sm_acomp FOREIGN KEY (acompanhamento_id) REFERENCES public.tb_acompanhamento_saude_mental(uuid);
ALTER TABLE ONLY public.tb_encaminhamento_saude_mental
    ADD CONSTRAINT fk_encaminhamento_sm_solicitacao FOREIGN KEY (solicitacao_id) REFERENCES public.tb_solicitacao_regulacao(uuid);
ALTER TABLE ONLY public.tb_compartilhamento_cuidado
    ADD CONSTRAINT fk_compartilhamento_cuidado_acomp FOREIGN KEY (acompanhamento_id) REFERENCES public.tb_acompanhamento_saude_mental(uuid);
ALTER TABLE ONLY public.tb_compartilhamento_cuidado
    ADD CONSTRAINT fk_compartilhamento_cuidado_equipe FOREIGN KEY (equipe_id) REFERENCES public.tb_equipe(uuid);
ALTER TABLE ONLY public.tb_transferencia_cuidado
    ADD CONSTRAINT fk_transferencia_cuidado_acomp FOREIGN KEY (acompanhamento_id) REFERENCES public.tb_acompanhamento_saude_mental(uuid);
ALTER TABLE ONLY public.tb_transferencia_cuidado
    ADD CONSTRAINT fk_transferencia_cuidado_destino FOREIGN KEY (unidade_destino_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_transferencia_cuidado
    ADD CONSTRAINT fk_transferencia_cuidado_caso_destino FOREIGN KEY (caso_destino_id) REFERENCES public.tb_acompanhamento_saude_mental(uuid);
ALTER TABLE ONLY public.tb_acompanhamento_saude_mental
    ADD CONSTRAINT fk_acomp_saude_mental_anterior FOREIGN KEY (caso_anterior_id) REFERENCES public.tb_acompanhamento_saude_mental(uuid);
ALTER TABLE ONLY public.tb_intervencao_saude_mental
    ADD CONSTRAINT fk_intervencao_sm_equipe_matriciada FOREIGN KEY (equipe_matriciada_id) REFERENCES public.tb_equipe(uuid);

-- Discussão de caso em apoio matricial (ADR-0103): a intervenção pede a equipe matriciada.
INSERT INTO public.tb_tipo_intervencao_saude_mental (uuid, codigo, nome, ordem, ativo) VALUES
    (gen_random_uuid(), 'MATRICIAMENTO', 'Discussão de caso / apoio matricial', 9, true);
