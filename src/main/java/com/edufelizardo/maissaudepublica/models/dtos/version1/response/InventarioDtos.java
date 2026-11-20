package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.AcaoInventario;
import com.edufelizardo.maissaudepublica.models.enuns.ClasseBem;
import com.edufelizardo.maissaudepublica.models.enuns.ConservacaoBem;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoConferencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoInventarioPatrimonio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** As respostas do inventário e do painel do Patrimônio, fatia PM3 (ADR-0217). */
public final class InventarioDtos {

    private InventarioDtos() {
    }

    public record Membro(String matricula, String nome) {
    }

    public record Item(UUID uuid, UUID bemId, String numeroTombamento, String bemDescricao, String setorEsperadoNome, ResultadoConferencia resultado,
                       ConservacaoBem conservacao, String setorEncontradoNome, String unidadeEncontradaNome, String observacao, LocalDate conferidoEm,
                       AcaoInventario acao) {
    }

    public record Achado(UUID uuid, String descricao, String local) {
    }

    /** As contagens das divergências valem para a conferência feita até agora. */
    public record Inventario(UUID uuid, String numero, UUID unidadeId, String unidadeNome, SituacaoInventarioPatrimonio situacao, LocalDate abertoEm,
                             LocalDate fechadoEm, String observacao, List<Membro> membros, int total, int conferidos, int naoEncontrados,
                             int outroLocal, int inserviveis, List<Item> itens, List<Achado> achados) {
    }

    public record PorUnidade(UUID unidadeId, String unidadeNome, long bens, BigDecimal valorAquisicao, BigDecimal valorContabil, LocalDate ultimoInventario,
                             boolean semInventarioHa12Meses) {
    }

    public record PorClasse(ClasseBem classe, long bens, BigDecimal valorAquisicao, BigDecimal valorContabil) {
    }

    public record CustoBem(UUID bemId, String numeroTombamento, String descricao, String unidadeNome, BigDecimal custo, long diasParado) {
    }

    /** Os bens tombados e não baixados entram nos totais; os a tombar só na contagem própria. */
    public record Painel(long bens, BigDecimal valorAquisicao, BigDecimal valorContabil, long ociosos, long aTombar, long inventariosAbertos,
                         long baixasNoAno, List<PatrimonioDtos.Baixa> baixasPendentes, List<PorUnidade> porUnidade, List<PorClasse> porClasse,
                         List<CustoBem> manutencao) {
    }
}
