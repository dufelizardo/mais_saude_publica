-- Vigilância em Saúde, fatia V3c (ADR-0201): a notificação de violência gera a comunicação obrigatória à rede intersetorial
-- (Conselho Tutelar pelo ECA; conselho da pessoa idosa, Ministério Público ou autoridade policial pelo Estatuto da Pessoa Idosa;
-- autoridade policial pela Lei 13.931/2019). A comunicação é um encaminhamento intersetorial (ADR-0154) com a Vigilância como origem.
-- Conferido contra o ddl-auto=validate (ADR-0090).

ALTER TABLE public.tb_encaminhamento_intersetorial DROP CONSTRAINT tb_encaminhamento_intersetorial_dominio_origem_check;
ALTER TABLE public.tb_encaminhamento_intersetorial ADD CONSTRAINT tb_encaminhamento_intersetorial_dominio_origem_check
    CHECK (dominio_origem IN ('SAUDE_MENTAL', 'EMERGENCIAS', 'VIGILANCIA'));

ALTER TABLE public.tb_instituicao_externa DROP CONSTRAINT tb_instituicao_externa_tipo_check;
ALTER TABLE public.tb_instituicao_externa ADD CONSTRAINT tb_instituicao_externa_tipo_check
    CHECK (tipo IN ('CRAS', 'CREAS', 'CENTRO_POP', 'ACOLHIMENTO_INSTITUCIONAL', 'ESCOLA', 'CONSELHO_TUTELAR', 'CONSELHO_DE_DIREITOS',
                    'DEFENSORIA_PUBLICA', 'MINISTERIO_PUBLICO', 'JUDICIARIO', 'SERVICO_DE_EMPREGO', 'HABITACAO', 'SEGURANCA_PUBLICA', 'OUTRO'));
