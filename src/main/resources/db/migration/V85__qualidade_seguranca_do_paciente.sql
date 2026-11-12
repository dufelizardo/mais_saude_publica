-- Qualidade, fatia Q1 (ADR-0203, desenho na ADR-0202): a segurança do paciente pela RDC ANVISA 36/2013 e pelo PNSP (Portaria MS
-- 529/2013). A notificação interna de incidente (anônima ou identificada, cultura não punitiva), a classificação pela taxonomia da OMS
-- (ICPS), a análise do Núcleo de Segurança do Paciente com a causa raiz e o registro da notificação ao SNVS (Notivisa) com o prazo. As
-- permissões e o papel entram pelo CatalogoDeAcesso. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_incidente_seguranca (
    anonima boolean NOT NULL,
    never_event boolean NOT NULL,
    analisado_em timestamp(6) with time zone,
    notivisa_em timestamp(6) with time zone,
    ocorrido_em timestamp(6) with time zone NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    paciente_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    numero character varying(20) NOT NULL,
    numero_notivisa character varying(40),
    tipo character varying(255) NOT NULL,
    natureza character varying(255),
    dano character varying(255),
    dano_percebido character varying(255),
    situacao character varying(255) NOT NULL,
    local character varying(200) NOT NULL,
    fatores character varying(300),
    descricao character varying(4000) NOT NULL,
    acao_imediata character varying(2000),
    causa_raiz character varying(4000),
    licoes character varying(2000),
    motivo_descarte character varying(1000),
    notificado_por_cpf character varying(255),
    analisado_por_cpf character varying(255),
    CONSTRAINT tb_incidente_seguranca_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['IDENTIFICACAO_PACIENTE'::character varying, 'COMUNICACAO'::character varying, 'MEDICACAO'::character varying, 'CIRURGIA'::character varying, 'INFECCAO'::character varying, 'QUEDA'::character varying, 'LESAO_POR_PRESSAO'::character varying, 'DIETA'::character varying, 'EQUIPAMENTO'::character varying, 'DOCUMENTACAO'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_incidente_seguranca_natureza_check CHECK (((natureza)::text = ANY ((ARRAY['CIRCUNSTANCIA_DE_RISCO'::character varying, 'QUASE_ERRO'::character varying, 'INCIDENTE_SEM_DANO'::character varying, 'EVENTO_ADVERSO'::character varying])::text[]))),
    CONSTRAINT tb_incidente_seguranca_dano_check CHECK (((dano)::text = ANY ((ARRAY['NENHUM'::character varying, 'LEVE'::character varying, 'MODERADO'::character varying, 'GRAVE'::character varying, 'OBITO'::character varying])::text[]))),
    CONSTRAINT tb_incidente_seguranca_dano_percebido_check CHECK (((dano_percebido)::text = ANY ((ARRAY['NENHUM'::character varying, 'LEVE'::character varying, 'MODERADO'::character varying, 'GRAVE'::character varying, 'OBITO'::character varying])::text[]))),
    CONSTRAINT tb_incidente_seguranca_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['RECEBIDA'::character varying, 'EM_ANALISE'::character varying, 'CONCLUIDA'::character varying, 'DESCARTADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_incidente_seguranca ADD CONSTRAINT tb_incidente_seguranca_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_incidente_seguranca ADD CONSTRAINT uk_incidente_seguranca_numero UNIQUE (numero);
CREATE INDEX ix_incidente_seguranca_unidade ON public.tb_incidente_seguranca USING btree (unidade_id, ocorrido_em);
ALTER TABLE ONLY public.tb_incidente_seguranca ADD CONSTRAINT fk_incidente_seguranca_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_incidente_seguranca ADD CONSTRAINT fk_incidente_seguranca_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
