package com.edufelizardo.maissaudepublica.models.enuns;

/** Tipo de veículo da frota (ADR-0178). Os quatro primeiros são do SAMU e ficam em unidade com o perfil Atenção móvel. */
public enum TipoVeiculo {
    /** Unidade de suporte avançado. */
    USA,
    /** Unidade de suporte básico. */
    USB,
    MOTOLANCIA,
    AEROMEDICO,
    /** Ambulância de transporte simples (tipo A). */
    AMBULANCIA_TRANSPORTE,
    VAN_SANITARIA,
    MICRO_ONIBUS,
    CARRO_ADMINISTRATIVO;

    /** Viatura do SAMU: só em unidade com o perfil Atenção móvel, com a tripulação mínima da Portaria 2.048/2002. */
    public boolean doSamu() {
        return this == USA || this == USB || this == MOTOLANCIA || this == AEROMEDICO;
    }
}
