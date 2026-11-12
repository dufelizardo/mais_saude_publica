package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.CanalDenuncia;
import com.edufelizardo.maissaudepublica.models.enuns.GrauRiscoSanitario;
import com.edufelizardo.maissaudepublica.models.enuns.GravidadeIrregularidade;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoInspecao;
import com.edufelizardo.maissaudepublica.models.enuns.RespostaRoteiro;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoInspecao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoDenuncia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIrregularidade;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** As respostas da Vigilância Sanitária, fatia V3a (ADR-0199). */
public final class VigilanciaSanitariaDtos {

    private VigilanciaSanitariaDtos() {
    }

    public record Atividade(String cnae, String descricao, GrauRiscoSanitario risco) {
    }

    /** situacao: VIGENTE, VENCIDA, DISPENSADA, SUSPENSA ou CASSADA (vencida e dispensada saem das datas e do tipo). */
    public record Licenca(UUID uuid, UUID estabelecimentoId, String estabelecimentoNome, String numero, boolean dispensa, LocalDate emitidaEm,
                          LocalDate validade, String situacao, String observacao, UUID inspecaoId) {
    }

    public record Estabelecimento(UUID uuid, String documento, String razaoSocial, String nomeFantasia, String endereco, Double latitude,
                                  Double longitude, UUID microareaId, String microareaCodigo, String areaNome, UUID unidadeId, String unidadeNome,
                                  String telefone, String responsavelLegal, String responsavelTecnico, String registroConselhoRt, boolean ativo,
                                  GrauRiscoSanitario risco, List<Atividade> atividades, Licenca licencaAtual, long irregularidadesAbertas,
                                  LocalDate ultimaInspecao) {
    }

    public record EstabelecimentoDetalhe(Estabelecimento estabelecimento, List<Licenca> licencas, List<Inspecao> inspecoes,
                                         List<Irregularidade> irregularidades, List<DocumentosDoPacienteDto.Item> documentos) {
    }

    public record ItemRoteiro(String codigo, String texto, GravidadeIrregularidade gravidade) {
    }

    public record Roteiro(UUID uuid, String codigo, String nome, boolean ativo, UUID versaoId, int versao, List<ItemRoteiro> itens) {
    }

    /** O denunciante só vem para quem gerencia a VISA; para os outros, nulo. */
    public record Denuncia(UUID uuid, String protocolo, CanalDenuncia canal, String descricao, UUID estabelecimentoId, String estabelecimentoNome,
                           String estabelecimentoDescricao, boolean anonima, String denuncianteNome, String denuncianteContato,
                           SituacaoDenuncia situacao, Instant recebidaEm, String conclusao, Instant concluidaEm, long diasEmAberto,
                           int inspecoes) {
    }

    public record Fiscal(String matricula, String nome) {
    }

    public record Irregularidade(UUID uuid, UUID inspecaoId, LocalDate dataInspecao, UUID estabelecimentoId, String estabelecimentoNome,
                                 String itemCodigo, String descricao, String enquadramento, GravidadeIrregularidade gravidade,
                                 LocalDate prazoCorrecao, SituacaoIrregularidade situacao, boolean prazoVencido, LocalDate corrigidaEm,
                                 UUID inspecaoCorrecaoId) {
    }

    public record Inspecao(UUID uuid, UUID estabelecimentoId, String estabelecimentoNome, String documento, MotivoInspecao motivo,
                           LocalDate dataInspecao, UUID denunciaId, String denunciaProtocolo, UUID surtoId, UUID inspecaoOrigemId,
                           UUID roteiroVersaoId, String roteiroNome, Integer roteiroVersao, List<ItemRoteiro> itens,
                           Map<String, RespostaRoteiro> respostas, ResultadoInspecao resultado, String relatorio, List<Fiscal> fiscais,
                           List<Irregularidade> irregularidades, List<UUID> corrigidas, boolean assinada, String hashConteudo,
                           Instant registradoEm) {
    }

    /** motivo: SEM_LICENCA, VENCIDA, VENCENDO (30 dias), SUSPENSA ou CASSADA. */
    public record PendenciaLicenca(UUID estabelecimentoId, String estabelecimentoNome, GrauRiscoSanitario risco, String motivo, LocalDate validade) {
    }

    public record Pendencias(List<PendenciaLicenca> licencas, List<Irregularidade> prazosVencidos, List<Denuncia> denunciasParadas,
                             List<ProcessoSanitarioDtos.PendenciaProcesso> processos, List<ProcessoSanitarioDtos.PendenciaMedida> medidas) {
    }
}
