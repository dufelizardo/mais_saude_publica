package com.edufelizardo.maissaudepublica.models.enuns;

/**
 * Tipo do turno da escala (ADR-0105), com as regras de duração e de horário de cada um. Férias, licença e folga não são
 * turno: férias e licença vêm do afastamento do RH, e folga é o dia sem turno.
 */
public enum TipoTurno {
    MANHA,
    TARDE,
    /** O dia inteiro, com intervalo: o padrão das 40 horas (8h + 1h, ex.: 08:00 às 17:00). */
    DIURNO,
    /** Atravessa a meia-noite; só em unidade 24 horas. */
    NOITE,
    PLANTAO_12H,
    PLANTAO_24H,
    /** À distância, fora da unidade: não precisa caber no horário e não soma na jornada (CLT, art. 244). */
    SOBREAVISO,
    /** Pode ser fora da unidade e do horário dela. */
    CAPACITACAO;

    /** Só em unidade que funciona 24 horas. */
    public boolean exige24h() {
        return this == NOITE || this == PLANTAO_12H || this == PLANTAO_24H;
    }

    /** Precisa caber no horário de funcionamento da unidade (quando ela não é 24 horas). */
    public boolean dentroDoHorario() {
        return this == MANHA || this == TARDE || this == DIURNO;
    }

    /** Termina no mesmo dia em que começa. */
    public boolean mesmoDia() {
        return this == MANHA || this == TARDE || this == DIURNO || this == CAPACITACAO;
    }

    /** Duração exigida em minutos; nulo quando a duração é livre, até o máximo. */
    public Integer duracaoExata() {
        return switch (this) {
            case PLANTAO_12H -> Integer.valueOf(12 * 60);
            case PLANTAO_24H -> Integer.valueOf(24 * 60);
            default -> null;
        };
    }

    public int duracaoMaxima() {
        return this == PLANTAO_24H || this == SOBREAVISO ? 24 * 60 : 12 * 60;
    }

    public boolean plantao() {
        return this == PLANTAO_12H || this == PLANTAO_24H || this == NOITE || this == SOBREAVISO;
    }

    /** Plantão de 12x36: o intervalo pode ser indenizado (CLT, art. 59-A), então a falta dele não gera alerta. */
    public boolean intervaloIndenizavel() {
        return this == PLANTAO_12H || this == PLANTAO_24H || this == SOBREAVISO;
    }

    public boolean somaNaJornada() {
        return this != SOBREAVISO;
    }
}
