package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.HierarquicoTresRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.HierarquicoTresResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HierarquicoTresService extends AbstractHierarquicoService<HierarquicoTresResponseDto> {

    @Override
    protected TipoUnidadeDeSaude getTipo() {
        return TipoUnidadeDeSaude.REGIONAL;
    }

    @Override
    protected HierarquicoTresResponseDto toResponseDto(UnidadeDeSaude unidadeDeSaude) {
        return HierarquicoTresResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
    }

    @Transactional
    public HierarquicoTresResponseDto create(HierarquicoTresRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = new UnidadeDeSaude(dto);
        vincularSuperiorSeInformado(unidadeDeSaude, dto.getAdministracaoSuperior());
        return salvar(unidadeDeSaude);
    }
}
