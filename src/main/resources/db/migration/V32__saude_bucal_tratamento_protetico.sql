-- Saúde Bucal, fatia SB4b (ADR-0139): o tratamento protético e as etapas, a capacidade de laboratório regional de
-- prótese dentária (LRPD) no Modelo Operacional e a origem prótese no odontograma. Conferido contra o
-- ddl-auto=validate (ADR-0090).

INSERT INTO public.tb_capacidade_assistencial (uuid, codigo, nome, descricao, ativo, perfil_operacional_id)
SELECT gen_random_uuid(), 'LRPD', 'Laboratório regional de prótese dentária (LRPD)', 'Confecção de próteses dentárias para a rede.', true, p.uuid
FROM public.tb_perfil_operacional p
WHERE p.codigo = 'ATENCAO_ESPECIALIZADA' AND NOT EXISTS (SELECT 1 FROM public.tb_capacidade_assistencial c WHERE c.codigo = 'LRPD');

CREATE TABLE public.tb_tratamento_protetico (
    data_entrega date,
    dente integer,
    aberto_em timestamp(6) with time zone NOT NULL,
    encerrado_em timestamp(6) with time zone,
    atendimento_id uuid NOT NULL,
    laboratorio_unidade_id uuid,
    paciente_id uuid NOT NULL,
    plano_id uuid,
    profissional_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    laboratorio_externo character varying(200),
    motivo_cancelamento character varying(500),
    observacao character varying(1000),
    aberto_por_cpf character varying(255),
    arcada character varying(255) NOT NULL,
    encerrado_por_cpf character varying(255),
    etapa_atual character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_tratamento_protetico_arcada_check CHECK (((arcada)::text = ANY ((ARRAY['SUPERIOR'::character varying, 'INFERIOR'::character varying])::text[]))),
    CONSTRAINT tb_tratamento_protetico_etapa_atual_check CHECK (((etapa_atual)::text = ANY ((ARRAY['PLANEJAMENTO'::character varying, 'MOLDAGEM'::character varying, 'PROVA'::character varying, 'LABORATORIO'::character varying, 'ENTREGA'::character varying, 'ACOMPANHAMENTO'::character varying])::text[]))),
    CONSTRAINT tb_tratamento_protetico_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['EM_ANDAMENTO'::character varying, 'CONCLUIDO'::character varying, 'CANCELADO'::character varying])::text[]))),
    CONSTRAINT tb_tratamento_protetico_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['TOTAL'::character varying, 'PARCIAL_REMOVIVEL'::character varying, 'UNITARIA'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_tratamento_protetico ADD CONSTRAINT tb_tratamento_protetico_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_tratamento_protetico_paciente ON public.tb_tratamento_protetico USING btree (paciente_id);
ALTER TABLE ONLY public.tb_tratamento_protetico ADD CONSTRAINT fk_tratamento_protetico_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_tratamento_protetico ADD CONSTRAINT fk_tratamento_protetico_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_tratamento_protetico ADD CONSTRAINT fk_tratamento_protetico_atendimento FOREIGN KEY (atendimento_id) REFERENCES public.tb_atendimento(uuid);
ALTER TABLE ONLY public.tb_tratamento_protetico ADD CONSTRAINT fk_tratamento_protetico_plano FOREIGN KEY (plano_id) REFERENCES public.tb_plano_tratamento(uuid);
ALTER TABLE ONLY public.tb_tratamento_protetico ADD CONSTRAINT fk_tratamento_protetico_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_tratamento_protetico ADD CONSTRAINT fk_tratamento_protetico_laboratorio FOREIGN KEY (laboratorio_unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_etapa_tratamento_protetico (
    data date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    profissional_id uuid NOT NULL,
    tratamento_id uuid NOT NULL,
    uuid uuid NOT NULL,
    observacao character varying(500),
    etapa character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_etapa_tratamento_protetico_etapa_check CHECK (((etapa)::text = ANY ((ARRAY['PLANEJAMENTO'::character varying, 'MOLDAGEM'::character varying, 'PROVA'::character varying, 'LABORATORIO'::character varying, 'ENTREGA'::character varying, 'ACOMPANHAMENTO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_etapa_tratamento_protetico ADD CONSTRAINT tb_etapa_tratamento_protetico_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_etapa_protetica_tratamento ON public.tb_etapa_tratamento_protetico USING btree (tratamento_id);
ALTER TABLE ONLY public.tb_etapa_tratamento_protetico ADD CONSTRAINT fk_etapa_protetica_tratamento FOREIGN KEY (tratamento_id) REFERENCES public.tb_tratamento_protetico(uuid);
ALTER TABLE ONLY public.tb_etapa_tratamento_protetico ADD CONSTRAINT fk_etapa_protetica_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

-- A origem prótese no odontograma (a unitária entregue).
ALTER TABLE public.tb_registro_odontograma ADD COLUMN tratamento_protetico_id uuid;
ALTER TABLE ONLY public.tb_registro_odontograma ADD CONSTRAINT fk_registro_odontograma_protese FOREIGN KEY (tratamento_protetico_id) REFERENCES public.tb_tratamento_protetico(uuid);
