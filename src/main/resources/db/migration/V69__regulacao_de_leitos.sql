-- Regulação de leitos entre unidades, fatia RL1 dos domínios 11 e 12 (ADR-0184): a faixa etária e a reserva no leito, o pedido de
-- leito à central e os eventos. Os leitos que já existem recebem a faixa pelo tipo. Conferido contra o ddl-auto=validate (ADR-0090).

ALTER TABLE public.tb_leito ADD COLUMN faixa_etaria character varying(255);
UPDATE public.tb_leito SET faixa_etaria = CASE tipo WHEN 'UTI_NEONATAL' THEN 'NEONATAL'
                                                    WHEN 'PEDIATRICO' THEN 'PEDIATRICO'
                                                    WHEN 'UTI_PEDIATRICA' THEN 'PEDIATRICO'
                                                    ELSE 'ADULTO' END;
ALTER TABLE public.tb_leito ALTER COLUMN faixa_etaria SET NOT NULL;
ALTER TABLE public.tb_leito ADD CONSTRAINT tb_leito_faixa_etaria_check CHECK (((faixa_etaria)::text = ANY ((ARRAY['ADULTO'::character varying, 'PEDIATRICO'::character varying, 'NEONATAL'::character varying])::text[])));
ALTER TABLE public.tb_leito ADD COLUMN reservado_ate timestamp(6) with time zone;
ALTER TABLE public.tb_leito ADD COLUMN reserva_solicitacao_id uuid;
ALTER TABLE public.tb_leito DROP CONSTRAINT tb_leito_situacao_check;
ALTER TABLE public.tb_leito ADD CONSTRAINT tb_leito_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['LIVRE'::character varying, 'OCUPADO'::character varying, 'HIGIENIZACAO'::character varying, 'BLOQUEADO'::character varying, 'RESERVADO'::character varying])::text[])));

CREATE TABLE public.tb_solicitacao_leito (
    vaga_zero boolean NOT NULL,
    reservado_ate timestamp(6) with time zone,
    solicitada_em timestamp(6) with time zone NOT NULL,
    atendimento_id uuid,
    internacao_destino_id uuid,
    internacao_origem_id uuid,
    leito_reservado_id uuid,
    ocorrencia_urgencia_id uuid,
    paciente_id uuid NOT NULL,
    solicitado_por_id uuid NOT NULL,
    unidade_destino_id uuid,
    unidade_origem_id uuid NOT NULL,
    uuid uuid NOT NULL,
    cid character varying(10) NOT NULL,
    transporte character varying(300),
    justificativa_vaga_zero character varying(1000),
    motivo_encerramento character varying(1000),
    quadro_clinico character varying(2000) NOT NULL,
    prioridade character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    status character varying(255) NOT NULL,
    tipo_leito character varying(255) NOT NULL,
    CONSTRAINT tb_solicitacao_leito_prioridade_check CHECK (((prioridade)::text = ANY ((ARRAY['VERMELHO'::character varying, 'AMARELO'::character varying, 'VERDE'::character varying, 'AZUL'::character varying])::text[]))),
    CONSTRAINT tb_solicitacao_leito_status_check CHECK (((status)::text = ANY ((ARRAY['SOLICITADA'::character varying, 'RESERVADA'::character varying, 'ADMITIDA'::character varying, 'NEGADA'::character varying, 'CANCELADA'::character varying])::text[]))),
    CONSTRAINT tb_solicitacao_leito_tipo_leito_check CHECK (((tipo_leito)::text = ANY ((ARRAY['CLINICO'::character varying, 'CIRURGICO'::character varying, 'PEDIATRICO'::character varying, 'OBSTETRICO'::character varying, 'UTI_ADULTO'::character varying, 'UTI_PEDIATRICA'::character varying, 'UTI_NEONATAL'::character varying, 'OBSERVACAO'::character varying, 'ISOLAMENTO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_solicitacao_leito ADD CONSTRAINT tb_solicitacao_leito_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_solicitacao_leito ADD CONSTRAINT fk_solicitacao_leito_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_solicitacao_leito ADD CONSTRAINT fk_solicitacao_leito_origem FOREIGN KEY (unidade_origem_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_solicitacao_leito ADD CONSTRAINT fk_solicitacao_leito_destino FOREIGN KEY (unidade_destino_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_solicitacao_leito ADD CONSTRAINT fk_solicitacao_leito_internacao_origem FOREIGN KEY (internacao_origem_id) REFERENCES public.tb_internacao(uuid);
ALTER TABLE ONLY public.tb_solicitacao_leito ADD CONSTRAINT fk_solicitacao_leito_internacao_destino FOREIGN KEY (internacao_destino_id) REFERENCES public.tb_internacao(uuid);
ALTER TABLE ONLY public.tb_solicitacao_leito ADD CONSTRAINT fk_solicitacao_leito_atendimento FOREIGN KEY (atendimento_id) REFERENCES public.tb_atendimento(uuid);
ALTER TABLE ONLY public.tb_solicitacao_leito ADD CONSTRAINT fk_solicitacao_leito_leito FOREIGN KEY (leito_reservado_id) REFERENCES public.tb_leito(uuid);
ALTER TABLE ONLY public.tb_solicitacao_leito ADD CONSTRAINT fk_solicitacao_leito_solicitante FOREIGN KEY (solicitado_por_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_solicitacao_leito_status ON public.tb_solicitacao_leito USING btree (status, solicitada_em);
CREATE INDEX ix_solicitacao_leito_paciente ON public.tb_solicitacao_leito USING btree (paciente_id);

CREATE TABLE public.tb_evento_solicitacao_leito (
    ocorrido_em timestamp(6) with time zone NOT NULL,
    profissional_id uuid,
    solicitacao_id uuid NOT NULL,
    uuid uuid NOT NULL,
    texto character varying(1000),
    registrado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_evento_solicitacao_leito_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['SOLICITACAO'::character varying, 'RESERVA'::character varying, 'VAGA_ZERO'::character varying, 'EXPIRACAO'::character varying, 'RECUSA_DESTINO'::character varying, 'NEGACAO'::character varying, 'CANCELAMENTO'::character varying, 'ADMISSAO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_evento_solicitacao_leito ADD CONSTRAINT tb_evento_solicitacao_leito_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_evento_solicitacao_leito ADD CONSTRAINT fk_evento_solicitacao_leito FOREIGN KEY (solicitacao_id) REFERENCES public.tb_solicitacao_leito(uuid);
ALTER TABLE ONLY public.tb_evento_solicitacao_leito ADD CONSTRAINT fk_evento_solicitacao_leito_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_evento_solicitacao_leito ON public.tb_evento_solicitacao_leito USING btree (solicitacao_id, ocorrido_em);
