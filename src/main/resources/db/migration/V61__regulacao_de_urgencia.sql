-- Regulação de urgência (ADR-0176): a ocorrência da central de regulação das urgências (192) e os passos dela, imutáveis.
-- Conferido contra o ddl-auto=validate (ADR-0090). O protocolo usa uma sequência que não reinicia com o ano.

CREATE SEQUENCE public.sq_protocolo_urgencia START WITH 1 INCREMENT BY 1;

CREATE TABLE public.tb_ocorrencia_urgencia (
    vaga_zero boolean NOT NULL,
    latitude double precision,
    longitude double precision,
    recebida_em timestamp(6) with time zone NOT NULL,
    regulada_em timestamp(6) with time zone,
    recebida_no_destino_em timestamp(6) with time zone,
    encerrada_em timestamp(6) with time zone,
    atendente_id uuid,
    central_id uuid NOT NULL,
    crise_id uuid,
    destino_id uuid,
    paciente_id uuid,
    regulador_id uuid,
    unidade_solicitante_id uuid,
    uuid uuid NOT NULL,
    prioridade character varying(10),
    protocolo character varying(20) NOT NULL,
    situacao character varying(20) NOT NULL,
    decisao character varying(20),
    tipo_recurso character varying(20),
    solicitante_telefone character varying(20),
    origem character varying(30) NOT NULL,
    local_tipo character varying(30),
    desfecho character varying(30),
    recurso_identificacao character varying(60),
    solicitante_nome character varying(100),
    descricao_paciente character varying(200),
    endereco character varying(300),
    referencia character varying(300),
    justificativa_vaga_zero character varying(500),
    queixa character varying(2000) NOT NULL,
    conduta character varying(2000),
    recebida_por_cpf character varying(255),
    CONSTRAINT tb_ocorrencia_urgencia_origem_check CHECK (origem IN ('CHAMADO_192', 'UNIDADE_DE_SAUDE', 'CRISE_SAUDE_MENTAL')),
    CONSTRAINT tb_ocorrencia_urgencia_situacao_check CHECK (situacao IN ('RECEBIDA', 'REGULADA', 'EM_ATENDIMENTO', 'CONCLUIDA', 'CANCELADA')),
    CONSTRAINT tb_ocorrencia_urgencia_prioridade_check CHECK (prioridade IN ('VERMELHO', 'AMARELO', 'VERDE', 'AZUL')),
    CONSTRAINT tb_ocorrencia_urgencia_decisao_check CHECK (decisao IN ('ORIENTACAO', 'ENVIO_DE_RECURSO', 'ENCAMINHAMENTO', 'TRANSFERENCIA')),
    CONSTRAINT tb_ocorrencia_urgencia_tipo_recurso_check CHECK (tipo_recurso IN ('USA', 'USB', 'MOTOLANCIA', 'AEROMEDICO', 'OUTRO')),
    CONSTRAINT tb_ocorrencia_urgencia_local_tipo_check CHECK (local_tipo IN ('DOMICILIO', 'VIA_PUBLICA', 'LOCAL_DO_EVENTO', 'ESCOLA', 'PONTO_DA_UNIDADE', 'OUTRO')),
    CONSTRAINT tb_ocorrencia_urgencia_desfecho_check CHECK (desfecho IN ('ORIENTADO', 'ATENDIDO_NO_LOCAL', 'RECEBIDO_NO_DESTINO', 'OBITO_NO_LOCAL', 'RECUSA', 'SEM_OCORRENCIA', 'DESISTENCIA'))
);
ALTER TABLE ONLY public.tb_ocorrencia_urgencia ADD CONSTRAINT tb_ocorrencia_urgencia_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_ocorrencia_urgencia ADD CONSTRAINT uk_ocorrencia_urgencia_protocolo UNIQUE (protocolo);
CREATE INDEX ix_ocorrencia_urgencia_central ON public.tb_ocorrencia_urgencia USING btree (central_id, situacao);
CREATE INDEX ix_ocorrencia_urgencia_paciente ON public.tb_ocorrencia_urgencia USING btree (paciente_id);
CREATE INDEX ix_ocorrencia_urgencia_destino ON public.tb_ocorrencia_urgencia USING btree (destino_id);
ALTER TABLE ONLY public.tb_ocorrencia_urgencia ADD CONSTRAINT fk_ocorrencia_urgencia_central FOREIGN KEY (central_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_ocorrencia_urgencia ADD CONSTRAINT fk_ocorrencia_urgencia_solicitante FOREIGN KEY (unidade_solicitante_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_ocorrencia_urgencia ADD CONSTRAINT fk_ocorrencia_urgencia_destino FOREIGN KEY (destino_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_ocorrencia_urgencia ADD CONSTRAINT fk_ocorrencia_urgencia_crise FOREIGN KEY (crise_id) REFERENCES public.tb_evento_crise(uuid);
ALTER TABLE ONLY public.tb_ocorrencia_urgencia ADD CONSTRAINT fk_ocorrencia_urgencia_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_ocorrencia_urgencia ADD CONSTRAINT fk_ocorrencia_urgencia_atendente FOREIGN KEY (atendente_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_ocorrencia_urgencia ADD CONSTRAINT fk_ocorrencia_urgencia_regulador FOREIGN KEY (regulador_id) REFERENCES public.tb_profissional(uuid);

CREATE TABLE public.tb_evento_urgencia (
    ocorrido_em timestamp(6) with time zone NOT NULL,
    ocorrencia_id uuid NOT NULL,
    profissional_id uuid,
    uuid uuid NOT NULL,
    prioridade character varying(10),
    situacao_resultante character varying(20) NOT NULL,
    tipo character varying(30) NOT NULL,
    texto character varying(2000),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_evento_urgencia_tipo_check CHECK (tipo IN ('RECEBIMENTO', 'REGULACAO', 'RECLASSIFICACAO', 'DESTINO', 'ENVIO_DE_RECURSO', 'CHEGADA_AO_LOCAL',
        'SAIDA_DO_LOCAL', 'CHEGADA_AO_DESTINO', 'RECEBIMENTO_NO_DESTINO', 'VINCULO_DO_PACIENTE', 'ENCERRAMENTO', 'CANCELAMENTO')),
    CONSTRAINT tb_evento_urgencia_situacao_resultante_check CHECK (situacao_resultante IN ('RECEBIDA', 'REGULADA', 'EM_ATENDIMENTO', 'CONCLUIDA', 'CANCELADA')),
    CONSTRAINT tb_evento_urgencia_prioridade_check CHECK (prioridade IN ('VERMELHO', 'AMARELO', 'VERDE', 'AZUL'))
);
ALTER TABLE ONLY public.tb_evento_urgencia ADD CONSTRAINT tb_evento_urgencia_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_evento_urgencia ON public.tb_evento_urgencia USING btree (ocorrencia_id, ocorrido_em);
ALTER TABLE ONLY public.tb_evento_urgencia ADD CONSTRAINT fk_evento_urgencia_ocorrencia FOREIGN KEY (ocorrencia_id) REFERENCES public.tb_ocorrencia_urgencia(uuid);
ALTER TABLE ONLY public.tb_evento_urgencia ADD CONSTRAINT fk_evento_urgencia_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
