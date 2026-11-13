-- Comunicação e Educação em Saúde, fatia C6 (ADR-0196): as metas da campanha de comunicação e a avaliação registrada, que congela os
-- números da apuração no momento do registro. A apuração em si é calculada, sem tabela. Conferido contra o ddl-auto=validate (ADR-0090).

ALTER TABLE public.tb_campanha_comunicacao ADD COLUMN meta_pessoas integer;
ALTER TABLE public.tb_campanha_comunicacao ADD COLUMN meta_acoes integer;

CREATE TABLE public.tb_avaliacao_campanha (
    acoes_realizadas integer NOT NULL,
    enviados integer NOT NULL,
    pessoas_alcancadas integer NOT NULL,
    cobertura_percentual numeric(5,1),
    atualizada_em timestamp(6) with time zone NOT NULL,
    registrada_em timestamp(6) with time zone NOT NULL,
    uuid uuid NOT NULL,
    campanha_id uuid NOT NULL,
    publico_atingido character varying(10) NOT NULL,
    situacao_meta_acoes character varying(15) NOT NULL,
    situacao_meta_pessoas character varying(15) NOT NULL,
    conclusoes character varying(2000) NOT NULL,
    recomendacoes character varying(2000),
    resultados character varying(2000),
    atualizada_por_cpf character varying(255),
    registrada_por_cpf character varying(255),
    CONSTRAINT tb_avaliacao_campanha_publico_atingido_check CHECK (publico_atingido IN ('SIM', 'PARCIAL', 'NAO')),
    CONSTRAINT tb_avaliacao_campanha_situacao_meta_acoes_check CHECK (situacao_meta_acoes IN ('ATINGIDA', 'EM_ANDAMENTO', 'ABAIXO', 'SEM_META')),
    CONSTRAINT tb_avaliacao_campanha_situacao_meta_pessoas_check CHECK (situacao_meta_pessoas IN ('ATINGIDA', 'EM_ANDAMENTO', 'ABAIXO', 'SEM_META'))
);
ALTER TABLE ONLY public.tb_avaliacao_campanha ADD CONSTRAINT tb_avaliacao_campanha_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_avaliacao_campanha ADD CONSTRAINT uk_avaliacao_campanha_campanha UNIQUE (campanha_id);
ALTER TABLE ONLY public.tb_avaliacao_campanha ADD CONSTRAINT fk_avaliacao_campanha_campanha FOREIGN KEY (campanha_id) REFERENCES public.tb_campanha_comunicacao(uuid);
