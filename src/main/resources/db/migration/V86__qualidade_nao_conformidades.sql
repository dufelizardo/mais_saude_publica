-- Qualidade, fatia Q2 (ADR-0204, desenho na ADR-0202): a não conformidade com a origem (incidente da Q1, irregularidade da VISA numa
-- unidade pública, auditoria interna, reclamação), a análise de causa (6M), o plano de ação 5W2H por ciclo e a verificação de eficácia
-- (PDCA). A permissão QUALIDADE.GERIR entra pelo CatalogoDeAcesso. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_nao_conformidade (
    ciclo integer NOT NULL,
    encerrada_em date,
    identificada_em date NOT NULL,
    verificacao_prevista date,
    registrado_em timestamp(6) with time zone NOT NULL,
    incidente_id uuid,
    irregularidade_id uuid,
    responsavel_id uuid NOT NULL,
    setor_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    numero character varying(20) NOT NULL,
    categorias_causa character varying(200),
    origem character varying(255) NOT NULL,
    gravidade character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    motivo_cancelamento character varying(1000),
    correcao_imediata character varying(2000),
    descricao character varying(4000) NOT NULL,
    analise_causa character varying(4000),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_nao_conformidade_origem_check CHECK (((origem)::text = ANY ((ARRAY['INCIDENTE'::character varying, 'INSPECAO_SANITARIA'::character varying, 'AUDITORIA_INTERNA'::character varying, 'RECLAMACAO'::character varying, 'INDICADOR'::character varying, 'OUTRA'::character varying])::text[]))),
    CONSTRAINT tb_nao_conformidade_gravidade_check CHECK (((gravidade)::text = ANY ((ARRAY['MENOR'::character varying, 'MAIOR'::character varying, 'CRITICA'::character varying])::text[]))),
    CONSTRAINT tb_nao_conformidade_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ABERTA'::character varying, 'EM_TRATAMENTO'::character varying, 'EM_VERIFICACAO'::character varying, 'ENCERRADA'::character varying, 'CANCELADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_nao_conformidade ADD CONSTRAINT tb_nao_conformidade_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_nao_conformidade ADD CONSTRAINT uk_nao_conformidade_numero UNIQUE (numero);
CREATE INDEX ix_nao_conformidade_unidade ON public.tb_nao_conformidade USING btree (unidade_id, situacao);
ALTER TABLE ONLY public.tb_nao_conformidade ADD CONSTRAINT fk_nao_conformidade_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_nao_conformidade ADD CONSTRAINT fk_nao_conformidade_setor FOREIGN KEY (setor_id) REFERENCES public.tb_setor(uuid);
ALTER TABLE ONLY public.tb_nao_conformidade ADD CONSTRAINT fk_nao_conformidade_responsavel FOREIGN KEY (responsavel_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_nao_conformidade ADD CONSTRAINT fk_nao_conformidade_incidente FOREIGN KEY (incidente_id) REFERENCES public.tb_incidente_seguranca(uuid);
ALTER TABLE ONLY public.tb_nao_conformidade ADD CONSTRAINT fk_nao_conformidade_irregularidade FOREIGN KEY (irregularidade_id) REFERENCES public.tb_irregularidade_sanitaria(uuid);

CREATE TABLE public.tb_acao_qualidade (
    ciclo integer NOT NULL,
    custo numeric(14,2),
    concluida_em date,
    prazo date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    nao_conformidade_id uuid NOT NULL,
    responsavel_id uuid NOT NULL,
    uuid uuid NOT NULL,
    onde character varying(200),
    situacao character varying(255) NOT NULL,
    o_que character varying(500) NOT NULL,
    por_que character varying(500) NOT NULL,
    motivo_cancelamento character varying(500),
    como character varying(1000),
    evidencia character varying(2000),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_acao_qualidade_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['PENDENTE'::character varying, 'EM_ANDAMENTO'::character varying, 'CONCLUIDA'::character varying, 'CANCELADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_acao_qualidade ADD CONSTRAINT tb_acao_qualidade_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_acao_qualidade_nc ON public.tb_acao_qualidade USING btree (nao_conformidade_id, ciclo);
ALTER TABLE ONLY public.tb_acao_qualidade ADD CONSTRAINT fk_acao_qualidade_nc FOREIGN KEY (nao_conformidade_id) REFERENCES public.tb_nao_conformidade(uuid);
ALTER TABLE ONLY public.tb_acao_qualidade ADD CONSTRAINT fk_acao_qualidade_responsavel FOREIGN KEY (responsavel_id) REFERENCES public.tb_profissional(uuid);

CREATE TABLE public.tb_verificacao_eficacia (
    ciclo integer NOT NULL,
    eficaz boolean NOT NULL,
    verificada_em date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    nao_conformidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    observacao character varying(2000),
    verificado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_verificacao_eficacia ADD CONSTRAINT tb_verificacao_eficacia_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_verificacao_eficacia ADD CONSTRAINT fk_verificacao_eficacia_nc FOREIGN KEY (nao_conformidade_id) REFERENCES public.tb_nao_conformidade(uuid);
