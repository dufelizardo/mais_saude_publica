-- Vigilância em Saúde, fatia V3a (ADR-0199, desenho na ADR-0110): a vigilância sanitária. O estabelecimento regulado com as
-- atividades e o grau de risco (RDC 153/2017), a licença sanitária, a denúncia, o roteiro de inspeção versionado, a inspeção
-- com os fiscais e a assinatura, e as irregularidades com o prazo de correção. Os documentos ganham dois donos (D2 parcial,
-- ADR-0172) e a unidade passa a ser opcional fora do paciente. As permissões e os papéis entram pelo CatalogoDeAcesso.
-- Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_estabelecimento_regulado (
    ativo boolean NOT NULL,
    latitude double precision,
    longitude double precision,
    registrado_em timestamp(6) with time zone NOT NULL,
    microarea_id uuid,
    unidade_id uuid,
    uuid uuid NOT NULL,
    documento character varying(14) NOT NULL,
    razao_social character varying(200) NOT NULL,
    nome_fantasia character varying(200),
    endereco character varying(300) NOT NULL,
    telefone character varying(30),
    responsavel_legal character varying(200),
    responsavel_tecnico character varying(200),
    registro_conselho_rt character varying(50),
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_estabelecimento_regulado ADD CONSTRAINT tb_estabelecimento_regulado_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_estabelecimento_regulado ADD CONSTRAINT uk_estabelecimento_regulado_documento UNIQUE (documento);
ALTER TABLE ONLY public.tb_estabelecimento_regulado ADD CONSTRAINT fk_estabelecimento_regulado_microarea FOREIGN KEY (microarea_id) REFERENCES public.tb_microarea(uuid);
ALTER TABLE ONLY public.tb_estabelecimento_regulado ADD CONSTRAINT fk_estabelecimento_regulado_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_atividade_estabelecimento (
    estabelecimento_id uuid NOT NULL,
    uuid uuid NOT NULL,
    cnae character varying(7) NOT NULL,
    descricao character varying(200) NOT NULL,
    risco character varying(255) NOT NULL,
    CONSTRAINT tb_atividade_estabelecimento_risco_check CHECK (((risco)::text = ANY ((ARRAY['BAIXO'::character varying, 'MEDIO'::character varying, 'ALTO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_atividade_estabelecimento ADD CONSTRAINT tb_atividade_estabelecimento_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_atividade_estabelecimento ADD CONSTRAINT uk_atividade_estabelecimento_cnae UNIQUE (estabelecimento_id, cnae);
ALTER TABLE ONLY public.tb_atividade_estabelecimento ADD CONSTRAINT fk_atividade_estabelecimento_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES public.tb_estabelecimento_regulado(uuid);

CREATE TABLE public.tb_roteiro_inspecao (
    ativo boolean NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    uuid uuid NOT NULL,
    codigo character varying(40) NOT NULL,
    nome character varying(200) NOT NULL
);
ALTER TABLE ONLY public.tb_roteiro_inspecao ADD CONSTRAINT tb_roteiro_inspecao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_roteiro_inspecao ADD CONSTRAINT uk_roteiro_inspecao_codigo UNIQUE (codigo);

CREATE TABLE public.tb_versao_roteiro_inspecao (
    versao integer NOT NULL,
    criada_em timestamp(6) with time zone NOT NULL,
    roteiro_id uuid NOT NULL,
    uuid uuid NOT NULL,
    definicao text NOT NULL,
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_versao_roteiro_inspecao ADD CONSTRAINT tb_versao_roteiro_inspecao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_versao_roteiro_inspecao ADD CONSTRAINT uk_versao_roteiro_inspecao UNIQUE (roteiro_id, versao);
ALTER TABLE ONLY public.tb_versao_roteiro_inspecao ADD CONSTRAINT fk_versao_roteiro_inspecao_roteiro FOREIGN KEY (roteiro_id) REFERENCES public.tb_roteiro_inspecao(uuid);

CREATE TABLE public.tb_denuncia_sanitaria (
    anonima boolean NOT NULL,
    concluida_em timestamp(6) with time zone,
    recebida_em timestamp(6) with time zone NOT NULL,
    estabelecimento_id uuid,
    uuid uuid NOT NULL,
    protocolo character varying(20) NOT NULL,
    canal character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    descricao character varying(2000) NOT NULL,
    estabelecimento_descricao character varying(300),
    denunciante_nome character varying(200),
    denunciante_contato character varying(200),
    conclusao character varying(1000),
    concluida_por_cpf character varying(255),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_denuncia_sanitaria_canal_check CHECK (((canal)::text = ANY ((ARRAY['TELEFONE'::character varying, 'PRESENCIAL'::character varying, 'OUVIDORIA'::character varying, 'INTERNET'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_denuncia_sanitaria_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['RECEBIDA'::character varying, 'EM_APURACAO'::character varying, 'PROCEDENTE'::character varying, 'IMPROCEDENTE'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_denuncia_sanitaria ADD CONSTRAINT tb_denuncia_sanitaria_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_denuncia_sanitaria ADD CONSTRAINT uk_denuncia_sanitaria_protocolo UNIQUE (protocolo);
ALTER TABLE ONLY public.tb_denuncia_sanitaria ADD CONSTRAINT fk_denuncia_sanitaria_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES public.tb_estabelecimento_regulado(uuid);

CREATE TABLE public.tb_inspecao_sanitaria (
    data_inspecao date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    denuncia_id uuid,
    estabelecimento_id uuid NOT NULL,
    inspecao_origem_id uuid,
    roteiro_versao_id uuid,
    surto_id uuid,
    uuid uuid NOT NULL,
    motivo character varying(255) NOT NULL,
    resultado character varying(255) NOT NULL,
    relatorio character varying(4000) NOT NULL,
    respostas text,
    hash_conteudo character varying(64),
    assinado_por_cpf character varying(255),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_inspecao_sanitaria_motivo_check CHECK (((motivo)::text = ANY ((ARRAY['LICENCIAMENTO'::character varying, 'PROGRAMADA'::character varying, 'DENUNCIA'::character varying, 'RETORNO'::character varying, 'SURTO'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_inspecao_sanitaria_resultado_check CHECK (((resultado)::text = ANY ((ARRAY['SATISFATORIA'::character varying, 'COM_RESSALVAS'::character varying, 'INSATISFATORIA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_inspecao_sanitaria ADD CONSTRAINT tb_inspecao_sanitaria_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_inspecao_sanitaria_estabelecimento ON public.tb_inspecao_sanitaria USING btree (estabelecimento_id, data_inspecao);
ALTER TABLE ONLY public.tb_inspecao_sanitaria ADD CONSTRAINT fk_inspecao_sanitaria_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES public.tb_estabelecimento_regulado(uuid);
ALTER TABLE ONLY public.tb_inspecao_sanitaria ADD CONSTRAINT fk_inspecao_sanitaria_denuncia FOREIGN KEY (denuncia_id) REFERENCES public.tb_denuncia_sanitaria(uuid);
ALTER TABLE ONLY public.tb_inspecao_sanitaria ADD CONSTRAINT fk_inspecao_sanitaria_origem FOREIGN KEY (inspecao_origem_id) REFERENCES public.tb_inspecao_sanitaria(uuid);
ALTER TABLE ONLY public.tb_inspecao_sanitaria ADD CONSTRAINT fk_inspecao_sanitaria_roteiro FOREIGN KEY (roteiro_versao_id) REFERENCES public.tb_versao_roteiro_inspecao(uuid);
ALTER TABLE ONLY public.tb_inspecao_sanitaria ADD CONSTRAINT fk_inspecao_sanitaria_surto FOREIGN KEY (surto_id) REFERENCES public.tb_surto(uuid);

CREATE TABLE public.tb_inspecao_sanitaria_fiscal (
    inspecao_id uuid NOT NULL,
    profissional_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_inspecao_sanitaria_fiscal ADD CONSTRAINT tb_inspecao_sanitaria_fiscal_pkey PRIMARY KEY (inspecao_id, profissional_id);
ALTER TABLE ONLY public.tb_inspecao_sanitaria_fiscal ADD CONSTRAINT fk_inspecao_sanitaria_fiscal_inspecao FOREIGN KEY (inspecao_id) REFERENCES public.tb_inspecao_sanitaria(uuid);
ALTER TABLE ONLY public.tb_inspecao_sanitaria_fiscal ADD CONSTRAINT fk_inspecao_sanitaria_fiscal_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

CREATE TABLE public.tb_licenca_sanitaria (
    dispensa boolean NOT NULL,
    emitida_em date NOT NULL,
    validade date,
    registrado_em timestamp(6) with time zone NOT NULL,
    estabelecimento_id uuid NOT NULL,
    inspecao_id uuid,
    uuid uuid NOT NULL,
    numero character varying(20) NOT NULL,
    situacao character varying(255) NOT NULL,
    observacao character varying(500),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_licenca_sanitaria_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['VIGENTE'::character varying, 'SUSPENSA'::character varying, 'CASSADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_licenca_sanitaria ADD CONSTRAINT tb_licenca_sanitaria_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_licenca_sanitaria ADD CONSTRAINT uk_licenca_sanitaria_numero UNIQUE (numero);
ALTER TABLE ONLY public.tb_licenca_sanitaria ADD CONSTRAINT fk_licenca_sanitaria_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES public.tb_estabelecimento_regulado(uuid);
ALTER TABLE ONLY public.tb_licenca_sanitaria ADD CONSTRAINT fk_licenca_sanitaria_inspecao FOREIGN KEY (inspecao_id) REFERENCES public.tb_inspecao_sanitaria(uuid);

CREATE TABLE public.tb_irregularidade_sanitaria (
    corrigida_em date,
    prazo_correcao date,
    registrado_em timestamp(6) with time zone NOT NULL,
    estabelecimento_id uuid NOT NULL,
    inspecao_correcao_id uuid,
    inspecao_id uuid NOT NULL,
    uuid uuid NOT NULL,
    item_codigo character varying(40),
    gravidade character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    enquadramento character varying(500),
    descricao character varying(1000) NOT NULL,
    CONSTRAINT tb_irregularidade_sanitaria_gravidade_check CHECK (((gravidade)::text = ANY ((ARRAY['LEVE'::character varying, 'GRAVE'::character varying, 'GRAVISSIMA'::character varying])::text[]))),
    CONSTRAINT tb_irregularidade_sanitaria_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ABERTA'::character varying, 'CORRIGIDA'::character varying, 'AUTUADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_irregularidade_sanitaria ADD CONSTRAINT tb_irregularidade_sanitaria_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_irregularidade_sanitaria_estabelecimento ON public.tb_irregularidade_sanitaria USING btree (estabelecimento_id, situacao);
ALTER TABLE ONLY public.tb_irregularidade_sanitaria ADD CONSTRAINT fk_irregularidade_sanitaria_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES public.tb_estabelecimento_regulado(uuid);
ALTER TABLE ONLY public.tb_irregularidade_sanitaria ADD CONSTRAINT fk_irregularidade_sanitaria_inspecao FOREIGN KEY (inspecao_id) REFERENCES public.tb_inspecao_sanitaria(uuid);
ALTER TABLE ONLY public.tb_irregularidade_sanitaria ADD CONSTRAINT fk_irregularidade_sanitaria_correcao FOREIGN KEY (inspecao_correcao_id) REFERENCES public.tb_inspecao_sanitaria(uuid);

-- Documentos (D2 parcial): o estabelecimento e a inspeção como donos; fora do paciente, a unidade é opcional.
ALTER TABLE public.tb_documento DROP CONSTRAINT tb_documento_dono_tipo_check;
ALTER TABLE public.tb_documento ADD CONSTRAINT tb_documento_dono_tipo_check CHECK (((dono_tipo)::text = ANY ((ARRAY['PACIENTE'::character varying, 'ESTABELECIMENTO'::character varying, 'INSPECAO_SANITARIA'::character varying])::text[])));
ALTER TABLE public.tb_documento ALTER COLUMN unidade_id DROP NOT NULL;
ALTER TABLE public.tb_documento ADD CONSTRAINT tb_documento_unidade_paciente_check CHECK (dono_tipo <> 'PACIENTE' OR unidade_id IS NOT NULL);

-- Carga: dois roteiros de referência, editáveis pela coordenação (nova versão).
INSERT INTO public.tb_roteiro_inspecao (ativo, registrado_em, uuid, codigo, nome) VALUES
    (true, now(), gen_random_uuid(), 'ALIMENTOS_BOAS_PRATICAS', 'Serviços de alimentação: boas práticas (RDC 216/2004)'),
    (true, now(), gen_random_uuid(), 'FARMACIA_DROGARIA', 'Farmácias e drogarias: boas práticas farmacêuticas (RDC 44/2009)');
INSERT INTO public.tb_versao_roteiro_inspecao (versao, criada_em, roteiro_id, uuid, definicao)
SELECT 1, now(), r.uuid, gen_random_uuid(), v.definicao FROM public.tb_roteiro_inspecao r JOIN (VALUES
    ('ALIMENTOS_BOAS_PRATICAS', '{"itens": [{"codigo": "A1", "texto": "Edificação, instalações e utensílios em bom estado de conservação e limpeza", "gravidade": "LEVE"}, {"codigo": "A2", "texto": "Água potável, com reservatório limpo e higienizado a cada seis meses", "gravidade": "GRAVE"}, {"codigo": "A3", "texto": "Controle integrado de vetores e pragas, com comprovante", "gravidade": "GRAVE"}, {"codigo": "A4", "texto": "Manipuladores com uniforme, asseio e saúde controlados", "gravidade": "LEVE"}, {"codigo": "A5", "texto": "Matérias-primas dentro da validade e armazenadas adequadamente", "gravidade": "GRAVISSIMA"}, {"codigo": "A6", "texto": "Controle de temperatura na conservação e na exposição dos alimentos", "gravidade": "GRAVE"}, {"codigo": "A7", "texto": "Manual de boas práticas e procedimentos operacionais padronizados", "gravidade": "LEVE"}, {"codigo": "A8", "texto": "Responsável pela manipulação capacitado", "gravidade": "LEVE"}]}'),
    ('FARMACIA_DROGARIA', '{"itens": [{"codigo": "F1", "texto": "Farmacêutico responsável presente durante todo o funcionamento", "gravidade": "GRAVE"}, {"codigo": "F2", "texto": "Medicamentos dentro da validade, sem violação nem desvio de qualidade", "gravidade": "GRAVISSIMA"}, {"codigo": "F3", "texto": "Controle de temperatura e umidade do ambiente e da geladeira", "gravidade": "GRAVE"}, {"codigo": "F4", "texto": "Escrituração dos controlados (Portaria 344/1998) em dia", "gravidade": "GRAVISSIMA"}, {"codigo": "F5", "texto": "Sala de serviços farmacêuticos adequada, quando houver", "gravidade": "LEVE"}, {"codigo": "F6", "texto": "Descarte de resíduos conforme o plano de gerenciamento (RDC 222/2018)", "gravidade": "GRAVE"}]}')
) AS v(codigo, definicao) ON v.codigo = r.codigo;
