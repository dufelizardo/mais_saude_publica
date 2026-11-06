-- Intersetorialidade, fatia IS1 (ADR-0154), e Saúde Mental, fatia SM6 (ADR-0155): o catálogo único de instituições
-- externas, o encaminhamento intersetorial e os indicadores programáticos do programa Saúde Mental.

-- O catálogo de instituições de outros setores (assistência social, educação, garantia de direitos...). O sistema não
-- implementa esses setores: só registra para onde encaminhou e o que voltou.
CREATE TABLE public.tb_instituicao_externa (
    ativa boolean NOT NULL,
    atualizada_em timestamp(6) with time zone NOT NULL,
    uuid uuid NOT NULL,
    setor character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    nome character varying(200) NOT NULL,
    endereco character varying(300),
    telefone character varying(30),
    email character varying(120),
    responsavel character varying(120),
    observacao character varying(500),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_instituicao_externa_setor_check CHECK (((setor)::text = ANY ((ARRAY['ASSISTENCIA_SOCIAL'::character varying, 'EDUCACAO'::character varying, 'GARANTIA_DE_DIREITOS'::character varying, 'TRABALHO_E_RENDA'::character varying, 'HABITACAO'::character varying, 'SEGURANCA_PUBLICA'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_instituicao_externa_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CRAS'::character varying, 'CREAS'::character varying, 'CENTRO_POP'::character varying, 'ACOLHIMENTO_INSTITUCIONAL'::character varying, 'ESCOLA'::character varying, 'CONSELHO_TUTELAR'::character varying, 'DEFENSORIA_PUBLICA'::character varying, 'MINISTERIO_PUBLICO'::character varying, 'JUDICIARIO'::character varying, 'SERVICO_DE_EMPREGO'::character varying, 'HABITACAO'::character varying, 'SEGURANCA_PUBLICA'::character varying, 'OUTRO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_instituicao_externa ADD CONSTRAINT tb_instituicao_externa_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_instituicao_externa ADD CONSTRAINT uk_instituicao_externa_nome UNIQUE (nome);

-- O encaminhamento a uma instituição externa, de qualquer domínio (a origem diz de onde veio); cada domínio aplica as
-- próprias regras de acesso. "Sem retorno" não é guardado: é o prazo vencido sem resposta.
CREATE TABLE public.tb_encaminhamento_intersetorial (
    enviado_em date NOT NULL,
    retorno_esperado_em date NOT NULL,
    recebido_em date,
    retorno_em date,
    registrado_em timestamp(6) with time zone NOT NULL,
    instituicao_id uuid NOT NULL,
    origem_id uuid NOT NULL,
    paciente_id uuid NOT NULL,
    profissional_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    dominio_origem character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    motivo character varying(1000) NOT NULL,
    retorno character varying(2000),
    motivo_cancelamento character varying(500),
    registrado_por_cpf character varying(255),
    retorno_registrado_por_cpf character varying(255),
    CONSTRAINT tb_encaminhamento_intersetorial_dominio_origem_check CHECK (((dominio_origem)::text = ANY ((ARRAY['SAUDE_MENTAL'::character varying])::text[]))),
    CONSTRAINT tb_encaminhamento_intersetorial_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ENVIADO'::character varying, 'RECEBIDO'::character varying, 'CONCLUIDO'::character varying, 'RECUSADO'::character varying, 'CANCELADO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_encaminhamento_intersetorial ADD CONSTRAINT tb_encaminhamento_intersetorial_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_encaminhamento_intersetorial ADD CONSTRAINT fk_encaminhamento_intersetorial_instituicao FOREIGN KEY (instituicao_id) REFERENCES public.tb_instituicao_externa(uuid);
ALTER TABLE ONLY public.tb_encaminhamento_intersetorial ADD CONSTRAINT fk_encaminhamento_intersetorial_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_encaminhamento_intersetorial ADD CONSTRAINT fk_encaminhamento_intersetorial_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_encaminhamento_intersetorial ADD CONSTRAINT fk_encaminhamento_intersetorial_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_encaminhamento_intersetorial_origem ON public.tb_encaminhamento_intersetorial USING btree (dominio_origem, origem_id);
CREATE INDEX ix_encaminhamento_intersetorial_enviado ON public.tb_encaminhamento_intersetorial USING btree (dominio_origem, enviado_em);

-- Os indicadores da Saúde Mental entram no conjunto fixo da P6 (ADR-0141): metas, série e Sala de Situação já valem.
ALTER TABLE public.tb_meta_programatica DROP CONSTRAINT tb_meta_programatica_indicador_check;
ALTER TABLE public.tb_meta_programatica ADD CONSTRAINT tb_meta_programatica_indicador_check CHECK (((indicador)::text = ANY ((ARRAY['ACOMPANHADOS_EM_DIA'::character varying, 'COBERTURA'::character varying, 'ACOES_REALIZADAS'::character varying, 'PA_CONTROLADA'::character varying, 'EXAME_NO_ALVO'::character varying, 'PRE_NATAL_6_CONSULTAS'::character varying, 'TRATAMENTO_ODONTOLOGICO_CONCLUIDO'::character varying, 'PROTESES_ENTREGUES'::character varying, 'ESCOVACAO_SUPERVISIONADA'::character varying, 'PTS_EM_DIA'::character varying, 'AVALIACAO_EM_30_DIAS'::character varying, 'POS_CRISE_EM_7_DIAS'::character varying, 'COMUNICACAO_MP_NO_PRAZO'::character varying, 'RETORNO_INTERSETORIAL'::character varying])::text[])));
