-- Saúde do Trabalhador, fatia V5a da Vigilância (ADR-0207): os agravos relacionados ao trabalho no catálogo, o bloco do
-- trabalho na notificação (ocupação, situação, empregador, local e dados do acidente), o acompanhamento sorológico do
-- acidente com material biológico e a ligação opcional ao acidente do servidor no RH. Conferido contra o ddl-auto=validate
-- (ADR-0090).

-- O agravo marcado pede o bloco do trabalho na notificação.
ALTER TABLE public.tb_agravo ADD COLUMN relacionado_ao_trabalho boolean DEFAULT false NOT NULL;
UPDATE public.tb_agravo SET relacionado_ao_trabalho = true WHERE codigo = 'ACIDENTE_TRABALHO_GRAVE';

-- O acidente com material biológico (compulsório, semanal) e os agravos da vigilância sentinela em saúde do trabalhador,
-- todos editáveis no catálogo.
INSERT INTO public.tb_agravo (uuid, codigo, nome, compulsorio, periodicidade, prazo_encerramento_dias, sensivel, ativo, relacionado_ao_trabalho) VALUES
    (gen_random_uuid(), 'ACIDENTE_TRABALHO_MATERIAL_BIOLOGICO', 'Acidente de trabalho com exposição a material biológico', true, 'SEMANAL', 180, false, true, true),
    (gen_random_uuid(), 'LER_DORT', 'LER/DORT relacionados ao trabalho', false, 'SEMANAL', 180, false, true, true),
    (gen_random_uuid(), 'PAIR', 'Perda auditiva induzida por ruído relacionada ao trabalho (PAIR)', false, 'SEMANAL', 180, false, true, true),
    (gen_random_uuid(), 'PNEUMOCONIOSE', 'Pneumoconiose relacionada ao trabalho', false, 'SEMANAL', 180, false, true, true),
    (gen_random_uuid(), 'DERMATOSE_OCUPACIONAL', 'Dermatose ocupacional', false, 'SEMANAL', 180, false, true, true),
    (gen_random_uuid(), 'CANCER_RELACIONADO_TRABALHO', 'Câncer relacionado ao trabalho', false, 'SEMANAL', 180, false, true, true),
    (gen_random_uuid(), 'TRANSTORNO_MENTAL_TRABALHO', 'Transtorno mental relacionado ao trabalho', false, 'SEMANAL', 180, false, true, true);

INSERT INTO public.tb_agravo_cid (agravo_id, cid)
SELECT a.uuid, v.cid FROM (VALUES ('ACIDENTE_TRABALHO_GRAVE', 'Y96'), ('ACIDENTE_TRABALHO_MATERIAL_BIOLOGICO', 'Z209'), ('LER_DORT', 'Z579'),
    ('PAIR', 'H833'), ('PNEUMOCONIOSE', 'J60'), ('PNEUMOCONIOSE', 'J61'), ('PNEUMOCONIOSE', 'J62'), ('PNEUMOCONIOSE', 'J63'),
    ('PNEUMOCONIOSE', 'J64'), ('PNEUMOCONIOSE', 'J65'), ('DERMATOSE_OCUPACIONAL', 'L989'), ('CANCER_RELACIONADO_TRABALHO', 'C80'),
    ('TRANSTORNO_MENTAL_TRABALHO', 'F99')) AS v(codigo, cid)
    JOIN public.tb_agravo a ON a.codigo = v.codigo;

INSERT INTO public.tb_agravo_criterio (agravo_id, criterio)
SELECT a.uuid, 'CLINICO' FROM public.tb_agravo a WHERE a.codigo IN ('ACIDENTE_TRABALHO_MATERIAL_BIOLOGICO', 'LER_DORT', 'PAIR', 'PNEUMOCONIOSE',
    'DERMATOSE_OCUPACIONAL', 'CANCER_RELACIONADO_TRABALHO', 'TRANSTORNO_MENTAL_TRABALHO');
INSERT INTO public.tb_agravo_criterio (agravo_id, criterio)
SELECT a.uuid, 'CLINICO_EPIDEMIOLOGICO' FROM public.tb_agravo a WHERE a.codigo IN ('LER_DORT', 'PAIR', 'PNEUMOCONIOSE', 'DERMATOSE_OCUPACIONAL',
    'CANCER_RELACIONADO_TRABALHO', 'TRANSTORNO_MENTAL_TRABALHO');
INSERT INTO public.tb_agravo_criterio (agravo_id, criterio)
SELECT a.uuid, 'LABORATORIAL' FROM public.tb_agravo a WHERE a.codigo IN ('PAIR', 'PNEUMOCONIOSE', 'CANCER_RELACIONADO_TRABALHO');

-- A ficha do material biológico (versão 1): exposição, material, circunstância, proteção, vacina, fonte e conduta.
INSERT INTO public.tb_ficha_agravo (uuid, agravo_id, versao, definicao, criada_em)
SELECT gen_random_uuid(), a.uuid, 1, '{"campos": [{"chave": "tipoExposicao", "rotulo": "Tipo de exposição", "tipo": "MULTIPLA", "obrigatorio": true, "opcoes": ["Percutânea", "Mucosa (oral ou ocular)", "Pele não íntegra", "Pele íntegra"]}, {"chave": "materialOrganico", "rotulo": "Material orgânico", "tipo": "OPCAO", "obrigatorio": true, "opcoes": ["Sangue", "Fluido com sangue", "Soro ou plasma", "Líquor", "Líquido pleural", "Líquido ascítico", "Líquido amniótico", "Outro"]}, {"chave": "circunstancia", "rotulo": "Circunstância", "tipo": "OPCAO", "obrigatorio": false, "opcoes": ["Administração de medicação", "Punção venosa ou arterial", "Descarte inadequado de perfurocortante", "Lavagem de material", "Procedimento cirúrgico", "Procedimento odontológico", "Procedimento laboratorial", "Glicemia capilar", "Reencape", "Outra"]}, {"chave": "agente", "rotulo": "Agente", "tipo": "OPCAO", "obrigatorio": false, "opcoes": ["Agulha com lúmen", "Agulha sem lúmen", "Lâmina ou lanceta", "Vidro", "Outro"]}, {"chave": "protecao", "rotulo": "Proteção em uso", "tipo": "MULTIPLA", "obrigatorio": false, "opcoes": ["Luva", "Avental", "Óculos", "Máscara", "Protetor facial", "Bota"]}, {"chave": "vacinadoHepatiteB", "rotulo": "Vacinado contra hepatite B (3 doses)", "tipo": "SIM_NAO", "obrigatorio": true, "opcoes": null}, {"chave": "fonteConhecida", "rotulo": "Paciente-fonte conhecido", "tipo": "SIM_NAO", "obrigatorio": true, "opcoes": null}, {"chave": "conduta", "rotulo": "Conduta no atendimento", "tipo": "MULTIPLA", "obrigatorio": false, "opcoes": ["Sem indicação de profilaxia", "Profilaxia antirretroviral (PEP)", "Imunoglobulina contra hepatite B", "Vacina contra hepatite B", "Recusou a profilaxia"]}]}', now()
FROM public.tb_agravo a WHERE a.codigo = 'ACIDENTE_TRABALHO_MATERIAL_BIOLOGICO';

-- O bloco do trabalho, 1:1 com a notificação. A ocupação fica no evento (é dado da data do evento, não do cadastro).
CREATE TABLE public.tb_evento_trabalho (
    uuid uuid NOT NULL,
    notificacao_id uuid NOT NULL,
    ocupacao_cbo character varying(6) NOT NULL,
    ocupacao_descricao character varying(200) NOT NULL,
    situacao_trabalho character varying(255) NOT NULL,
    empregador_documento character varying(14),
    empregador_nome character varying(200),
    empregador_cnae character varying(7),
    estabelecimento_id uuid,
    local_evento character varying(500),
    latitude double precision,
    longitude double precision,
    tipo_acidente character varying(255),
    horas_apos_inicio_jornada integer,
    cat character varying(255),
    outros_trabalhadores_atingidos integer,
    obito boolean DEFAULT false NOT NULL,
    acidente_trabalho_id uuid,
    acompanhamento_seis_semanas date,
    acompanhamento_tres_meses date,
    acompanhamento_seis_meses date,
    registrado_em timestamp(6) with time zone NOT NULL,
    registrado_por_cpf character varying(255),
    atualizado_em timestamp(6) with time zone,
    atualizado_por_cpf character varying(255),
    CONSTRAINT tb_evento_trabalho_situacao_trabalho_check CHECK (((situacao_trabalho)::text = ANY ((ARRAY['EMPREGADO_REGISTRADO'::character varying, 'EMPREGADO_NAO_REGISTRADO'::character varying, 'SERVIDOR_ESTATUTARIO'::character varying, 'SERVIDOR_CELETISTA'::character varying, 'AUTONOMO'::character varying, 'EMPREGADOR'::character varying, 'TRABALHADOR_AVULSO'::character varying, 'COOPERADO'::character varying, 'ESTAGIARIO'::character varying, 'APOSENTADO'::character varying, 'DESEMPREGADO'::character varying, 'OUTRA'::character varying])::text[]))),
    CONSTRAINT tb_evento_trabalho_tipo_acidente_check CHECK (((tipo_acidente)::text = ANY ((ARRAY['TIPICO'::character varying, 'TRAJETO'::character varying])::text[]))),
    CONSTRAINT tb_evento_trabalho_cat_check CHECK (((cat)::text = ANY ((ARRAY['SIM'::character varying, 'NAO'::character varying, 'IGNORADO'::character varying])::text[]))),
    CONSTRAINT ck_evento_trabalho_horas CHECK ((horas_apos_inicio_jornada IS NULL) OR (horas_apos_inicio_jornada >= 0 AND horas_apos_inicio_jornada <= 24)),
    CONSTRAINT ck_evento_trabalho_atingidos CHECK ((outros_trabalhadores_atingidos IS NULL) OR (outros_trabalhadores_atingidos >= 0))
);
ALTER TABLE ONLY public.tb_evento_trabalho ADD CONSTRAINT tb_evento_trabalho_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_evento_trabalho ADD CONSTRAINT uk_evento_trabalho_notificacao UNIQUE (notificacao_id);
ALTER TABLE ONLY public.tb_evento_trabalho ADD CONSTRAINT uk_evento_trabalho_acidente UNIQUE (acidente_trabalho_id);
ALTER TABLE ONLY public.tb_evento_trabalho ADD CONSTRAINT fk_evento_trabalho_notificacao FOREIGN KEY (notificacao_id) REFERENCES public.tb_notificacao(uuid);
ALTER TABLE ONLY public.tb_evento_trabalho ADD CONSTRAINT fk_evento_trabalho_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES public.tb_estabelecimento_regulado(uuid);
ALTER TABLE ONLY public.tb_evento_trabalho ADD CONSTRAINT fk_evento_trabalho_acidente FOREIGN KEY (acidente_trabalho_id) REFERENCES public.tb_acidente_trabalho(uuid);
