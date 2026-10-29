-- Emergências e Desastres, fatia E1 (ADR-0156, desenho na ADR-0113): catálogos de tipo e nível, evento emergencial,
-- emergência (com o histórico de nível e o território afetado) e avaliação de impacto. Conferido contra o
-- ddl-auto=validate (ADR-0090). As permissões nascem pelo catálogo de acesso.

CREATE TABLE public.tb_tipo_emergencia (
    ativo boolean NOT NULL,
    uuid uuid NOT NULL,
    categoria character varying(255) NOT NULL,
    codigo character varying(40) NOT NULL,
    nome character varying(120) NOT NULL,
    cobrade character varying(20),
    CONSTRAINT tb_tipo_emergencia_categoria_check CHECK (((categoria)::text = ANY ((ARRAY['DESASTRE'::character varying, 'SAUDE'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_tipo_emergencia ADD CONSTRAINT tb_tipo_emergencia_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_tipo_emergencia ADD CONSTRAINT uk_tipo_emergencia_codigo UNIQUE (codigo);

CREATE TABLE public.tb_nivel_emergencia (
    ativo boolean NOT NULL,
    ordem integer NOT NULL,
    uuid uuid NOT NULL,
    nome character varying(120) NOT NULL,
    descricao character varying(500)
);
ALTER TABLE ONLY public.tb_nivel_emergencia ADD CONSTRAINT tb_nivel_emergencia_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_nivel_emergencia ADD CONSTRAINT uk_nivel_emergencia_ordem UNIQUE (ordem);

CREATE TABLE public.tb_evento_emergencial (
    ocorrido_em timestamp(6) with time zone NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    surto_id uuid,
    tipo_id uuid NOT NULL,
    uuid uuid NOT NULL,
    fonte character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    local character varying(300),
    descricao character varying(2000) NOT NULL,
    motivo_descarte character varying(500),
    geometria text,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_evento_emergencial_fonte_check CHECK (((fonte)::text = ANY ((ARRAY['VIGILANCIA'::character varying, 'UNIDADE'::character varying, 'DEFESA_CIVIL'::character varying, 'GESTOR'::character varying, 'PROFISSIONAL'::character varying, 'SISTEMA_EXTERNO'::character varying])::text[]))),
    CONSTRAINT tb_evento_emergencial_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['REGISTRADO'::character varying, 'MONITORADO'::character varying, 'DESCARTADO'::character varying, 'CONVERTIDO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_evento_emergencial ADD CONSTRAINT tb_evento_emergencial_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_evento_emergencial ADD CONSTRAINT fk_evento_emergencial_tipo FOREIGN KEY (tipo_id) REFERENCES public.tb_tipo_emergencia(uuid);
ALTER TABLE ONLY public.tb_evento_emergencial ADD CONSTRAINT fk_evento_emergencial_surto FOREIGN KEY (surto_id) REFERENCES public.tb_surto(uuid);
CREATE INDEX ix_evento_emergencial_situacao ON public.tb_evento_emergencial USING btree (situacao, ocorrido_em);

CREATE TABLE public.tb_emergencia (
    decreto_data date,
    espin_data date,
    fim timestamp(6) with time zone,
    inicio timestamp(6) with time zone NOT NULL,
    evento_id uuid NOT NULL,
    nivel_id uuid NOT NULL,
    responsavel_id uuid,
    tipo_id uuid NOT NULL,
    uuid uuid NOT NULL,
    situacao character varying(255) NOT NULL,
    decreto_tipo character varying(255),
    nome character varying(200) NOT NULL,
    decreto_numero character varying(60),
    espin_numero character varying(60),
    motivo_encerramento character varying(1000),
    geometria text,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_emergencia_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ATIVA'::character varying, 'ENCERRADA'::character varying])::text[]))),
    CONSTRAINT tb_emergencia_decreto_tipo_check CHECK (((decreto_tipo)::text = ANY ((ARRAY['SITUACAO_DE_EMERGENCIA'::character varying, 'CALAMIDADE_PUBLICA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_emergencia ADD CONSTRAINT tb_emergencia_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_emergencia ADD CONSTRAINT uk_emergencia_evento UNIQUE (evento_id);
ALTER TABLE ONLY public.tb_emergencia ADD CONSTRAINT fk_emergencia_evento FOREIGN KEY (evento_id) REFERENCES public.tb_evento_emergencial(uuid);
ALTER TABLE ONLY public.tb_emergencia ADD CONSTRAINT fk_emergencia_tipo FOREIGN KEY (tipo_id) REFERENCES public.tb_tipo_emergencia(uuid);
ALTER TABLE ONLY public.tb_emergencia ADD CONSTRAINT fk_emergencia_nivel FOREIGN KEY (nivel_id) REFERENCES public.tb_nivel_emergencia(uuid);
ALTER TABLE ONLY public.tb_emergencia ADD CONSTRAINT fk_emergencia_responsavel FOREIGN KEY (responsavel_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_emergencia_situacao ON public.tb_emergencia USING btree (situacao, inicio);

CREATE TABLE public.tb_emergencia_microarea (
    emergencia_id uuid NOT NULL,
    microarea_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_emergencia_microarea ADD CONSTRAINT tb_emergencia_microarea_pkey PRIMARY KEY (emergencia_id, microarea_id);
ALTER TABLE ONLY public.tb_emergencia_microarea ADD CONSTRAINT fk_emergencia_microarea_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);
ALTER TABLE ONLY public.tb_emergencia_microarea ADD CONSTRAINT fk_emergencia_microarea_microarea FOREIGN KEY (microarea_id) REFERENCES public.tb_microarea(uuid);

CREATE TABLE public.tb_mudanca_nivel_emergencia (
    mudado_em timestamp(6) with time zone NOT NULL,
    emergencia_id uuid NOT NULL,
    nivel_anterior_id uuid,
    nivel_novo_id uuid NOT NULL,
    uuid uuid NOT NULL,
    justificativa character varying(1000) NOT NULL,
    mudado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_mudanca_nivel_emergencia ADD CONSTRAINT tb_mudanca_nivel_emergencia_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_mudanca_nivel_emergencia ADD CONSTRAINT fk_mudanca_nivel_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);
ALTER TABLE ONLY public.tb_mudanca_nivel_emergencia ADD CONSTRAINT fk_mudanca_nivel_anterior FOREIGN KEY (nivel_anterior_id) REFERENCES public.tb_nivel_emergencia(uuid);
ALTER TABLE ONLY public.tb_mudanca_nivel_emergencia ADD CONSTRAINT fk_mudanca_nivel_novo FOREIGN KEY (nivel_novo_id) REFERENCES public.tb_nivel_emergencia(uuid);
CREATE INDEX ix_mudanca_nivel_emergencia ON public.tb_mudanca_nivel_emergencia USING btree (emergencia_id, mudado_em);

CREATE TABLE public.tb_avaliacao_impacto (
    leitos_indisponiveis integer,
    populacao_afetada integer,
    profissionais_indisponiveis integer,
    avaliada_em timestamp(6) with time zone NOT NULL,
    emergencia_id uuid NOT NULL,
    uuid uuid NOT NULL,
    nivel_impacto character varying(255) NOT NULL,
    equipamentos character varying(1000),
    infraestrutura character varying(1000),
    medicamentos_criticos character varying(1000),
    transporte character varying(1000),
    observacao character varying(2000),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_avaliacao_impacto_nivel_impacto_check CHECK (((nivel_impacto)::text = ANY ((ARRAY['BAIXO'::character varying, 'MODERADO'::character varying, 'ALTO'::character varying, 'CRITICO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_avaliacao_impacto ADD CONSTRAINT tb_avaliacao_impacto_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_avaliacao_impacto ADD CONSTRAINT fk_avaliacao_impacto_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);
CREATE INDEX ix_avaliacao_impacto_emergencia ON public.tb_avaliacao_impacto USING btree (emergencia_id, avaliada_em);

CREATE TABLE public.tb_avaliacao_impacto_unidade (
    avaliacao_id uuid NOT NULL,
    unidade_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_avaliacao_impacto_unidade ADD CONSTRAINT tb_avaliacao_impacto_unidade_pkey PRIMARY KEY (avaliacao_id, unidade_id);
ALTER TABLE ONLY public.tb_avaliacao_impacto_unidade ADD CONSTRAINT fk_avaliacao_impacto_unidade_avaliacao FOREIGN KEY (avaliacao_id) REFERENCES public.tb_avaliacao_impacto(uuid);
ALTER TABLE ONLY public.tb_avaliacao_impacto_unidade ADD CONSTRAINT fk_avaliacao_impacto_unidade_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

-- Carga: os desastres mais comuns no município, pela COBRADE, e os tipos próprios da saúde. O município revisa.
INSERT INTO public.tb_tipo_emergencia (uuid, codigo, nome, categoria, cobrade, ativo) VALUES
    (gen_random_uuid(), 'INUNDACAO', 'Inundação', 'DESASTRE', '1.2.1.0.0', true),
    (gen_random_uuid(), 'ENXURRADA', 'Enxurrada', 'DESASTRE', '1.2.2.0.0', true),
    (gen_random_uuid(), 'ALAGAMENTO', 'Alagamento', 'DESASTRE', '1.2.3.0.0', true),
    (gen_random_uuid(), 'DESLIZAMENTO', 'Deslizamento', 'DESASTRE', '1.1.3.2.1', true),
    (gen_random_uuid(), 'ESTIAGEM', 'Estiagem', 'DESASTRE', '1.4.1.1.0', true),
    (gen_random_uuid(), 'INCENDIO_FLORESTAL', 'Incêndio florestal', 'DESASTRE', '1.4.1.3.1', true),
    (gen_random_uuid(), 'PRODUTO_PERIGOSO', 'Acidente com produto perigoso', 'DESASTRE', NULL, true),
    (gen_random_uuid(), 'ACIDENTE_MULTIPLAS_VITIMAS', 'Acidente com múltiplas vítimas', 'SAUDE', NULL, true),
    (gen_random_uuid(), 'EPIDEMIA', 'Epidemia', 'SAUDE', NULL, true),
    (gen_random_uuid(), 'EVENTO_DE_MASSA', 'Evento de massa', 'SAUDE', NULL, true),
    (gen_random_uuid(), 'DESABASTECIMENTO', 'Desabastecimento', 'SAUDE', NULL, true),
    (gen_random_uuid(), 'INTERRUPCAO_DE_UNIDADE', 'Interrupção de unidade de saúde', 'SAUDE', NULL, true);

-- Níveis de resposta de 0 a 3, como no plano de resposta às emergências em saúde pública; nome e descrição editáveis.
INSERT INTO public.tb_nivel_emergencia (uuid, ordem, nome, descricao, ativo) VALUES
    (gen_random_uuid(), 0, 'Nível 0 · Alerta', 'Monitoramento: a rede opera normalmente, com atenção redobrada.', true),
    (gen_random_uuid(), 1, 'Nível 1 · Resposta local', 'O município responde com os recursos próprios.', true),
    (gen_random_uuid(), 2, 'Nível 2 · Resposta ampliada', 'Os recursos do município não bastam: apoio regional ou estadual.', true),
    (gen_random_uuid(), 3, 'Nível 3 · Resposta máxima', 'Ameaça de relevância nacional; pode envolver a ESPIN e a Força Nacional do SUS.', true);
