-- Indicadores e BI, fatia BI2 (ADR-0144): a hora de chegada do paciente, gravada pelo sistema quando o atendimento passa
-- a "em andamento". Sem preenchimento retroativo: a chegada dos atendimentos antigos não é conhecida.
ALTER TABLE public.tb_atendimento ADD COLUMN chegada_em timestamp(6) without time zone;
CREATE INDEX ix_atendimento_chegada ON public.tb_atendimento USING btree (chegada_em);
