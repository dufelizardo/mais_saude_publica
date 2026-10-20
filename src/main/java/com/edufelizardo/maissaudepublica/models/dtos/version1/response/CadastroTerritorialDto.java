package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.enuns.AbastecimentoAgua;
import com.edufelizardo.maissaudepublica.models.enuns.DestinoLixo;
import com.edufelizardo.maissaudepublica.models.enuns.EscoamentoSanitario;
import com.edufelizardo.maissaudepublica.models.enuns.FaixaRendaFamiliar;
import com.edufelizardo.maissaudepublica.models.enuns.LocalizacaoDomicilio;
import com.edufelizardo.maissaudepublica.models.enuns.Parentesco;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMoradia;
import com.edufelizardo.maissaudepublica.models.enuns.TipoImovel;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Respostas do cadastro territorial (ADR-0127): imóvel, domicílio, família e membro. */
public final class CadastroTerritorialDto {

    private CadastroTerritorialDto() {
    }

    /** Um imóvel na lista da área, com as contagens. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Imovel implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private UUID microareaId;
        private String microareaCodigo;
        private TipoImovel tipo;
        private Endereco endereco;
        private String enderecoTexto;
        private Double latitude;
        private Double longitude;
        private boolean ativo;
        private int domicilios;
        private int familias;
        private int pessoas;
    }

    /** O imóvel com os domicílios e as famílias. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class ImovelDetalhe implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Imovel imovel;
        private UUID areaId;
        private String areaCodigo;
        private List<Domicilio> domicilios;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Domicilio implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private UUID imovelId;
        private String identificacao;
        private SituacaoMoradia situacao;
        private LocalizacaoDomicilio localizacao;
        private Integer comodos;
        private AbastecimentoAgua abastecimentoAgua;
        private EscoamentoSanitario escoamentoSanitario;
        private DestinoLixo destinoLixo;
        private Boolean energiaEletrica;
        private boolean ativo;
        /** As famílias que moram ali hoje. */
        private List<Familia> familias;
        /** As que já moraram (mudaram, ou se desfizeram). */
        private List<Familia> anteriores;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Familia implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private UUID domicilioId;
        private UUID responsavelId;
        private String responsavelNome;
        private FaixaRendaFamiliar rendaFamiliar;
        private LocalDate inicio;
        private LocalDate fim;
        private String motivoFim;
        private List<Membro> membros;
        private List<Membro> antigos;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Membro implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private UUID pacienteId;
        private String nome;
        private LocalDate dataNascimento;
        private Parentesco parentesco;
        private LocalDate inicio;
        private LocalDate fim;
        private String motivoSaida;
        /** O endereço do cadastro do paciente não é o do imóvel. */
        private boolean enderecoDiverge;
    }

    /** A família do paciente hoje e onde ele já morou. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class FamiliaDoPaciente implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID pacienteId;
        /** Nula: o paciente não está em família cadastrada. */
        private Familia familia;
        private UUID imovelId;
        private String enderecoTexto;
        private UUID areaId;
        private String areaCodigo;
        private String microareaCodigo;
        private List<Moradia> moradias;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Moradia implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID familiaId;
        private String enderecoTexto;
        private String microareaCodigo;
        private LocalDate inicio;
        private LocalDate fim;
    }
}
