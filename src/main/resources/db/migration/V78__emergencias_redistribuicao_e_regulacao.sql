-- Emergências, fatia E4b (ADR-0194): o medicamento do catálogo na mobilização (para o "onde tem" na rede) e a marca da
-- emergência na solicitação regulada (prioridade dentro da mesma cor). Conferido contra o ddl-auto=validate (ADR-0090).

ALTER TABLE public.tb_mobilizacao ADD COLUMN medicamento_id uuid;
ALTER TABLE ONLY public.tb_mobilizacao ADD CONSTRAINT fk_mobilizacao_medicamento FOREIGN KEY (medicamento_id) REFERENCES public.tb_medicamento(uuid);

ALTER TABLE public.tb_solicitacao_regulacao ADD COLUMN emergencia_id uuid;
ALTER TABLE ONLY public.tb_solicitacao_regulacao ADD CONSTRAINT fk_solicitacao_regulacao_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);
CREATE INDEX ix_solicitacao_regulacao_emergencia ON public.tb_solicitacao_regulacao USING btree (emergencia_id);
