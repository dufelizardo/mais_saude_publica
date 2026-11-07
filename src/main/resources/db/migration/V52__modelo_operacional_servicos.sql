-- Modelo Operacional, fatia MO2 (ADR-0165, desenho na ADR-0115): o catálogo de tipos de serviço e os serviços da unidade,
-- ligados aos setores. A carga aponta a capacidade exigida pelo código; o município revisa na tela Modelo operacional.
-- Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_tipo_servico (
    ativo boolean NOT NULL,
    capacidade_id uuid,
    uuid uuid NOT NULL,
    codigo character varying(60) NOT NULL,
    nome character varying(100) NOT NULL,
    descricao character varying(500)
);
ALTER TABLE ONLY public.tb_tipo_servico ADD CONSTRAINT tb_tipo_servico_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_tipo_servico ADD CONSTRAINT uk_tipo_servico_codigo UNIQUE (codigo);
ALTER TABLE ONLY public.tb_tipo_servico ADD CONSTRAINT fk_tipo_servico_capacidade FOREIGN KEY (capacidade_id) REFERENCES public.tb_capacidade_assistencial(uuid);

CREATE TABLE public.tb_servico_unidade (
    vinte_quatro_horas boolean NOT NULL,
    fim date,
    inicio date NOT NULL,
    responsavel_id uuid,
    tipo_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    nome character varying(150) NOT NULL,
    horario character varying(200),
    motivo_fim character varying(500),
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_servico_unidade ADD CONSTRAINT tb_servico_unidade_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_servico_unidade_unidade ON public.tb_servico_unidade USING btree (unidade_id);
ALTER TABLE ONLY public.tb_servico_unidade ADD CONSTRAINT fk_servico_unidade_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_servico_unidade ADD CONSTRAINT fk_servico_unidade_tipo FOREIGN KEY (tipo_id) REFERENCES public.tb_tipo_servico(uuid);
ALTER TABLE ONLY public.tb_servico_unidade ADD CONSTRAINT fk_servico_unidade_responsavel FOREIGN KEY (responsavel_id) REFERENCES public.tb_profissional(uuid);

CREATE TABLE public.tb_servico_setor (
    servico_id uuid NOT NULL,
    setor_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_servico_setor ADD CONSTRAINT tb_servico_setor_pkey PRIMARY KEY (servico_id, setor_id);
ALTER TABLE ONLY public.tb_servico_setor ADD CONSTRAINT fk_servico_setor_servico FOREIGN KEY (servico_id) REFERENCES public.tb_servico_unidade(uuid);
ALTER TABLE ONLY public.tb_servico_setor ADD CONSTRAINT fk_servico_setor_setor FOREIGN KEY (setor_id) REFERENCES public.tb_setor(uuid);

INSERT INTO public.tb_tipo_servico (uuid, codigo, nome, ativo, capacidade_id)
SELECT gen_random_uuid(), v.codigo, v.nome, true, c.uuid
FROM (VALUES
    ('PRONTO_SOCORRO', 'Pronto-socorro', 'URGENCIA'),
    ('UTI', 'Unidade de terapia intensiva', 'UTI'),
    ('INTERNACAO_CLINICA', 'Internação clínica', 'INTERNACAO_CLINICA'),
    ('CENTRO_CIRURGICO', 'Centro cirúrgico', 'CIRURGIA'),
    ('CENTRO_OBSTETRICO', 'Centro obstétrico', 'OBSTETRICIA'),
    ('SALA_DE_VACINA', 'Sala de vacina', NULL),
    ('AMBULATORIO', 'Ambulatório de especialidades', 'CONSULTA_ESPECIALIZADA'),
    ('ODONTOLOGIA', 'Odontologia', 'ODONTO_DIAGNOSTICO_BUCAL'),
    ('FARMACIA', 'Farmácia', 'DISPENSACAO'),
    ('LABORATORIO', 'Laboratório', 'ANALISES_CLINICAS'),
    ('DIAGNOSTICO_POR_IMAGEM', 'Diagnóstico por imagem', 'DIAGNOSTICO_POR_IMAGEM')
) AS v(codigo, nome, capacidade)
LEFT JOIN public.tb_capacidade_assistencial c ON c.codigo = v.capacidade;
