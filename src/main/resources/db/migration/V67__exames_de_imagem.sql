-- Laboratório, fatia L3 (ADR-0182): exames de imagem. Categoria e modalidade no catálogo (o material passa a valer só para o
-- laboratorial), o serviço de imagem no pedido, a realização do exame, o laudo no resultado e os valores novos de situação,
-- interpretação e evento. Conferido contra o ddl-auto=validate (ADR-0090). Papéis e permissões vêm do CatalogoDeAcesso.

ALTER TABLE public.tb_exame_laboratorial ADD COLUMN categoria character varying(255) NOT NULL DEFAULT 'LABORATORIAL';
ALTER TABLE public.tb_exame_laboratorial ALTER COLUMN categoria DROP DEFAULT;
ALTER TABLE public.tb_exame_laboratorial ADD CONSTRAINT tb_exame_laboratorial_categoria_check CHECK (((categoria)::text = ANY ((ARRAY['LABORATORIAL'::character varying, 'IMAGEM'::character varying])::text[])));
ALTER TABLE public.tb_exame_laboratorial ADD COLUMN modalidade character varying(255);
ALTER TABLE public.tb_exame_laboratorial ADD CONSTRAINT tb_exame_laboratorial_modalidade_check CHECK (((modalidade)::text = ANY ((ARRAY['RAIO_X'::character varying, 'ULTRASSONOGRAFIA'::character varying, 'TOMOGRAFIA'::character varying, 'RESSONANCIA_MAGNETICA'::character varying, 'MAMOGRAFIA'::character varying, 'DENSITOMETRIA'::character varying, 'OUTRA'::character varying])::text[])));
ALTER TABLE public.tb_exame_laboratorial ALTER COLUMN material DROP NOT NULL;

ALTER TABLE public.tb_pedido_exame ADD COLUMN unidade_executante_imagem_id uuid;
ALTER TABLE ONLY public.tb_pedido_exame ADD CONSTRAINT fk_pedido_exame_executante_imagem FOREIGN KEY (unidade_executante_imagem_id) REFERENCES public.tb_unidade_de_saude(uuid);
CREATE INDEX ix_pedido_exame_executante_imagem ON public.tb_pedido_exame USING btree (unidade_executante_imagem_id);

CREATE TABLE public.tb_realizacao_exame_imagem (
    contraste boolean NOT NULL,
    realizada_em timestamp(6) with time zone NOT NULL,
    item_id uuid NOT NULL,
    tecnico_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    equipamento character varying(150),
    contraste_descricao character varying(200),
    observacao character varying(1000),
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_realizacao_exame_imagem ADD CONSTRAINT tb_realizacao_exame_imagem_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_realizacao_exame_imagem ADD CONSTRAINT uk_realizacao_exame_imagem_item UNIQUE (item_id);
ALTER TABLE ONLY public.tb_realizacao_exame_imagem ADD CONSTRAINT fk_realizacao_imagem_item FOREIGN KEY (item_id) REFERENCES public.tb_item_pedido_exame(uuid);
ALTER TABLE ONLY public.tb_realizacao_exame_imagem ADD CONSTRAINT fk_realizacao_imagem_tecnico FOREIGN KEY (tecnico_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_realizacao_exame_imagem ADD CONSTRAINT fk_realizacao_imagem_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE public.tb_resultado_exame ADD COLUMN laudo_tecnica character varying(1000);
ALTER TABLE public.tb_resultado_exame ADD COLUMN laudo_achados character varying(4000);
ALTER TABLE public.tb_resultado_exame ADD COLUMN laudo_conclusao character varying(2000);
ALTER TABLE public.tb_resultado_exame ADD COLUMN imagem_url character varying(500);

ALTER TABLE public.tb_item_pedido_exame DROP CONSTRAINT tb_item_pedido_exame_status_check;
ALTER TABLE public.tb_item_pedido_exame ADD CONSTRAINT tb_item_pedido_exame_status_check CHECK (((status)::text = ANY ((ARRAY['SOLICITADO'::character varying, 'COLETADO'::character varying, 'REALIZADO'::character varying, 'RESULTADO_REGISTRADO'::character varying, 'LIBERADO'::character varying, 'CANCELADO'::character varying])::text[])));

ALTER TABLE public.tb_resultado_exame DROP CONSTRAINT tb_resultado_exame_interpretacao_check;
ALTER TABLE public.tb_resultado_exame ADD CONSTRAINT tb_resultado_exame_interpretacao_check CHECK (((interpretacao)::text = ANY ((ARRAY['NORMAL'::character varying, 'ACIMA'::character varying, 'ABAIXO'::character varying, 'CRITICO_ALTO'::character varying, 'CRITICO_BAIXO'::character varying, 'ALTERADO'::character varying, 'ACHADO_CRITICO'::character varying])::text[])));

ALTER TABLE public.tb_evento_exame DROP CONSTRAINT tb_evento_exame_tipo_check;
ALTER TABLE public.tb_evento_exame ADD CONSTRAINT tb_evento_exame_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['SOLICITACAO'::character varying, 'COLETA'::character varying, 'REJEICAO_AMOSTRA'::character varying, 'RESULTADO'::character varying, 'LIBERACAO'::character varying, 'RETIFICACAO'::character varying, 'CANCELAMENTO'::character varying, 'COMUNICACAO_CRITICO'::character varying, 'CIENCIA'::character varying, 'ENTREGA'::character varying, 'REALIZACAO'::character varying, 'LAUDO'::character varying, 'ADENDO'::character varying])::text[])));
