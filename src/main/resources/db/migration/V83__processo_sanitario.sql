-- Vigilância em Saúde, fatia V3b (ADR-0200, desenho na ADR-0110): o auto de infração abre o processo administrativo sanitário
-- (Lei 6.437/1977: ciência, defesa em 15 dias, decisão, recurso em 15 dias, encerramento), com as penalidades do art. 2º, as
-- medidas (interdição cautelar de até 90 dias, interdição, apreensão) e os termos em PDF com código de verificação pública.
-- A permissão VISA.JULGAR entra pelo CatalogoDeAcesso. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_processo_sanitario (
    ciencia_auto_em date,
    ciencia_decisao_em date,
    data_decisao date,
    data_julgamento_recurso date,
    data_lavratura date NOT NULL,
    defesa_em date,
    defesa_intempestiva boolean,
    encerrado_em date,
    recurso_em date,
    lavrado_em timestamp(6) with time zone NOT NULL,
    estabelecimento_id uuid NOT NULL,
    inspecao_id uuid,
    uuid uuid NOT NULL,
    numero character varying(20) NOT NULL,
    hash_auto character varying(64),
    fase character varying(255) NOT NULL,
    gravidade character varying(255) NOT NULL,
    forma_ciencia_auto character varying(255),
    forma_ciencia_decisao character varying(255),
    decisao_resultado character varying(255),
    recurso_resultado character varying(255),
    atenuantes character varying(500),
    agravantes character varying(500),
    dispositivo_legal character varying(500) NOT NULL,
    descricao character varying(2000) NOT NULL,
    defesa_texto character varying(4000),
    decisao_fundamentacao character varying(4000),
    recurso_texto character varying(4000),
    recurso_fundamentacao character varying(4000),
    lavrado_por_cpf character varying(255),
    decidido_por_cpf character varying(255),
    recurso_julgado_por_cpf character varying(255),
    CONSTRAINT tb_processo_sanitario_fase_check CHECK (((fase)::text = ANY ((ARRAY['AGUARDANDO_CIENCIA'::character varying, 'EM_DEFESA'::character varying, 'AGUARDANDO_DECISAO'::character varying, 'DECIDIDO'::character varying, 'EM_RECURSO'::character varying, 'ENCERRADO'::character varying])::text[]))),
    CONSTRAINT tb_processo_sanitario_gravidade_check CHECK (((gravidade)::text = ANY ((ARRAY['LEVE'::character varying, 'GRAVE'::character varying, 'GRAVISSIMA'::character varying])::text[]))),
    CONSTRAINT tb_processo_sanitario_forma_ciencia_auto_check CHECK (((forma_ciencia_auto)::text = ANY ((ARRAY['PESSOAL'::character varying, 'POSTAL'::character varying, 'EDITAL'::character varying, 'RECUSA'::character varying])::text[]))),
    CONSTRAINT tb_processo_sanitario_forma_ciencia_decisao_check CHECK (((forma_ciencia_decisao)::text = ANY ((ARRAY['PESSOAL'::character varying, 'POSTAL'::character varying, 'EDITAL'::character varying, 'RECUSA'::character varying])::text[]))),
    CONSTRAINT tb_processo_sanitario_decisao_resultado_check CHECK (((decisao_resultado)::text = ANY ((ARRAY['ARQUIVAMENTO'::character varying, 'PENALIDADE'::character varying])::text[]))),
    CONSTRAINT tb_processo_sanitario_recurso_resultado_check CHECK (((recurso_resultado)::text = ANY ((ARRAY['PROVIDO'::character varying, 'PARCIALMENTE_PROVIDO'::character varying, 'IMPROVIDO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_processo_sanitario ADD CONSTRAINT tb_processo_sanitario_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_processo_sanitario ADD CONSTRAINT uk_processo_sanitario_numero UNIQUE (numero);
ALTER TABLE ONLY public.tb_processo_sanitario ADD CONSTRAINT fk_processo_sanitario_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES public.tb_estabelecimento_regulado(uuid);
ALTER TABLE ONLY public.tb_processo_sanitario ADD CONSTRAINT fk_processo_sanitario_inspecao FOREIGN KEY (inspecao_id) REFERENCES public.tb_inspecao_sanitaria(uuid);

CREATE TABLE public.tb_processo_sanitario_fiscal (
    processo_id uuid NOT NULL,
    profissional_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_processo_sanitario_fiscal ADD CONSTRAINT tb_processo_sanitario_fiscal_pkey PRIMARY KEY (processo_id, profissional_id);
ALTER TABLE ONLY public.tb_processo_sanitario_fiscal ADD CONSTRAINT fk_processo_sanitario_fiscal_processo FOREIGN KEY (processo_id) REFERENCES public.tb_processo_sanitario(uuid);
ALTER TABLE ONLY public.tb_processo_sanitario_fiscal ADD CONSTRAINT fk_processo_sanitario_fiscal_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE public.tb_irregularidade_sanitaria ADD COLUMN processo_id uuid;
ALTER TABLE ONLY public.tb_irregularidade_sanitaria ADD CONSTRAINT fk_irregularidade_sanitaria_processo FOREIGN KEY (processo_id) REFERENCES public.tb_processo_sanitario(uuid);

CREATE TABLE public.tb_penalidade_sanitaria (
    revogada boolean NOT NULL,
    suspensao_ate date,
    valor_multa numeric(14,2),
    processo_id uuid NOT NULL,
    uuid uuid NOT NULL,
    tipo character varying(255) NOT NULL,
    descricao character varying(500),
    CONSTRAINT tb_penalidade_sanitaria_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ADVERTENCIA'::character varying, 'MULTA'::character varying, 'APREENSAO'::character varying, 'INUTILIZACAO'::character varying, 'INTERDICAO_PARCIAL'::character varying, 'INTERDICAO_TOTAL'::character varying, 'SUSPENSAO_VENDAS'::character varying, 'SUSPENSAO_LICENCA'::character varying, 'CANCELAMENTO_LICENCA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_penalidade_sanitaria ADD CONSTRAINT tb_penalidade_sanitaria_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_penalidade_sanitaria ADD CONSTRAINT fk_penalidade_sanitaria_processo FOREIGN KEY (processo_id) REFERENCES public.tb_processo_sanitario(uuid);

CREATE TABLE public.tb_medida_sanitaria (
    encerrada_em date,
    fim_previsto date,
    inicio date NOT NULL,
    total boolean NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    estabelecimento_id uuid NOT NULL,
    inspecao_id uuid,
    processo_id uuid,
    uuid uuid NOT NULL,
    numero character varying(20) NOT NULL,
    lote character varying(60),
    quantidade character varying(60),
    produto character varying(200),
    local_guarda character varying(200),
    tipo character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    descricao character varying(1000) NOT NULL,
    motivo_encerramento character varying(1000),
    registrado_por_cpf character varying(255),
    encerrada_por_cpf character varying(255),
    CONSTRAINT tb_medida_sanitaria_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['INTERDICAO_CAUTELAR'::character varying, 'INTERDICAO'::character varying, 'APREENSAO'::character varying])::text[]))),
    CONSTRAINT tb_medida_sanitaria_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['VIGENTE'::character varying, 'ENCERRADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_medida_sanitaria ADD CONSTRAINT tb_medida_sanitaria_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_medida_sanitaria ADD CONSTRAINT uk_medida_sanitaria_numero UNIQUE (numero);
CREATE INDEX ix_medida_sanitaria_estabelecimento ON public.tb_medida_sanitaria USING btree (estabelecimento_id, situacao);
ALTER TABLE ONLY public.tb_medida_sanitaria ADD CONSTRAINT fk_medida_sanitaria_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES public.tb_estabelecimento_regulado(uuid);
ALTER TABLE ONLY public.tb_medida_sanitaria ADD CONSTRAINT fk_medida_sanitaria_inspecao FOREIGN KEY (inspecao_id) REFERENCES public.tb_inspecao_sanitaria(uuid);
ALTER TABLE ONLY public.tb_medida_sanitaria ADD CONSTRAINT fk_medida_sanitaria_processo FOREIGN KEY (processo_id) REFERENCES public.tb_processo_sanitario(uuid);

CREATE TABLE public.tb_termo_sanitario (
    emitido_em timestamp(6) with time zone NOT NULL,
    documento_id uuid NOT NULL,
    estabelecimento_id uuid NOT NULL,
    medida_id uuid,
    processo_id uuid,
    uuid uuid NOT NULL,
    codigo character varying(14) NOT NULL,
    hash_pdf character varying(64) NOT NULL,
    numero character varying(20) NOT NULL,
    tipo character varying(255) NOT NULL,
    emitido_por_cpf character varying(255),
    CONSTRAINT tb_termo_sanitario_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['AUTO_INFRACAO'::character varying, 'DECISAO'::character varying, 'DECISAO_RECURSO'::character varying, 'INTERDICAO'::character varying, 'APREENSAO'::character varying, 'DESINTERDICAO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_termo_sanitario ADD CONSTRAINT tb_termo_sanitario_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_termo_sanitario ADD CONSTRAINT uk_termo_sanitario_codigo UNIQUE (codigo);
ALTER TABLE ONLY public.tb_termo_sanitario ADD CONSTRAINT fk_termo_sanitario_documento FOREIGN KEY (documento_id) REFERENCES public.tb_documento(uuid);
ALTER TABLE ONLY public.tb_termo_sanitario ADD CONSTRAINT fk_termo_sanitario_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES public.tb_estabelecimento_regulado(uuid);
ALTER TABLE ONLY public.tb_termo_sanitario ADD CONSTRAINT fk_termo_sanitario_medida FOREIGN KEY (medida_id) REFERENCES public.tb_medida_sanitaria(uuid);
ALTER TABLE ONLY public.tb_termo_sanitario ADD CONSTRAINT fk_termo_sanitario_processo FOREIGN KEY (processo_id) REFERENCES public.tb_processo_sanitario(uuid);

-- A licença suspensa por prazo volta sozinha depois da data.
ALTER TABLE public.tb_licenca_sanitaria ADD COLUMN suspensa_ate date;

-- Os termos ficam como documentos do processo ou do estabelecimento (medida sem processo).
ALTER TABLE public.tb_documento DROP CONSTRAINT tb_documento_dono_tipo_check;
ALTER TABLE public.tb_documento ADD CONSTRAINT tb_documento_dono_tipo_check CHECK (((dono_tipo)::text = ANY ((ARRAY['PACIENTE'::character varying, 'ESTABELECIMENTO'::character varying, 'INSPECAO_SANITARIA'::character varying, 'PROCESSO_SANITARIO'::character varying])::text[])));
INSERT INTO public.tb_tipo_documento (uuid, codigo, nome, ativo) VALUES (gen_random_uuid(), 'TERMO_SANITARIO', 'Termo sanitário', true);
