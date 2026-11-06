-- Saúde Mental, fatia SM5 (ADR-0153): componentes da RAPS como dado do modelo operacional e o acolhimento noturno do CAPS.

-- O componente da Rede de Atenção Psicossocial (Portaria de Consolidação nº 3/2017, anexo V) no perfil e na capacidade.
ALTER TABLE public.tb_perfil_operacional ADD COLUMN componente_raps character varying(255);
ALTER TABLE public.tb_capacidade_assistencial ADD COLUMN componente_raps character varying(255);
ALTER TABLE public.tb_perfil_operacional ADD CONSTRAINT tb_perfil_operacional_componente_raps_check CHECK (((componente_raps)::text = ANY ((ARRAY['ATENCAO_BASICA'::character varying, 'ATENCAO_PSICOSSOCIAL_ESPECIALIZADA'::character varying, 'URGENCIA_EMERGENCIA'::character varying, 'RESIDENCIAL_TRANSITORIO'::character varying, 'HOSPITALAR'::character varying, 'DESINSTITUCIONALIZACAO'::character varying, 'REABILITACAO_PSICOSSOCIAL'::character varying])::text[])));
ALTER TABLE public.tb_capacidade_assistencial ADD CONSTRAINT tb_capacidade_assistencial_componente_raps_check CHECK (((componente_raps)::text = ANY ((ARRAY['ATENCAO_BASICA'::character varying, 'ATENCAO_PSICOSSOCIAL_ESPECIALIZADA'::character varying, 'URGENCIA_EMERGENCIA'::character varying, 'RESIDENCIAL_TRANSITORIO'::character varying, 'HOSPITALAR'::character varying, 'DESINSTITUCIONALIZACAO'::character varying, 'REABILITACAO_PSICOSSOCIAL'::character varying])::text[])));

UPDATE public.tb_perfil_operacional SET componente_raps = 'ATENCAO_BASICA' WHERE codigo = 'ATENCAO_PRIMARIA';
UPDATE public.tb_perfil_operacional SET componente_raps = 'URGENCIA_EMERGENCIA' WHERE codigo IN ('URGENCIA', 'ATENCAO_MOVEL');
UPDATE public.tb_capacidade_assistencial SET componente_raps = 'ATENCAO_PSICOSSOCIAL_ESPECIALIZADA'
WHERE codigo IN ('CAPS_I', 'CAPS_II', 'CAPS_III', 'CAPS_INFANTOJUVENIL', 'CAPS_AD', 'CAPS_AD_III', 'ACOLHIMENTO_NOTURNO');
UPDATE public.tb_capacidade_assistencial SET componente_raps = 'HOSPITALAR' WHERE codigo = 'LEITO_SAUDE_MENTAL';

-- Os pontos da RAPS que faltavam no catálogo; os de moradia e de trabalho são transversais (sem perfil).
INSERT INTO public.tb_capacidade_assistencial (uuid, codigo, nome, descricao, ativo, perfil_operacional_id, componente_raps)
SELECT gen_random_uuid(), c.codigo, c.nome, c.descricao, true, p.uuid, c.componente
FROM (VALUES
    ('CONSULTORIO_NA_RUA', 'Consultório na Rua', 'Equipe que cuida da população em situação de rua no território.', 'ATENCAO_PRIMARIA', 'ATENCAO_BASICA'),
    ('CENTRO_DE_CONVIVENCIA', 'Centro de convivência', 'Espaço de sociabilidade, cultura e inclusão social.', NULL, 'ATENCAO_BASICA'),
    ('UNIDADE_ACOLHIMENTO_ADULTO', 'Unidade de acolhimento adulto', 'Acolhimento residencial transitório de adultos, ligado ao CAPS.', NULL,
     'RESIDENCIAL_TRANSITORIO'),
    ('UNIDADE_ACOLHIMENTO_INFANTOJUVENIL', 'Unidade de acolhimento infantojuvenil', 'Acolhimento residencial transitório de crianças e adolescentes.',
     NULL, 'RESIDENCIAL_TRANSITORIO'),
    ('SERVICO_RESIDENCIAL_TERAPEUTICO', 'Serviço residencial terapêutico', 'Moradia para egressos de longas internações psiquiátricas.', NULL,
     'DESINSTITUCIONALIZACAO'),
    ('GERACAO_TRABALHO_RENDA', 'Geração de trabalho e renda', 'Iniciativas de trabalho, renda e economia solidária.', NULL, 'REABILITACAO_PSICOSSOCIAL')
) AS c(codigo, nome, descricao, perfil, componente)
LEFT JOIN public.tb_perfil_operacional p ON p.codigo = c.perfil
WHERE NOT EXISTS (SELECT 1 FROM public.tb_capacidade_assistencial x WHERE x.codigo = c.codigo);

-- O acolhimento noturno do CAPS: as vagas e a permanência máxima da unidade, e as permanências.
CREATE TABLE public.tb_configuracao_acolhimento_noturno (
    permanencia_maxima_dias integer NOT NULL,
    vagas integer NOT NULL,
    atualizada_em timestamp(6) with time zone NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_configuracao_acolhimento_noturno ADD CONSTRAINT tb_configuracao_acolhimento_noturno_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_configuracao_acolhimento_noturno ADD CONSTRAINT uk_configuracao_acolhimento_unidade UNIQUE (unidade_id);
ALTER TABLE ONLY public.tb_configuracao_acolhimento_noturno ADD CONSTRAINT fk_configuracao_acolhimento_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_permanencia_acolhimento_noturno (
    entrada date NOT NULL,
    saida date,
    saida_prevista date NOT NULL,
    registrada_em timestamp(6) with time zone NOT NULL,
    acompanhamento_id uuid NOT NULL,
    paciente_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    justificativa_prazo character varying(500),
    motivo_saida character varying(500),
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_permanencia_acolhimento_noturno ADD CONSTRAINT tb_permanencia_acolhimento_noturno_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_permanencia_acolhimento_noturno ADD CONSTRAINT fk_permanencia_acolhimento_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_permanencia_acolhimento_noturno ADD CONSTRAINT fk_permanencia_acolhimento_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_permanencia_acolhimento_noturno ADD CONSTRAINT fk_permanencia_acolhimento_acompanhamento FOREIGN KEY (acompanhamento_id) REFERENCES public.tb_acompanhamento_saude_mental(uuid);
CREATE INDEX ix_permanencia_acolhimento_unidade ON public.tb_permanencia_acolhimento_noturno USING btree (unidade_id, saida);
CREATE INDEX ix_permanencia_acolhimento_paciente ON public.tb_permanencia_acolhimento_noturno USING btree (paciente_id);
