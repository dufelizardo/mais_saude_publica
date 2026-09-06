package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.HierarquicoUmRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.HierarquicoUmResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HierarquicoUmService extends AbstractHierarquicoService<HierarquicoUmResponseDto> {

    @Override
    protected TipoUnidadeDeSaude getTipo() {
        return TipoUnidadeDeSaude.ADMINISTRACAO2;
    }

    @Override
    protected HierarquicoUmResponseDto toResponseDto(UnidadeDeSaude unidadeDeSaude) {
        return HierarquicoUmResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
    }

    @Transactional
    public HierarquicoUmResponseDto create(HierarquicoUmRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = new UnidadeDeSaude(dto);
        vincularSuperiorSeInformado(unidadeDeSaude, dto.getAdministracaoSuperior());
        return salvar(unidadeDeSaude);
    }
}
