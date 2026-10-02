-- Alertas da auditoria (ADR-0096): padrões suspeitos achados na trilha, analisados por quem audita. Escrito no formato
-- que o Hibernate gera e conferido pelo MigracoesDoEsquemaTest (ADR-0090).

CREATE TABLE public.tb_alerta_auditoria (
    quantidade integer NOT NULL,
    analisado_em timestamp(6) with time zone,
    atualizado_em timestamp(6) with time zone NOT NULL,
    detectado_em timestamp(6) with time zone NOT NULL,
    primeiro_evento_em timestamp(6) with time zone NOT NULL,
    ultimo_evento_em timestamp(6) with time zone NOT NULL,
    paciente_id uuid,
    unidade_id uuid,
    uuid uuid NOT NULL,
    sujeito character varying(100) NOT NULL,
    chave character varying(200) NOT NULL,
    descricao character varying(500) NOT NULL,
    parecer character varying(2000),
    analisado_por_cpf character varying(255),
    severidade character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    usuario_cpf character varying(255),
    CONSTRAINT tb_alerta_auditoria_severidade_check CHECK (((severidade)::text = ANY ((ARRAY['ALTA'::character varying, 'MEDIA'::character varying])::text[]))),
    CONSTRAINT tb_alerta_auditoria_status_check CHECK (((status)::text = ANY ((ARRAY['ABERTO'::character varying, 'PROCEDENTE'::character varying, 'IMPROCEDENTE'::character varying])::text[]))),
    CONSTRAINT tb_alerta_auditoria_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['RECUSAS_SEGUIDAS'::character varying, 'LOGIN_RECUSADO'::character varying, 'LEITURA_EM_MASSA'::character varying, 'LEITURA_FORA_DO_HORARIO'::character varying, 'ACESSO_JUSTIFICADO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_alerta_auditoria
    ADD CONSTRAINT tb_alerta_auditoria_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_alerta_auditoria
    ADD CONSTRAINT tb_alerta_auditoria_chave_key UNIQUE (chave);

CREATE INDEX ix_alerta_auditoria_status ON public.tb_alerta_auditoria USING btree (status, detectado_em);

CREATE INDEX ix_alerta_auditoria_sujeito ON public.tb_alerta_auditoria USING btree (tipo, sujeito, ultimo_evento_em);

-- A detecção agrega por ação e período: a leitura em massa e a de madrugada filtram por ação antes do usuário.
CREATE INDEX ix_auditoria_acao_data ON public.tb_evento_auditoria USING btree (acao, ocorrido_em);
