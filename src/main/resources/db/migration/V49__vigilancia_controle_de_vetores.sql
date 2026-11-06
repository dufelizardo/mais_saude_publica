-- Vigilância em Saúde, fatia V4a (ADR-0162, desenho na ADR-0110): o controle de vetores no modelo do PNCD. A vistoria do
-- agente de endemias ao imóvel do Território, os focos e as ações de controle. As permissões e os papéis entram pelo
-- CatalogoDeAcesso na subida. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_acao_controle_vetorial (
    imoveis_trabalhados integer,
    data_prevista date NOT NULL,
    data_realizada date,
    registrado_em timestamp(6) with time zone NOT NULL,
    notificacao_id uuid,
    surto_id uuid,
    uuid uuid NOT NULL,
    tipo character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    responsavel character varying(200) NOT NULL,
    descricao character varying(1000),
    resultado character varying(1000),
    motivo_cancelamento character varying(500),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_acao_controle_vetorial_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['BLOQUEIO_TRANSMISSAO'::character varying, 'NEBULIZACAO'::character varying, 'MUTIRAO_LIMPEZA'::character varying, 'EDUCATIVA'::character varying, 'OUTRA'::character varying])::text[]))),
    CONSTRAINT tb_acao_controle_vetorial_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['PLANEJADA'::character varying, 'REALIZADA'::character varying, 'CANCELADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_acao_controle_vetorial ADD CONSTRAINT tb_acao_controle_vetorial_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_acao_controle_vetorial ADD CONSTRAINT fk_acao_controle_vetorial_notificacao FOREIGN KEY (notificacao_id) REFERENCES public.tb_notificacao(uuid);
ALTER TABLE ONLY public.tb_acao_controle_vetorial ADD CONSTRAINT fk_acao_controle_vetorial_surto FOREIGN KEY (surto_id) REFERENCES public.tb_surto(uuid);

CREATE TABLE public.tb_acao_controle_vetorial_microarea (
    acao_id uuid NOT NULL,
    microarea_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_acao_controle_vetorial_microarea ADD CONSTRAINT tb_acao_controle_vetorial_microarea_pkey PRIMARY KEY (acao_id, microarea_id);
ALTER TABLE ONLY public.tb_acao_controle_vetorial_microarea ADD CONSTRAINT fk_acao_controle_vetorial_microarea_acao FOREIGN KEY (acao_id) REFERENCES public.tb_acao_controle_vetorial(uuid);
ALTER TABLE ONLY public.tb_acao_controle_vetorial_microarea ADD CONSTRAINT fk_acao_controle_vetorial_microarea_microarea FOREIGN KEY (microarea_id) REFERENCES public.tb_microarea(uuid);

CREATE TABLE public.tb_vistoria_vetorial (
    ciclo_ano integer NOT NULL,
    ciclo_numero integer NOT NULL,
    depositos_a1 integer NOT NULL,
    depositos_a2 integer NOT NULL,
    depositos_b integer NOT NULL,
    depositos_c integer NOT NULL,
    depositos_d1 integer NOT NULL,
    depositos_d2 integer NOT NULL,
    depositos_e integer NOT NULL,
    depositos_eliminados integer NOT NULL,
    depositos_tratados integer NOT NULL,
    larvicida_gramas double precision,
    data date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    acao_id uuid,
    agente_id uuid NOT NULL,
    imovel_id uuid NOT NULL,
    uuid uuid NOT NULL,
    tipo character varying(255) NOT NULL,
    resultado character varying(255) NOT NULL,
    larvicida character varying(100),
    observacao character varying(1000),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_vistoria_vetorial_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ROTINA'::character varying, 'LIRAA'::character varying, 'BLOQUEIO'::character varying, 'PONTO_ESTRATEGICO'::character varying, 'DENUNCIA'::character varying])::text[]))),
    CONSTRAINT tb_vistoria_vetorial_resultado_check CHECK (((resultado)::text = ANY ((ARRAY['INSPECIONADA'::character varying, 'FECHADA'::character varying, 'RECUSADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_vistoria_vetorial ADD CONSTRAINT tb_vistoria_vetorial_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_vistoria_vetorial_ciclo ON public.tb_vistoria_vetorial USING btree (ciclo_ano, ciclo_numero);
CREATE INDEX ix_vistoria_vetorial_imovel ON public.tb_vistoria_vetorial USING btree (imovel_id, data);
ALTER TABLE ONLY public.tb_vistoria_vetorial ADD CONSTRAINT fk_vistoria_vetorial_imovel FOREIGN KEY (imovel_id) REFERENCES public.tb_imovel(uuid);
ALTER TABLE ONLY public.tb_vistoria_vetorial ADD CONSTRAINT fk_vistoria_vetorial_agente FOREIGN KEY (agente_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_vistoria_vetorial ADD CONSTRAINT fk_vistoria_vetorial_acao FOREIGN KEY (acao_id) REFERENCES public.tb_acao_controle_vetorial(uuid);

CREATE TABLE public.tb_foco_vetorial (
    amostra_coletada boolean NOT NULL,
    encerrado_em timestamp(6) with time zone,
    resultado_em timestamp(6) with time zone,
    uuid uuid NOT NULL,
    vistoria_id uuid NOT NULL,
    tipo_deposito character varying(255) NOT NULL,
    fase character varying(255) NOT NULL,
    especie_suspeita character varying(255) NOT NULL,
    resultado_laboratorio character varying(255),
    situacao character varying(255) NOT NULL,
    observacao character varying(500),
    CONSTRAINT tb_foco_vetorial_tipo_deposito_check CHECK (((tipo_deposito)::text = ANY ((ARRAY['A1'::character varying, 'A2'::character varying, 'B'::character varying, 'C'::character varying, 'D1'::character varying, 'D2'::character varying, 'E'::character varying])::text[]))),
    CONSTRAINT tb_foco_vetorial_fase_check CHECK (((fase)::text = ANY ((ARRAY['LARVA'::character varying, 'PUPA'::character varying, 'LARVA_E_PUPA'::character varying])::text[]))),
    CONSTRAINT tb_foco_vetorial_especie_suspeita_check CHECK (((especie_suspeita)::text = ANY ((ARRAY['AEDES_AEGYPTI'::character varying, 'AEDES_ALBOPICTUS'::character varying, 'OUTRA'::character varying, 'NAO_IDENTIFICADA'::character varying])::text[]))),
    CONSTRAINT tb_foco_vetorial_resultado_laboratorio_check CHECK (((resultado_laboratorio)::text = ANY ((ARRAY['AEDES_AEGYPTI'::character varying, 'AEDES_ALBOPICTUS'::character varying, 'OUTRA'::character varying, 'NEGATIVO'::character varying])::text[]))),
    CONSTRAINT tb_foco_vetorial_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ATIVO'::character varying, 'TRATADO'::character varying, 'ELIMINADO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_foco_vetorial ADD CONSTRAINT tb_foco_vetorial_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_foco_vetorial_situacao ON public.tb_foco_vetorial USING btree (situacao);
ALTER TABLE ONLY public.tb_foco_vetorial ADD CONSTRAINT fk_foco_vetorial_vistoria FOREIGN KEY (vistoria_id) REFERENCES public.tb_vistoria_vetorial(uuid);
