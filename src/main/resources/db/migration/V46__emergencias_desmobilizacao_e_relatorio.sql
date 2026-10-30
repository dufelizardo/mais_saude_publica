-- Emergências e Desastres, fatia E5 (ADR-0159, desenho na ADR-0113): a emergência em desmobilização, o registro da
-- desmobilização (o que foi liberado) e o relatório pós-evento, com as recomendações e a revisão do plano que elas
-- geraram. Conferido contra o ddl-auto=validate (ADR-0090).

ALTER TABLE public.tb_emergencia DROP CONSTRAINT tb_emergencia_situacao_check;
ALTER TABLE public.tb_emergencia ADD CONSTRAINT tb_emergencia_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ATIVA'::character varying, 'EM_DESMOBILIZACAO'::character varying, 'ENCERRADA'::character varying])::text[])));

CREATE TABLE public.tb_desmobilizacao (
    capacidades_desativadas integer NOT NULL,
    mobilizacoes_concluidas integer NOT NULL,
    planos_desativados integer NOT NULL,
    profissionais_liberados integer NOT NULL,
    iniciada_em timestamp(6) with time zone NOT NULL,
    emergencia_id uuid NOT NULL,
    uuid uuid NOT NULL,
    responsavel character varying(200) NOT NULL,
    justificativa character varying(1000) NOT NULL,
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_desmobilizacao ADD CONSTRAINT tb_desmobilizacao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_desmobilizacao ADD CONSTRAINT uk_desmobilizacao_emergencia UNIQUE (emergencia_id);
ALTER TABLE ONLY public.tb_desmobilizacao ADD CONSTRAINT fk_desmobilizacao_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);

CREATE TABLE public.tb_relatorio_pos_evento (
    atualizado_em timestamp(6) with time zone NOT NULL,
    criado_em timestamp(6) with time zone NOT NULL,
    finalizado_em timestamp(6) with time zone,
    emergencia_id uuid NOT NULL,
    uuid uuid NOT NULL,
    situacao character varying(255) NOT NULL,
    resumo character varying(4000),
    problemas character varying(4000),
    licoes character varying(4000),
    finalizado_por_cpf character varying(255),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_relatorio_pos_evento_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['RASCUNHO'::character varying, 'FINALIZADO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_relatorio_pos_evento ADD CONSTRAINT tb_relatorio_pos_evento_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_relatorio_pos_evento ADD CONSTRAINT uk_relatorio_pos_evento_emergencia UNIQUE (emergencia_id);
ALTER TABLE ONLY public.tb_relatorio_pos_evento ADD CONSTRAINT fk_relatorio_pos_evento_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);

CREATE TABLE public.tb_recomendacao_relatorio (
    ordem integer NOT NULL,
    plano_revisao_id uuid,
    relatorio_id uuid NOT NULL,
    uuid uuid NOT NULL,
    plano_codigo character varying(40),
    descricao character varying(1000) NOT NULL
);
ALTER TABLE ONLY public.tb_recomendacao_relatorio ADD CONSTRAINT tb_recomendacao_relatorio_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_recomendacao_relatorio ADD CONSTRAINT fk_recomendacao_relatorio_relatorio FOREIGN KEY (relatorio_id) REFERENCES public.tb_relatorio_pos_evento(uuid);
ALTER TABLE ONLY public.tb_recomendacao_relatorio ADD CONSTRAINT fk_recomendacao_relatorio_plano FOREIGN KEY (plano_revisao_id) REFERENCES public.tb_plano_contingencia(uuid);
