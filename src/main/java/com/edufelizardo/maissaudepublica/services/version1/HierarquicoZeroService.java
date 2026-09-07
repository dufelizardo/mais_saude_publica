package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.HierarquicoZeroRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.HierarquicoZeroResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HierarquicoZeroService extends AbstractHierarquicoService<HierarquicoZeroResponseDto> {

    @Override
    protected TipoUnidadeDeSaude getTipo() {
        return TipoUnidadeDeSaude.FEDERAL;
    }

    @Override
    protected HierarquicoZeroResponseDto toResponseDto(UnidadeDeSaude unidadeDeSaude) {
        return HierarquicoZeroResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
    }

    @Transactional
    public HierarquicoZeroResponseDto create(HierarquicoZeroRequestDto dto) {
        return salvar(new UnidadeDeSaude(dto));
    }
}
