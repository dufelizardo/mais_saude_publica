-- Imunização, fatia I4 (ADR-0150): evento supostamente atribuível à vacinação ou imunização (ESAVI) e erro de imunização,
-- notificados pela Vigilância e ligados à dose; a contraindicação vacinal que sai do encerramento.

-- Os dois agravos, no catálogo da Vigilância (dado). O ESAVI grave é de comunicação imediata, como no PNI.
INSERT INTO public.tb_agravo (uuid, codigo, nome, descricao, compulsorio, periodicidade, prazo_encerramento_dias, sensivel, ativo)
SELECT gen_random_uuid(), v.codigo, v.nome, v.descricao, true, v.periodicidade, v.prazo, false, true
FROM (VALUES
    ('ESAVI', 'Evento supostamente atribuível à vacinação ou imunização (ESAVI)',
     'Evento adverso depois da vacina, ligado à dose aplicada; o grave é comunicado em até 24 horas.', 'IMEDIATA', 60),
    ('ERRO_IMUNIZACAO', 'Erro de imunização',
     'Vacina, idade, intervalo, via, dose, validade ou armazenamento fora do recomendado, com ou sem evento.', 'SEMANAL', 30)
) AS v(codigo, nome, descricao, periodicidade, prazo)
WHERE NOT EXISTS (SELECT 1 FROM public.tb_agravo a WHERE a.codigo = v.codigo);

INSERT INTO public.tb_agravo_criterio (agravo_id, criterio)
SELECT a.uuid, v.criterio FROM (VALUES ('ESAVI', 'CLINICO'), ('ESAVI', 'LABORATORIAL'), ('ERRO_IMUNIZACAO', 'CLINICO')) AS v(codigo, criterio)
JOIN public.tb_agravo a ON a.codigo = v.codigo;

INSERT INTO public.tb_agravo_cid (agravo_id, cid)
SELECT a.uuid, 'T881' FROM public.tb_agravo a WHERE a.codigo = 'ESAVI';

INSERT INTO public.tb_ficha_agravo (uuid, agravo_id, versao, definicao, criada_em)
SELECT gen_random_uuid(), a.uuid, 1, '{"campos": [{"chave": "manifestacoes", "rotulo": "Manifestações", "tipo": "MULTIPLA", "obrigatorio": true, "opcoes": ["Reação local", "Febre", "Convulsão", "Anafilaxia", "Episódio hipotônico-hiporresponsivo", "Choro persistente", "Síndrome de Guillain-Barré", "Encefalite ou encefalopatia", "Outra"]}, {"chave": "horasAteInicio", "rotulo": "Horas entre a vacina e o início", "tipo": "NUMERO", "obrigatorio": false, "opcoes": null}, {"chave": "hospitalizado", "rotulo": "Hospitalizado", "tipo": "SIM_NAO", "obrigatorio": false, "opcoes": null}]}', now()
FROM public.tb_agravo a WHERE a.codigo = 'ESAVI';

INSERT INTO public.tb_ficha_agravo (uuid, agravo_id, versao, definicao, criada_em)
SELECT gen_random_uuid(), a.uuid, 1, '{"campos": [{"chave": "tipoErro", "rotulo": "Tipo de erro", "tipo": "OPCAO", "obrigatorio": true, "opcoes": ["Imunobiológico errado", "Fora da idade", "Intervalo menor que o mínimo", "Via ou local errado", "Dose ou volume errado", "Vencido ou sob suspeita", "Armazenamento inadequado", "Outro"]}, {"chave": "houveEvento", "rotulo": "Houve evento adverso", "tipo": "SIM_NAO", "obrigatorio": true, "opcoes": null}]}', now()
FROM public.tb_agravo a WHERE a.codigo = 'ERRO_IMUNIZACAO';

-- A origem nova da notificação e os campos do ESAVI.
ALTER TABLE public.tb_notificacao DROP CONSTRAINT IF EXISTS tb_notificacao_origem_check;
ALTER TABLE public.tb_notificacao ADD CONSTRAINT tb_notificacao_origem_check CHECK (((origem)::text = ANY ((ARRAY['ATENDIMENTO'::character varying, 'INTERNACAO'::character varying, 'EXAME'::character varying, 'SAUDE_MENTAL'::character varying, 'VACINACAO'::character varying, 'BUSCA_ATIVA'::character varying, 'COMUNIDADE'::character varying, 'OUTRA'::character varying])::text[])));

ALTER TABLE public.tb_notificacao ADD COLUMN gravidade_esavi character varying(255);
ALTER TABLE public.tb_notificacao ADD COLUMN causalidade_esavi character varying(255);
ALTER TABLE public.tb_notificacao ADD COLUMN conduta_esavi character varying(255);
ALTER TABLE public.tb_notificacao ADD CONSTRAINT tb_notificacao_gravidade_esavi_check CHECK (((gravidade_esavi)::text = ANY ((ARRAY['NAO_GRAVE'::character varying, 'GRAVE'::character varying])::text[])));
ALTER TABLE public.tb_notificacao ADD CONSTRAINT tb_notificacao_causalidade_esavi_check CHECK (((causalidade_esavi)::text = ANY ((ARRAY['A1_REACAO_PRODUTO'::character varying, 'A2_DESVIO_QUALIDADE'::character varying, 'A3_ERRO_IMUNIZACAO'::character varying, 'A4_ANSIEDADE'::character varying, 'B_INDETERMINADO'::character varying, 'C_COINCIDENTE'::character varying, 'D_INCLASSIFICAVEL'::character varying])::text[])));
ALTER TABLE public.tb_notificacao ADD CONSTRAINT tb_notificacao_conduta_esavi_check CHECK (((conduta_esavi)::text = ANY ((ARRAY['MANTER'::character varying, 'PRECAUCAO_CRIE'::character varying, 'CONTRAINDICAR'::character varying])::text[])));

-- As doses suspeitas da notificação (a criança costuma tomar várias no mesmo dia).
CREATE TABLE public.tb_notificacao_dose (
    notificacao_id uuid NOT NULL,
    registro_vacinacao_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_notificacao_dose ADD CONSTRAINT tb_notificacao_dose_pkey PRIMARY KEY (notificacao_id, registro_vacinacao_id);
ALTER TABLE ONLY public.tb_notificacao_dose ADD CONSTRAINT fk_notificacao_dose_notificacao FOREIGN KEY (notificacao_id) REFERENCES public.tb_notificacao(uuid);
ALTER TABLE ONLY public.tb_notificacao_dose ADD CONSTRAINT fk_notificacao_dose_registro FOREIGN KEY (registro_vacinacao_id) REFERENCES public.tb_registro_vacinacao(uuid);
CREATE INDEX ix_notificacao_dose_registro ON public.tb_notificacao_dose USING btree (registro_vacinacao_id);

-- A contraindicação vacinal: a conduta "contraindicar" do ESAVI encerrado, por paciente e imunobiológico.
CREATE TABLE public.tb_contraindicacao_vacinal (
    desde date NOT NULL,
    registrada_em timestamp(6) with time zone NOT NULL,
    imunobiologico_id uuid NOT NULL,
    notificacao_id uuid,
    paciente_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo character varying(500) NOT NULL,
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_contraindicacao_vacinal ADD CONSTRAINT tb_contraindicacao_vacinal_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_contraindicacao_vacinal ADD CONSTRAINT uk_contraindicacao_paciente_imunobiologico UNIQUE (paciente_id, imunobiologico_id);
ALTER TABLE ONLY public.tb_contraindicacao_vacinal ADD CONSTRAINT fk_contraindicacao_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_contraindicacao_vacinal ADD CONSTRAINT fk_contraindicacao_imunobiologico FOREIGN KEY (imunobiologico_id) REFERENCES public.tb_imunobiologico(uuid);
ALTER TABLE ONLY public.tb_contraindicacao_vacinal ADD CONSTRAINT fk_contraindicacao_notificacao FOREIGN KEY (notificacao_id) REFERENCES public.tb_notificacao(uuid);
