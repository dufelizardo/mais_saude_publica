-- Educação permanente, fatia C4 (ADR-0173): catálogo de competências e as ligações do treinamento com competências e conteúdos.
-- Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_competencia (
    ativo boolean NOT NULL,
    uuid uuid NOT NULL,
    codigo character varying(60) NOT NULL,
    nome character varying(120) NOT NULL,
    descricao character varying(500)
);
ALTER TABLE ONLY public.tb_competencia ADD CONSTRAINT tb_competencia_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_competencia ADD CONSTRAINT uk_competencia_codigo UNIQUE (codigo);

CREATE TABLE public.tb_treinamento_competencia (
    treinamento_id uuid NOT NULL,
    competencia_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_treinamento_competencia ADD CONSTRAINT tb_treinamento_competencia_pkey PRIMARY KEY (treinamento_id, competencia_id);
ALTER TABLE ONLY public.tb_treinamento_competencia ADD CONSTRAINT fk_treinamento_competencia_treinamento FOREIGN KEY (treinamento_id) REFERENCES public.tb_treinamento(uuid);
ALTER TABLE ONLY public.tb_treinamento_competencia ADD CONSTRAINT fk_treinamento_competencia_competencia FOREIGN KEY (competencia_id) REFERENCES public.tb_competencia(uuid);

-- O conteúdo entra pelo grupo de versões, que não é tabela: o treinamento mostra sempre a versão em vigor.
CREATE TABLE public.tb_treinamento_conteudo (
    treinamento_id uuid NOT NULL,
    conteudo_grupo_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_treinamento_conteudo ADD CONSTRAINT tb_treinamento_conteudo_pkey PRIMARY KEY (treinamento_id, conteudo_grupo_id);
ALTER TABLE ONLY public.tb_treinamento_conteudo ADD CONSTRAINT fk_treinamento_conteudo_treinamento FOREIGN KEY (treinamento_id) REFERENCES public.tb_treinamento(uuid);

INSERT INTO public.tb_competencia (uuid, codigo, nome, descricao, ativo) VALUES
    (gen_random_uuid(), 'ACOLHIMENTO_CLASSIFICACAO_RISCO', 'Acolhimento e classificação de risco', 'Escuta qualificada e priorização pelo risco (Política Nacional de Humanização).', true),
    (gen_random_uuid(), 'BIOSSEGURANCA', 'Biossegurança e controle de infecção', 'Precauções, EPI, higiene das mãos e resíduos (NR-32).', true),
    (gen_random_uuid(), 'SUPORTE_BASICO_VIDA', 'Suporte básico de vida', 'Reconhecimento da parada e reanimação até a chegada do suporte avançado.', true),
    (gen_random_uuid(), 'VIGILANCIA_NOTIFICACAO', 'Vigilância e notificação de agravos', 'Agravos de notificação compulsória e o fluxo da notificação.', true),
    (gen_random_uuid(), 'IMUNIZACAO_SALA_VACINA', 'Imunização e sala de vacina', 'Rede de frio, calendário, aplicação segura e registro.', true),
    (gen_random_uuid(), 'TRABALHO_EM_EQUIPE', 'Trabalho em equipe e comunicação', 'Comunicação entre profissionais, passagem de caso e com o usuário.', true),
    (gen_random_uuid(), 'GESTAO_DO_CUIDADO', 'Gestão do cuidado e linhas de cuidado', 'Acompanhamento longitudinal, plano de cuidado e coordenação na rede.', true);
