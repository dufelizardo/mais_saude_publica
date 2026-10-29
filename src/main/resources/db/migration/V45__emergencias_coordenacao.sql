-- Emergências e Desastres, fatia E3 (ADR-0158, desenho na ADR-0113): ações emergenciais, mobilização (com a exceção da
-- Escala para o profissional mobilizado), capacidade emergencial das unidades e a ativação dela na emergência.
-- Conferido contra o ddl-auto=validate (ADR-0090). A permissão EMERGENCIA.COORDENAR nasce pelo catálogo de acesso.

CREATE TABLE public.tb_acao_emergencial (
    concluida_em timestamp(6) with time zone,
    criada_em timestamp(6) with time zone NOT NULL,
    iniciada_em timestamp(6) with time zone,
    prazo timestamp(6) with time zone,
    acao_plano_id uuid,
    ativacao_id uuid,
    emergencia_id uuid NOT NULL,
    unidade_id uuid,
    uuid uuid NOT NULL,
    prioridade character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    responsavel character varying(200) NOT NULL,
    motivo_cancelamento character varying(500),
    descricao character varying(1000) NOT NULL,
    observacao character varying(1000),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_acao_emergencial_prioridade_check CHECK (((prioridade)::text = ANY ((ARRAY['BAIXA'::character varying, 'MEDIA'::character varying, 'ALTA'::character varying, 'URGENTE'::character varying])::text[]))),
    CONSTRAINT tb_acao_emergencial_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['PENDENTE'::character varying, 'EM_EXECUCAO'::character varying, 'CONCLUIDA'::character varying, 'CANCELADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_acao_emergencial ADD CONSTRAINT tb_acao_emergencial_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_acao_emergencial ADD CONSTRAINT fk_acao_emergencial_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);
ALTER TABLE ONLY public.tb_acao_emergencial ADD CONSTRAINT fk_acao_emergencial_acao_plano FOREIGN KEY (acao_plano_id) REFERENCES public.tb_acao_plano(uuid);
ALTER TABLE ONLY public.tb_acao_emergencial ADD CONSTRAINT fk_acao_emergencial_ativacao FOREIGN KEY (ativacao_id) REFERENCES public.tb_ativacao_plano(uuid);
ALTER TABLE ONLY public.tb_acao_emergencial ADD CONSTRAINT fk_acao_emergencial_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
CREATE INDEX ix_acao_emergencial_emergencia ON public.tb_acao_emergencial USING btree (emergencia_id, situacao);

CREATE TABLE public.tb_capacidade_emergencial (
    ativa boolean NOT NULL,
    emergencial integer NOT NULL,
    normal integer NOT NULL,
    atualizada_em timestamp(6) with time zone NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    recurso character varying(255) NOT NULL,
    observacao character varying(500),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_capacidade_emergencial_recurso_check CHECK (((recurso)::text = ANY ((ARRAY['ATENDIMENTOS_DIA'::character varying, 'LEITOS'::character varying, 'CONSULTORIOS'::character varying, 'OUTRO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_capacidade_emergencial ADD CONSTRAINT tb_capacidade_emergencial_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_capacidade_emergencial ADD CONSTRAINT uk_capacidade_emergencial_recurso UNIQUE (unidade_id, recurso);
ALTER TABLE ONLY public.tb_capacidade_emergencial ADD CONSTRAINT fk_capacidade_emergencial_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_ativacao_capacidade (
    quantidade integer NOT NULL,
    fim timestamp(6) with time zone,
    inicio timestamp(6) with time zone NOT NULL,
    capacidade_id uuid NOT NULL,
    emergencia_id uuid NOT NULL,
    uuid uuid NOT NULL,
    situacao character varying(255) NOT NULL,
    justificativa_fim character varying(500),
    justificativa character varying(1000) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_ativacao_capacidade_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ATIVA'::character varying, 'DESATIVADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_ativacao_capacidade ADD CONSTRAINT tb_ativacao_capacidade_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_ativacao_capacidade ADD CONSTRAINT fk_ativacao_capacidade_capacidade FOREIGN KEY (capacidade_id) REFERENCES public.tb_capacidade_emergencial(uuid);
ALTER TABLE ONLY public.tb_ativacao_capacidade ADD CONSTRAINT fk_ativacao_capacidade_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);
CREATE INDEX ix_ativacao_capacidade_capacidade ON public.tb_ativacao_capacidade USING btree (capacidade_id, situacao);

CREATE TABLE public.tb_mobilizacao (
    fim date,
    inicio date NOT NULL,
    quantidade integer,
    registrada_em timestamp(6) with time zone NOT NULL,
    capacidade_ativacao_id uuid,
    destino_id uuid NOT NULL,
    emergencia_id uuid NOT NULL,
    origem_id uuid,
    profissional_id uuid,
    transferencia_id uuid,
    uuid uuid NOT NULL,
    situacao character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    responsavel character varying(200),
    recurso character varying(300) NOT NULL,
    motivo_cancelamento character varying(500),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_mobilizacao_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['SOLICITADA'::character varying, 'EM_ANDAMENTO'::character varying, 'CONCLUIDA'::character varying, 'CANCELADA'::character varying])::text[]))),
    CONSTRAINT tb_mobilizacao_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['PROFISSIONAL'::character varying, 'MEDICAMENTO'::character varying, 'INSUMO'::character varying, 'EQUIPAMENTO'::character varying, 'VEICULO'::character varying, 'LEITO'::character varying, 'ESPACO_FISICO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_mobilizacao ADD CONSTRAINT tb_mobilizacao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_mobilizacao ADD CONSTRAINT fk_mobilizacao_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);
ALTER TABLE ONLY public.tb_mobilizacao ADD CONSTRAINT fk_mobilizacao_origem FOREIGN KEY (origem_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_mobilizacao ADD CONSTRAINT fk_mobilizacao_destino FOREIGN KEY (destino_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_mobilizacao ADD CONSTRAINT fk_mobilizacao_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_mobilizacao ADD CONSTRAINT fk_mobilizacao_transferencia FOREIGN KEY (transferencia_id) REFERENCES public.tb_transferencia_farmacia(uuid);
ALTER TABLE ONLY public.tb_mobilizacao ADD CONSTRAINT fk_mobilizacao_capacidade FOREIGN KEY (capacidade_ativacao_id) REFERENCES public.tb_ativacao_capacidade(uuid);
CREATE INDEX ix_mobilizacao_emergencia ON public.tb_mobilizacao USING btree (emergencia_id, tipo);
CREATE INDEX ix_mobilizacao_profissional ON public.tb_mobilizacao USING btree (profissional_id, destino_id);

-- O turno designado pela exceção da mobilização (o profissional sem lotação na unidade de destino).
ALTER TABLE public.tb_turno_escala ADD COLUMN mobilizacao_id uuid;
ALTER TABLE ONLY public.tb_turno_escala ADD CONSTRAINT fk_turno_escala_mobilizacao FOREIGN KEY (mobilizacao_id) REFERENCES public.tb_mobilizacao(uuid);
