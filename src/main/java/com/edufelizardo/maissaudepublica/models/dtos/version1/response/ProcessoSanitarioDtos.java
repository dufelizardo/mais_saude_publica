package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.AgravanteSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.AtenuanteSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.FaseProcessoSanitario;
import com.edufelizardo.maissaudepublica.models.enuns.FormaCiencia;
import com.edufelizardo.maissaudepublica.models.enuns.GravidadeIrregularidade;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoDecisaoSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoRecursoSanitario;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMedidaSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMedidaSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoPenalidadeSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoTermoSanitario;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** As respostas do processo administrativo sanitário e das medidas, fatia V3b (ADR-0200). */
public final class ProcessoSanitarioDtos {

    private ProcessoSanitarioDtos() {
    }

    public record Termo(UUID uuid, String codigo, TipoTermoSanitario tipo, String numero, Instant emitidoEm, UUID versaoId) {
    }

    public record Penalidade(UUID uuid, TipoPenalidadeSanitaria tipo, BigDecimal valorMulta, LocalDate suspensaoAte, String descricao, boolean revogada) {
    }

    /** prazoDefesa e prazoRecurso: o último dia; revelia: o prazo de defesa venceu sem defesa. */
    public record Processo(UUID uuid, String numero, UUID estabelecimentoId, String estabelecimentoNome, String documento, UUID inspecaoId,
                           String dispositivoLegal, String descricao, GravidadeIrregularidade gravidade, FaseProcessoSanitario fase,
                           LocalDate dataLavratura, List<VigilanciaSanitariaDtos.Fiscal> fiscais,
                           List<VigilanciaSanitariaDtos.Irregularidade> irregularidades, LocalDate cienciaAutoEm, FormaCiencia formaCienciaAuto,
                           LocalDate prazoDefesa, LocalDate defesaEm, String defesaTexto, Boolean defesaIntempestiva, boolean revelia,
                           ResultadoDecisaoSanitaria decisaoResultado, LocalDate dataDecisao, String decisaoFundamentacao,
                           List<AtenuanteSanitaria> atenuantes, List<AgravanteSanitaria> agravantes, LocalDate cienciaDecisaoEm,
                           FormaCiencia formaCienciaDecisao, LocalDate prazoRecurso, LocalDate recursoEm, String recursoTexto,
                           ResultadoRecursoSanitario recursoResultado, LocalDate dataJulgamentoRecurso, String recursoFundamentacao,
                           LocalDate encerradoEm, List<Penalidade> penalidades, List<Termo> termos, List<Medida> medidas) {
    }

    public record Medida(UUID uuid, String numero, UUID estabelecimentoId, String estabelecimentoNome, UUID processoId, String processoNumero,
                         TipoMedidaSanitaria tipo, boolean total, String descricao, String produto, String lote, String quantidade, String localGuarda,
                         LocalDate inicio, LocalDate fimPrevisto, SituacaoMedidaSanitaria situacao, LocalDate encerradaEm, String motivoEncerramento,
                         boolean vencida, List<Termo> termos) {
    }

    /** motivo: CIENCIA_DO_AUTO, REVELIA, DECISAO_PARADA, CIENCIA_DA_DECISAO ou A_ENCERRAR. */
    public record PendenciaProcesso(UUID processoId, String numero, String estabelecimentoNome, String motivo, LocalDate desde) {
    }

    /** motivo: CAUTELAR_VENCENDO ou CAUTELAR_VENCIDA. */
    public record PendenciaMedida(UUID medidaId, String numero, String estabelecimentoNome, String motivo, LocalDate fimPrevisto) {
    }

    /** A interdição vigente, para a Sala de Situação: o estabelecimento é atividade econômica, sem dado de pessoa. */
    public record Interdicao(UUID uuid, String numero, String estabelecimentoNome, TipoMedidaSanitaria tipo, boolean total, LocalDate inicio,
                             LocalDate fimPrevisto) {
    }

    /** A verificação pública do termo pelo código: sem pessoas nem conteúdo; o CPF do estabelecimento pessoa física aparece mascarado. */
    public record Verificacao(String codigo, TipoTermoSanitario tipo, String numero, Instant emitidoEm, String estabelecimento, String documento,
                              boolean integro, String hashPdf) {
    }

    /** A verificação pública da licença pelo número: situação vigente, vencida, dispensada, suspensa ou cassada. */
    public record VerificacaoLicenca(String numero, String estabelecimento, String documento, boolean dispensa, LocalDate emitidaEm, LocalDate validade,
                                     String situacao) {
    }
}
