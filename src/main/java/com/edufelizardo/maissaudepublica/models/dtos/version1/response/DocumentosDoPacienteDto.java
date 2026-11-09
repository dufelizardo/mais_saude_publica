package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Os documentos do paciente (ADR-0172). Quem não lê o prontuário vê só o que anexou, e não abre o arquivo. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DocumentosDoPacienteDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Pode abrir os arquivos. */
    private boolean podeAbrir;
    private List<Item> documentos;
    private List<Item> retirados;

    /** Um documento, com as versões da mais recente para a mais antiga. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Item implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String tipoNome;
        private String titulo;
        private LocalDate dataDocumento;
        private UUID unidadeId;
        private String unidadeNome;
        private LocalDateTime retiradoEm;
        private String motivoRetirada;
        private List<Versao> versoes;
    }

    /** Uma versão do arquivo. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Versao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private int numero;
        private String nomeArquivo;
        private String tipoConteudo;
        private long tamanho;
        private LocalDateTime enviadoEm;
    }
}
