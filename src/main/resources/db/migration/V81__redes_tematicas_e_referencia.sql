-- Gestão da Rede, fatia GR2 (ADR-0197): os componentes das redes temáticas ligados aos perfis e às capacidades do modelo operacional,
-- a referência estrutural (para onde a origem encaminha cada procedimento regulado, com vigência) e a marca de fora da referência na
-- autorização da Regulação. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_componente_rede_item (
    capacidade_id uuid,
    perfil_id uuid,
    uuid uuid NOT NULL,
    componente character varying(40) NOT NULL,
    CONSTRAINT tb_componente_rede_item_alvo_check CHECK ((perfil_id IS NULL) <> (capacidade_id IS NULL)),
    CONSTRAINT tb_componente_rede_item_componente_check CHECK (componente IN ('RUE_ATENCAO_BASICA', 'RUE_SAMU', 'RUE_SALA_ESTABILIZACAO', 'RUE_UPA_24H',
        'RUE_HOSPITALAR', 'RUE_ATENCAO_DOMICILIAR', 'RC_PRE_NATAL', 'RC_PARTO_NASCIMENTO', 'RC_PUERPERIO_CRIANCA', 'RC_SISTEMA_LOGISTICO',
        'PCD_ATENCAO_BASICA', 'PCD_REABILITACAO', 'PCD_HOSPITALAR_URGENCIA', 'DC_ATENCAO_BASICA', 'DC_ESPECIALIZADA', 'DC_SISTEMAS_APOIO',
        'DC_SISTEMAS_LOGISTICOS'))
);
ALTER TABLE ONLY public.tb_componente_rede_item ADD CONSTRAINT tb_componente_rede_item_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_componente_rede_item ADD CONSTRAINT uk_componente_rede_perfil UNIQUE (componente, perfil_id);
ALTER TABLE ONLY public.tb_componente_rede_item ADD CONSTRAINT uk_componente_rede_capacidade UNIQUE (componente, capacidade_id);
ALTER TABLE ONLY public.tb_componente_rede_item ADD CONSTRAINT fk_componente_rede_perfil FOREIGN KEY (perfil_id) REFERENCES public.tb_perfil_operacional(uuid);
ALTER TABLE ONLY public.tb_componente_rede_item ADD CONSTRAINT fk_componente_rede_capacidade FOREIGN KEY (capacidade_id) REFERENCES public.tb_capacidade_assistencial(uuid);

-- A carga: cada componente com os códigos do modelo operacional que o compõem; o código pode ser perfil, capacidade ou os dois.
CREATE TEMPORARY TABLE carga_componente_rede (componente character varying(40), codigo character varying(60));
INSERT INTO carga_componente_rede (componente, codigo) VALUES
    ('RUE_ATENCAO_BASICA', 'ATENCAO_PRIMARIA'), ('RUE_ATENCAO_BASICA', 'DEMANDA_ESPONTANEA'),
    ('RUE_SAMU', 'ATENDIMENTO_PRE_HOSPITALAR'), ('RUE_SAMU', 'ATENCAO_MOVEL'), ('RUE_SAMU', 'REGULACAO_MEDICA_URGENCIA'),
    ('RUE_SALA_ESTABILIZACAO', 'ESTABILIZACAO'),
    ('RUE_UPA_24H', 'URGENCIA'), ('RUE_UPA_24H', 'CLASSIFICACAO_DE_RISCO'), ('RUE_UPA_24H', 'OBSERVACAO'),
    ('RUE_HOSPITALAR', 'INTERNACAO'), ('RUE_HOSPITALAR', 'INTERNACAO_CLINICA'), ('RUE_HOSPITALAR', 'CIRURGIA'),
    ('RUE_ATENCAO_DOMICILIAR', 'ATENCAO_DOMICILIAR'),
    ('RC_PRE_NATAL', 'ATENCAO_PRIMARIA'), ('RC_PRE_NATAL', 'ACOMPANHAMENTO_LONGITUDINAL'), ('RC_PRE_NATAL', 'ANALISES_CLINICAS'),
    ('RC_PRE_NATAL', 'COLETA_DE_AMOSTRAS'), ('RC_PRE_NATAL', 'DIAGNOSTICO_POR_IMAGEM'),
    ('RC_PARTO_NASCIMENTO', 'OBSTETRICIA'),
    ('RC_PUERPERIO_CRIANCA', 'ATENCAO_PRIMARIA'), ('RC_PUERPERIO_CRIANCA', 'SALA_VACINA'),
    ('RC_SISTEMA_LOGISTICO', 'TRANSFERENCIA'), ('RC_SISTEMA_LOGISTICO', 'ATENCAO_MOVEL'),
    ('PCD_ATENCAO_BASICA', 'ATENCAO_PRIMARIA'),
    ('PCD_REABILITACAO', 'REABILITACAO'), ('PCD_REABILITACAO', 'REABILITACAO_FISICA'), ('PCD_REABILITACAO', 'REABILITACAO_AUDITIVA'),
    ('PCD_REABILITACAO', 'REABILITACAO_VISUAL'), ('PCD_REABILITACAO', 'REABILITACAO_INTELECTUAL'),
    ('PCD_REABILITACAO', 'DISPENSACAO_OPM'), ('PCD_REABILITACAO', 'ODONTO_PNE'),
    ('PCD_HOSPITALAR_URGENCIA', 'INTERNACAO'), ('PCD_HOSPITALAR_URGENCIA', 'URGENCIA'),
    ('DC_ATENCAO_BASICA', 'ATENCAO_PRIMARIA'), ('DC_ATENCAO_BASICA', 'ACOMPANHAMENTO_LONGITUDINAL'),
    ('DC_ESPECIALIZADA', 'ATENCAO_ESPECIALIZADA'), ('DC_ESPECIALIZADA', 'CONSULTA_ESPECIALIZADA'), ('DC_ESPECIALIZADA', 'INTERNACAO'),
    ('DC_SISTEMAS_APOIO', 'ANALISES_CLINICAS'), ('DC_SISTEMAS_APOIO', 'DIAGNOSTICO_POR_IMAGEM'),
    ('DC_SISTEMAS_APOIO', 'ASSISTENCIA_FARMACEUTICA'), ('DC_SISTEMAS_APOIO', 'DISPENSACAO'),
    ('DC_SISTEMAS_LOGISTICOS', 'TRANSFERENCIA'), ('DC_SISTEMAS_LOGISTICOS', 'ATENCAO_MOVEL');
INSERT INTO public.tb_componente_rede_item (uuid, componente, perfil_id)
    SELECT gen_random_uuid(), c.componente, p.uuid FROM carga_componente_rede c JOIN public.tb_perfil_operacional p ON p.codigo = c.codigo;
INSERT INTO public.tb_componente_rede_item (uuid, componente, capacidade_id)
    SELECT gen_random_uuid(), c.componente, k.uuid FROM carga_componente_rede c JOIN public.tb_capacidade_assistencial k ON k.codigo = c.codigo;
DROP TABLE carga_componente_rede;

CREATE TABLE public.tb_referencia_rede (
    fim date,
    inicio date NOT NULL,
    encerrada_em timestamp(6) with time zone,
    registrada_em timestamp(6) with time zone NOT NULL,
    executante_id uuid NOT NULL,
    origem_id uuid NOT NULL,
    procedimento_id uuid NOT NULL,
    uuid uuid NOT NULL,
    observacao character varying(500),
    encerrada_por_cpf character varying(255),
    registrada_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_referencia_rede ADD CONSTRAINT tb_referencia_rede_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_referencia_rede ADD CONSTRAINT fk_referencia_rede_origem FOREIGN KEY (origem_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_referencia_rede ADD CONSTRAINT fk_referencia_rede_executante FOREIGN KEY (executante_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_referencia_rede ADD CONSTRAINT fk_referencia_rede_procedimento FOREIGN KEY (procedimento_id) REFERENCES public.tb_procedimento_regulado(uuid);
CREATE INDEX ix_referencia_rede_procedimento ON public.tb_referencia_rede USING btree (procedimento_id);
CREATE UNIQUE INDEX ux_referencia_rede_aberta ON public.tb_referencia_rede USING btree (origem_id, procedimento_id) WHERE fim IS NULL;

-- Na autorização: nulo sem referência vigente; verdadeiro quando a executante escolhida não é a da referência.
ALTER TABLE public.tb_solicitacao_regulacao ADD COLUMN fora_da_referencia boolean;
