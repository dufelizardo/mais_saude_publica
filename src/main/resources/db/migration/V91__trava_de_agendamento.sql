-- Resiliência do deploy, R1 (ADR-0209): com mais de uma réplica da API, cada tarefa agendada roda em uma só. A réplica que
-- consegue marcar a trava (travada_ate no passado) roda; as outras pulam. Conferido contra o ddl-auto=validate (ADR-0090).
CREATE TABLE public.tb_trava_agendamento (
    nome character varying(100) NOT NULL,
    travada_ate timestamp(6) with time zone NOT NULL,
    travada_em timestamp(6) with time zone,
    travada_por character varying(255)
);
ALTER TABLE ONLY public.tb_trava_agendamento ADD CONSTRAINT tb_trava_agendamento_pkey PRIMARY KEY (nome);
