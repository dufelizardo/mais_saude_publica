-- Vigilância epidemiológica, fatia V2a (ADR-0130): o local do caso na notificação (ponto, microárea e de onde vem), os
-- contatos do caso com o acompanhamento e o período de monitoramento do contato no catálogo de agravos. Conferido contra
-- o ddl-auto=validate (ADR-0090).

ALTER TABLE public.tb_notificacao ADD COLUMN latitude double precision;
ALTER TABLE public.tb_notificacao ADD COLUMN longitude double precision;
ALTER TABLE public.tb_notificacao ADD COLUMN microarea_id uuid;
ALTER TABLE public.tb_notificacao ADD COLUMN origem_local character varying(255);
ALTER TABLE public.tb_notificacao ADD CONSTRAINT tb_notificacao_origem_local_check
    CHECK (((origem_local)::text = ANY ((ARRAY['DOMICILIO'::character varying, 'MARCADO'::character varying])::text[])));
ALTER TABLE ONLY public.tb_notificacao
    ADD CONSTRAINT fk_notificacao_microarea FOREIGN KEY (microarea_id) REFERENCES public.tb_microarea(uuid);
CREATE INDEX ix_notificacao_microarea ON public.tb_notificacao USING btree (microarea_id);

ALTER TABLE public.tb_agravo ADD COLUMN periodo_monitoramento_contato_dias integer;
-- Monitoramento dos contatos: sarampo 21 dias, coqueluche 14, doença meningocócica 10.
UPDATE public.tb_agravo SET periodo_monitoramento_contato_dias = 21 WHERE codigo = 'SARAMPO';
UPDATE public.tb_agravo SET periodo_monitoramento_contato_dias = 14 WHERE codigo = 'COQUELUCHE';
UPDATE public.tb_agravo SET periodo_monitoramento_contato_dias = 10 WHERE codigo = 'MENINGITE';

CREATE TABLE public.tb_contato_epidemiologico (
    data_exposicao date,
    fim_monitoramento date,
    atualizado_em timestamp(6) with time zone NOT NULL,
    notificacao_gerada_id uuid,
    notificacao_id uuid NOT NULL,
    paciente_id uuid,
    uuid uuid NOT NULL,
    telefone character varying(40),
    nome character varying(200),
    endereco character varying(500),
    observacao character varying(1000),
    registrado_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    tipo_exposicao character varying(255) NOT NULL,
    CONSTRAINT tb_contato_epidemiologico_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['EM_ACOMPANHAMENTO'::character varying, 'LIBERADO'::character varying, 'SINTOMATICO'::character varying, 'VIROU_CASO'::character varying, 'PERDA_DE_SEGUIMENTO'::character varying])::text[]))),
    CONSTRAINT tb_contato_epidemiologico_tipo_exposicao_check CHECK (((tipo_exposicao)::text = ANY ((ARRAY['DOMICILIAR'::character varying, 'SEXUAL'::character varying, 'TRABALHO'::character varying, 'ESCOLA'::character varying, 'SERVICO_SAUDE'::character varying, 'COMUNITARIO'::character varying, 'OUTRO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_contato_epidemiologico ADD CONSTRAINT tb_contato_epidemiologico_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_contato_notificacao ON public.tb_contato_epidemiologico USING btree (notificacao_id);
ALTER TABLE ONLY public.tb_contato_epidemiologico
    ADD CONSTRAINT fk_contato_notificacao FOREIGN KEY (notificacao_id) REFERENCES public.tb_notificacao(uuid);
ALTER TABLE ONLY public.tb_contato_epidemiologico
    ADD CONSTRAINT fk_contato_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_contato_epidemiologico
    ADD CONSTRAINT fk_contato_notificacao_gerada FOREIGN KEY (notificacao_gerada_id) REFERENCES public.tb_notificacao(uuid);
