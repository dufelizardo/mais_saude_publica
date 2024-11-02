package com.edufelizardo.maissaudepublica.models.enuns;

import lombok.Getter;

@Getter
public enum TipoUnidadeDeSaude {
    ADMINISTRACAO1(0),
    ADMINISTRACAO2(1),
    ADMINISTRACAO3(2),
    ADMINISTRACAO4(3),
    UBS(4),
    HOSPITAL(5);

    private final int tipo;

    TipoUnidadeDeSaude(int tipo) {
        this.tipo = tipo;
    }

}
