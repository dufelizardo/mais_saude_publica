-- Mudanças de esquema entre a v1.4.0 e a entrada do Flyway (ADR-0090):
--   - ação EXPORTACAO na trilha de auditoria (ADR-0082);
--   - Central de Regulação do Acesso (ADRs 0087 e 0089).
-- Idempotente: o ambiente dev já tinha estas tabelas, criadas pelo ddl-auto antes do Flyway; os demais
-- estão na v1.4.0 e as recebem aqui.

-- ── Auditoria: EXPORTACAO ───────────────────────────────────────────────────────────────────────────
ALTER TABLE public.tb_evento_auditoria DROP CONSTRAINT IF EXISTS tb_evento_auditoria_acao_check;
ALTER TABLE public.tb_evento_auditoria ADD CONSTRAINT tb_evento_auditoria_acao_check CHECK (((acao)::text = ANY ((ARRAY[
    'LOGIN'::character varying, 'TROCA_DE_SENHA'::character varying, 'RECUPERACAO_DE_SENHA'::character varying,
    'LEITURA'::character varying, 'CRIACAO'::character varying, 'ALTERACAO'::character varying,
    'RETIFICACAO'::character varying, 'REVOGACAO'::character varying, 'ACESSO_JUSTIFICADO'::character varying,
    'EXPORTACAO'::character varying, 'EXCLUSAO'::character varying])::text[])));

-- ── Regulação ───────────────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS public.tb_procedimento_regulado (
    uuid uuid NOT NULL,
    ativo boolean NOT NULL,
    nome character varying(200) NOT NULL,
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_procedimento_regulado_pkey PRIMARY KEY (uuid),
    CONSTRAINT uk62gdu4nchj5pmm89kb870n98y UNIQUE (nome),
    CONSTRAINT tb_procedimento_regulado_tipo_check CHECK (((tipo)::text = ANY ((ARRAY[
        'CONSULTA_ESPECIALIZADA'::character varying, 'EXAME'::character varying, 'PROCEDIMENTO'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.tb_solicitacao_regulacao (
    uuid uuid NOT NULL,
    cid character varying(8) NOT NULL,
    concluido_em timestamp(6) with time zone,
    contrarreferencia character varying(4000),
    data_hora_prevista timestamp(6) without time zone,
    justificativa character varying(4000) NOT NULL,
    prioridade character varying(255) NOT NULL,
    solicitado_em timestamp(6) with time zone NOT NULL,
    solicitado_por_cpf character varying(255),
    status character varying(255) NOT NULL,
    agendamento_id uuid,
    paciente_id uuid NOT NULL,
    procedimento_id uuid NOT NULL,
    profissional_solicitante_id uuid NOT NULL,
    unidade_executante_id uuid,
    unidade_solicitante_id uuid NOT NULL,
    CONSTRAINT tb_solicitacao_regulacao_pkey PRIMARY KEY (uuid),
    CONSTRAINT tb_solicitacao_regulacao_prioridade_check CHECK (((prioridade)::text = ANY ((ARRAY[
        'VERMELHO'::character varying, 'AMARELO'::character varying, 'VERDE'::character varying, 'AZUL'::character varying])::text[]))),
    CONSTRAINT tb_solicitacao_regulacao_status_check CHECK (((status)::text = ANY ((ARRAY[
        'SOLICITADA'::character varying, 'DEVOLVIDA'::character varying, 'AUTORIZADA'::character varying,
        'NEGADA'::character varying, 'AGENDADA'::character varying, 'REALIZADA'::character varying,
        'FALTOU'::character varying, 'CANCELADA'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.tb_evento_regulacao (
    uuid uuid NOT NULL,
    ocorrido_em timestamp(6) with time zone NOT NULL,
    prioridade character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    status_resultante character varying(255) NOT NULL,
    texto character varying(4000),
    tipo character varying(255) NOT NULL,
    profissional_id uuid NOT NULL,
    solicitacao_id uuid NOT NULL,
    CONSTRAINT tb_evento_regulacao_pkey PRIMARY KEY (uuid),
    CONSTRAINT tb_evento_regulacao_prioridade_check CHECK (((prioridade)::text = ANY ((ARRAY[
        'VERMELHO'::character varying, 'AMARELO'::character varying, 'VERDE'::character varying, 'AZUL'::character varying])::text[]))),
    CONSTRAINT tb_evento_regulacao_status_resultante_check CHECK (((status_resultante)::text = ANY ((ARRAY[
        'SOLICITADA'::character varying, 'DEVOLVIDA'::character varying, 'AUTORIZADA'::character varying,
        'NEGADA'::character varying, 'AGENDADA'::character varying, 'REALIZADA'::character varying,
        'FALTOU'::character varying, 'CANCELADA'::character varying])::text[]))),
    CONSTRAINT tb_evento_regulacao_tipo_check CHECK (((tipo)::text = ANY ((ARRAY[
        'SOLICITACAO'::character varying, 'COMPLEMENTO'::character varying, 'RECLASSIFICACAO'::character varying,
        'AUTORIZACAO'::character varying, 'DEVOLUCAO'::character varying, 'NEGATIVA'::character varying,
        'CANCELAMENTO'::character varying, 'AGENDAMENTO'::character varying, 'REALIZACAO'::character varying,
        'FALTA'::character varying])::text[])))
);

-- Chaves estrangeiras: só cria a que ainda não existe (no dev, o ddl-auto já as criou com estes nomes).
DO $$
DECLARE
    fk record;
BEGIN
    FOR fk IN SELECT * FROM (VALUES
        ('tb_evento_regulacao', 'fk26wdy0876j6ad77ukyheerect', 'solicitacao_id', 'tb_solicitacao_regulacao'),
        ('tb_evento_regulacao', 'fkpfh1bhhvr50uv2n0rvw5t97wd', 'profissional_id', 'tb_profissional'),
        ('tb_solicitacao_regulacao', 'fk3xjwx5dw5lhdapc84k3hxf0w', 'agendamento_id', 'tb_agendamento'),
        ('tb_solicitacao_regulacao', 'fkc7uan76koqggw22sdrojcek1d', 'procedimento_id', 'tb_procedimento_regulado'),
        ('tb_solicitacao_regulacao', 'fkh59yrr0d2m4xeu6bx540slj91', 'paciente_id', 'tb_paciente'),
        ('tb_solicitacao_regulacao', 'fkhp07l7uh28t2w8pslr030wt2i', 'profissional_solicitante_id', 'tb_profissional'),
        ('tb_solicitacao_regulacao', 'fkocjkfomj0bixregmwppo8va7m', 'unidade_executante_id', 'tb_unidade_de_saude'),
        ('tb_solicitacao_regulacao', 'fkrhr4ojm8baf1m565ro3av89d0', 'unidade_solicitante_id', 'tb_unidade_de_saude')
    ) AS t(tabela, nome, coluna, referencia)
    LOOP
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = fk.nome) THEN
            EXECUTE format('ALTER TABLE public.%I ADD CONSTRAINT %I FOREIGN KEY (%I) REFERENCES public.%I(uuid)',
                           fk.tabela, fk.nome, fk.coluna, fk.referencia);
        END IF;
    END LOOP;
END
$$;
