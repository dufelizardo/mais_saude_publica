-- Qualidade, fatia Q3 (ADR-0205, desenho na ADR-0202): as metas dos indicadores de qualidade por unidade (histórico de vigência), os
-- checklists de adesão aos protocolos básicos versionados como dado e as auditorias de protocolo; a não conformidade passa a aceitar a
-- origem indicador com o indicador e a competência. Os indicadores são calculados dos registros da rede, sem tabela.
-- Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_checklist_protocolo (
    ativo boolean NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    uuid uuid NOT NULL,
    codigo character varying(40) NOT NULL,
    nome character varying(200) NOT NULL
);
ALTER TABLE ONLY public.tb_checklist_protocolo ADD CONSTRAINT tb_checklist_protocolo_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_checklist_protocolo ADD CONSTRAINT uk_checklist_protocolo_codigo UNIQUE (codigo);

CREATE TABLE public.tb_versao_checklist_protocolo (
    versao integer NOT NULL,
    criada_em timestamp(6) with time zone NOT NULL,
    checklist_id uuid NOT NULL,
    uuid uuid NOT NULL,
    definicao text NOT NULL,
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_versao_checklist_protocolo ADD CONSTRAINT tb_versao_checklist_protocolo_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_versao_checklist_protocolo ADD CONSTRAINT uk_versao_checklist_protocolo UNIQUE (checklist_id, versao);
ALTER TABLE ONLY public.tb_versao_checklist_protocolo ADD CONSTRAINT fk_versao_checklist_protocolo_checklist FOREIGN KEY (checklist_id) REFERENCES public.tb_checklist_protocolo(uuid);

CREATE TABLE public.tb_auditoria_protocolo (
    aplicaveis integer NOT NULL,
    conformes integer NOT NULL,
    data_auditoria date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    setor_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    versao_id uuid NOT NULL,
    respostas text NOT NULL,
    observacao character varying(1000),
    auditor_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_auditoria_protocolo ADD CONSTRAINT tb_auditoria_protocolo_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_auditoria_protocolo_unidade ON public.tb_auditoria_protocolo USING btree (unidade_id, data_auditoria);
ALTER TABLE ONLY public.tb_auditoria_protocolo ADD CONSTRAINT fk_auditoria_protocolo_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_auditoria_protocolo ADD CONSTRAINT fk_auditoria_protocolo_setor FOREIGN KEY (setor_id) REFERENCES public.tb_setor(uuid);
ALTER TABLE ONLY public.tb_auditoria_protocolo ADD CONSTRAINT fk_auditoria_protocolo_versao FOREIGN KEY (versao_id) REFERENCES public.tb_versao_checklist_protocolo(uuid);

CREATE TABLE public.tb_meta_qualidade (
    valor numeric(14,2) NOT NULL,
    vigente_ate date,
    vigente_desde date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    checklist_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    indicador character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_meta_qualidade_indicador_check CHECK (((indicador)::text = ANY ((ARRAY['TAXA_QUEDAS'::character varying, 'INCIDENCIA_LESAO_PRESSAO'::character varying, 'REINTERNACAO_30_DIAS'::character varying, 'EVENTOS_ADVERSOS_GRAVES'::character varying, 'TEMPO_ATE_MEDICO'::character varying, 'ABSENTEISMO'::character varying, 'ADESAO_PROTOCOLO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_meta_qualidade ADD CONSTRAINT tb_meta_qualidade_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_meta_qualidade_unidade ON public.tb_meta_qualidade USING btree (unidade_id, indicador);
ALTER TABLE ONLY public.tb_meta_qualidade ADD CONSTRAINT fk_meta_qualidade_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_meta_qualidade ADD CONSTRAINT fk_meta_qualidade_checklist FOREIGN KEY (checklist_id) REFERENCES public.tb_checklist_protocolo(uuid);

-- A não conformidade com origem indicador: o indicador, a competência (primeiro dia do mês) e, na adesão, o protocolo.
ALTER TABLE public.tb_nao_conformidade ADD COLUMN indicador character varying(255);
ALTER TABLE public.tb_nao_conformidade ADD COLUMN competencia date;
ALTER TABLE public.tb_nao_conformidade ADD COLUMN checklist_id uuid;
ALTER TABLE public.tb_nao_conformidade ADD CONSTRAINT tb_nao_conformidade_indicador_check CHECK (((indicador)::text = ANY ((ARRAY['TAXA_QUEDAS'::character varying, 'INCIDENCIA_LESAO_PRESSAO'::character varying, 'REINTERNACAO_30_DIAS'::character varying, 'EVENTOS_ADVERSOS_GRAVES'::character varying, 'TEMPO_ATE_MEDICO'::character varying, 'ABSENTEISMO'::character varying, 'ADESAO_PROTOCOLO'::character varying])::text[])));
ALTER TABLE ONLY public.tb_nao_conformidade ADD CONSTRAINT fk_nao_conformidade_checklist FOREIGN KEY (checklist_id) REFERENCES public.tb_checklist_protocolo(uuid);

-- Carga: os protocolos básicos do PNSP com a auditoria de adesão mais comum.
INSERT INTO public.tb_checklist_protocolo (ativo, registrado_em, uuid, codigo, nome) VALUES
    (true, now(), gen_random_uuid(), 'HIGIENE_DAS_MAOS', 'Higiene das mãos: os cinco momentos (OMS)'),
    (true, now(), gen_random_uuid(), 'CIRURGIA_SEGURA', 'Cirurgia segura: lista de verificação (OMS)');
INSERT INTO public.tb_versao_checklist_protocolo (versao, criada_em, checklist_id, uuid, definicao)
SELECT 1, now(), c.uuid, gen_random_uuid(), v.definicao FROM public.tb_checklist_protocolo c JOIN (VALUES
    ('HIGIENE_DAS_MAOS', '{"itens": [{"codigo": "M1", "texto": "Antes de tocar o paciente"}, {"codigo": "M2", "texto": "Antes de realizar procedimento limpo ou asséptico"}, {"codigo": "M3", "texto": "Após o risco de exposição a fluidos corporais"}, {"codigo": "M4", "texto": "Após tocar o paciente"}, {"codigo": "M5", "texto": "Após tocar superfícies próximas ao paciente"}]}'),
    ('CIRURGIA_SEGURA', '{"itens": [{"codigo": "E1", "texto": "Entrada: identidade, sítio, procedimento e consentimento confirmados"}, {"codigo": "E2", "texto": "Entrada: sítio demarcado"}, {"codigo": "E3", "texto": "Entrada: verificação de anestesia e oxímetro funcionando"}, {"codigo": "E4", "texto": "Entrada: alergias, via aérea difícil e risco de sangramento avaliados"}, {"codigo": "P1", "texto": "Pausa: equipe apresentada e paciente, sítio e procedimento confirmados em voz alta"}, {"codigo": "P2", "texto": "Pausa: antibiótico profilático nos últimos 60 minutos"}, {"codigo": "P3", "texto": "Pausa: eventos críticos revisados e imagens disponíveis"}, {"codigo": "S1", "texto": "Saída: procedimento registrado e contagem de instrumentais, compressas e agulhas conferida"}, {"codigo": "S2", "texto": "Saída: amostras identificadas e problemas com equipamentos registrados"}, {"codigo": "S3", "texto": "Saída: cuidados da recuperação revisados"}]}')
) AS v(codigo, definicao) ON v.codigo = c.codigo;
