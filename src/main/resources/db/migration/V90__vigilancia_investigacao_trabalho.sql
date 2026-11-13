-- Saúde do Trabalhador, fatia V5b da Vigilância (ADR-0208): a investigação do ambiente de trabalho com o nexo, as medidas com prazo
-- e verificação, a comunicação à vigilância sanitária e o risco ocupacional por empregador. Conferido contra o ddl-auto=validate
-- (ADR-0090).

-- A denúncia sanitária aberta pela própria vigilância (a investigação do trabalho comunica a VISA).
ALTER TABLE public.tb_denuncia_sanitaria DROP CONSTRAINT tb_denuncia_sanitaria_canal_check;
ALTER TABLE public.tb_denuncia_sanitaria ADD CONSTRAINT tb_denuncia_sanitaria_canal_check CHECK (((canal)::text = ANY ((ARRAY['TELEFONE'::character varying, 'PRESENCIAL'::character varying, 'OUVIDORIA'::character varying, 'INTERNET'::character varying, 'OUTRO'::character varying, 'VIGILANCIA'::character varying])::text[])));

-- A investigação do ambiente de trabalho, 1:1 com a notificação que tem o bloco do trabalho.
CREATE TABLE public.tb_investigacao_trabalho (
    uuid uuid NOT NULL,
    notificacao_id uuid NOT NULL,
    investigador_id uuid,
    visita_em date,
    local_visitado character varying(500),
    achados character varying(2000),
    nexo character varying(255) NOT NULL,
    justificativa_nexo character varying(1000),
    nexo_concluido_em timestamp(6) with time zone,
    nexo_concluido_por_cpf character varying(255),
    denuncia_id uuid,
    iniciada_em timestamp(6) with time zone NOT NULL,
    registrado_por_cpf character varying(255),
    atualizado_em timestamp(6) with time zone,
    atualizado_por_cpf character varying(255),
    CONSTRAINT tb_investigacao_trabalho_nexo_check CHECK (((nexo)::text = ANY ((ARRAY['EM_ANALISE'::character varying, 'CONFIRMADO'::character varying, 'DESCARTADO'::character varying, 'INCONCLUSIVO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_investigacao_trabalho ADD CONSTRAINT tb_investigacao_trabalho_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_investigacao_trabalho ADD CONSTRAINT uk_investigacao_trabalho_notificacao UNIQUE (notificacao_id);
ALTER TABLE ONLY public.tb_investigacao_trabalho ADD CONSTRAINT uk_investigacao_trabalho_denuncia UNIQUE (denuncia_id);
ALTER TABLE ONLY public.tb_investigacao_trabalho ADD CONSTRAINT fk_investigacao_trabalho_notificacao FOREIGN KEY (notificacao_id) REFERENCES public.tb_notificacao(uuid);
ALTER TABLE ONLY public.tb_investigacao_trabalho ADD CONSTRAINT fk_investigacao_trabalho_investigador FOREIGN KEY (investigador_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_investigacao_trabalho ADD CONSTRAINT fk_investigacao_trabalho_denuncia FOREIGN KEY (denuncia_id) REFERENCES public.tb_denuncia_sanitaria(uuid);

-- A medida recomendada pela investigação, com prazo e a verificação (cumprida ou não).
CREATE TABLE public.tb_medida_trabalho (
    uuid uuid NOT NULL,
    investigacao_id uuid NOT NULL,
    tipo character varying(255) NOT NULL,
    descricao character varying(1000) NOT NULL,
    prazo date NOT NULL,
    situacao character varying(255) NOT NULL,
    verificada_em date,
    observacao_verificacao character varying(1000),
    verificada_por_cpf character varying(255),
    criada_em timestamp(6) with time zone NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_medida_trabalho_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ADEQUACAO_DO_AMBIENTE'::character varying, 'PROTECAO_COLETIVA'::character varying, 'PROTECAO_INDIVIDUAL'::character varying, 'ORGANIZACAO_DO_TRABALHO'::character varying, 'CAPACITACAO'::character varying, 'AFASTAMENTO_DA_EXPOSICAO'::character varying, 'VIGILANCIA_DOS_EXPOSTOS'::character varying, 'OUTRA'::character varying])::text[]))),
    CONSTRAINT tb_medida_trabalho_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['RECOMENDADA'::character varying, 'CUMPRIDA'::character varying, 'NAO_CUMPRIDA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_medida_trabalho ADD CONSTRAINT tb_medida_trabalho_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_medida_trabalho ADD CONSTRAINT fk_medida_trabalho_investigacao FOREIGN KEY (investigacao_id) REFERENCES public.tb_investigacao_trabalho(uuid);

-- O risco ocupacional por empregador; pode nascer de uma investigação.
CREATE TABLE public.tb_risco_ocupacional (
    uuid uuid NOT NULL,
    tipo character varying(255) NOT NULL,
    agente character varying(200) NOT NULL,
    descricao character varying(1000),
    empregador_documento character varying(14),
    empregador_nome character varying(200) NOT NULL,
    estabelecimento_id uuid,
    local character varying(500),
    trabalhadores_expostos integer,
    situacao character varying(255) NOT NULL,
    controlado_em timestamp(6) with time zone,
    motivo_controle character varying(1000),
    investigacao_id uuid,
    registrado_em timestamp(6) with time zone NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_risco_ocupacional_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['FISICO'::character varying, 'QUIMICO'::character varying, 'BIOLOGICO'::character varying, 'ERGONOMICO'::character varying, 'ACIDENTE'::character varying, 'PSICOSSOCIAL'::character varying])::text[]))),
    CONSTRAINT tb_risco_ocupacional_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ATIVO'::character varying, 'CONTROLADO'::character varying])::text[]))),
    CONSTRAINT ck_risco_ocupacional_expostos CHECK ((trabalhadores_expostos IS NULL) OR (trabalhadores_expostos >= 0))
);
ALTER TABLE ONLY public.tb_risco_ocupacional ADD CONSTRAINT tb_risco_ocupacional_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_risco_ocupacional ADD CONSTRAINT fk_risco_ocupacional_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES public.tb_estabelecimento_regulado(uuid);
ALTER TABLE ONLY public.tb_risco_ocupacional ADD CONSTRAINT fk_risco_ocupacional_investigacao FOREIGN KEY (investigacao_id) REFERENCES public.tb_investigacao_trabalho(uuid);
