-- Painel da rede intersetorial (IS2, ADR-0211): os números saem das tabelas da IS1. O índice serve a lista de cobrança
-- (os encaminhamentos em aberto de uma instituição) e o painel, que junta os em aberto de qualquer data.
CREATE INDEX ix_encaminhamento_intersetorial_instituicao ON public.tb_encaminhamento_intersetorial USING btree (instituicao_id, situacao);
