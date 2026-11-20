-- Financeiro, fatia F3 (ADR-0220, desenho do domínio na ADR-0218): o centro de custo por unidade (o geral) e por setor, no padrão do Programa
-- Nacional de Gestão de Custos (produtivo, apoio, administrativo); a apuração mensal por competência, com as linhas por centro e categoria e a
-- produção da unidade, que vira fotografia ao fechar; e o centro de custo na liquidação avulsa. Conferido contra o ddl-auto=validate (ADR-0090).
-- A unidade e o setor não se apagam na plataforma (inativam-se); a cascata existe porque o centro geral nasce para toda unidade ativa e não
-- pode travar a remoção de uma unidade sem histórico (a limpeza dos testes, o cadastro feito por engano).

CREATE TABLE public.tb_centro_custo (
    ativo boolean NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    setor_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    codigo character varying(20) NOT NULL,
    nome character varying(150) NOT NULL,
    registrado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_centro_custo_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['PRODUTIVO'::character varying, 'APOIO'::character varying, 'ADMINISTRATIVO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_centro_custo ADD CONSTRAINT tb_centro_custo_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_centro_custo ADD CONSTRAINT uk_centro_custo_codigo UNIQUE (codigo);
ALTER TABLE ONLY public.tb_centro_custo ADD CONSTRAINT uk_centro_custo_setor UNIQUE (setor_id);
CREATE UNIQUE INDEX uk_centro_custo_geral ON public.tb_centro_custo USING btree (unidade_id) WHERE (setor_id IS NULL);
ALTER TABLE ONLY public.tb_centro_custo ADD CONSTRAINT fk_centro_custo_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid) ON DELETE CASCADE;
ALTER TABLE ONLY public.tb_centro_custo ADD CONSTRAINT fk_centro_custo_setor FOREIGN KEY (setor_id) REFERENCES public.tb_setor(uuid) ON DELETE CASCADE;

CREATE TABLE public.tb_apuracao_custo (
    nao_alocado numeric(15,2) NOT NULL,
    saidas_sem_custo integer NOT NULL,
    apurada_em timestamp(6) with time zone NOT NULL,
    fechada_em timestamp(6) with time zone,
    uuid uuid NOT NULL,
    competencia character varying(7) NOT NULL,
    apurada_por_cpf character varying(255),
    fechada_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    CONSTRAINT tb_apuracao_custo_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ABERTA'::character varying, 'FECHADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_apuracao_custo ADD CONSTRAINT tb_apuracao_custo_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_apuracao_custo ADD CONSTRAINT uk_apuracao_custo_competencia UNIQUE (competencia);

CREATE TABLE public.tb_linha_apuracao_custo (
    valor numeric(15,2) NOT NULL,
    apuracao_id uuid NOT NULL,
    centro_id uuid NOT NULL,
    uuid uuid NOT NULL,
    categoria character varying(255) NOT NULL,
    CONSTRAINT tb_linha_apuracao_custo_categoria_check CHECK (((categoria)::text = ANY ((ARRAY['PESSOAL'::character varying, 'MATERIAL_CONSUMO'::character varying,
        'MEDICAMENTOS'::character varying, 'SERVICOS_TERCEIROS'::character varying, 'MANUTENCAO'::character varying, 'DEPRECIACAO'::character varying,
        'DIARIAS_TFD'::character varying, 'OUTRAS'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_linha_apuracao_custo ADD CONSTRAINT tb_linha_apuracao_custo_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_linha_apuracao_custo ADD CONSTRAINT uk_linha_apuracao_custo UNIQUE (apuracao_id, centro_id, categoria);
ALTER TABLE ONLY public.tb_linha_apuracao_custo ADD CONSTRAINT fk_linha_apuracao_custo_apuracao FOREIGN KEY (apuracao_id) REFERENCES public.tb_apuracao_custo(uuid);
ALTER TABLE ONLY public.tb_linha_apuracao_custo ADD CONSTRAINT fk_linha_apuracao_custo_centro FOREIGN KEY (centro_id) REFERENCES public.tb_centro_custo(uuid) ON DELETE CASCADE;

CREATE TABLE public.tb_producao_apuracao_custo (
    atendimentos integer NOT NULL,
    pacientes_dia integer NOT NULL,
    apuracao_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL
);
ALTER TABLE ONLY public.tb_producao_apuracao_custo ADD CONSTRAINT tb_producao_apuracao_custo_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_producao_apuracao_custo ADD CONSTRAINT uk_producao_apuracao_custo UNIQUE (apuracao_id, unidade_id);
ALTER TABLE ONLY public.tb_producao_apuracao_custo ADD CONSTRAINT fk_producao_apuracao_custo_apuracao FOREIGN KEY (apuracao_id) REFERENCES public.tb_apuracao_custo(uuid);
ALTER TABLE ONLY public.tb_producao_apuracao_custo ADD CONSTRAINT fk_producao_apuracao_custo_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid) ON DELETE CASCADE;

ALTER TABLE public.tb_liquidacao ADD COLUMN centro_custo_id uuid;
ALTER TABLE ONLY public.tb_liquidacao ADD CONSTRAINT fk_liquidacao_centro_custo FOREIGN KEY (centro_custo_id) REFERENCES public.tb_centro_custo(uuid) ON DELETE SET NULL;
