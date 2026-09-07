package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.HierarquicoDoisRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.HierarquicoDoisResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HierarquicoDoisService extends AbstractHierarquicoService<HierarquicoDoisResponseDto> {

    @Override
    protected TipoUnidadeDeSaude getTipo() {
        return TipoUnidadeDeSaude.MUNICIPAL;
    }

    @Override
    protected HierarquicoDoisResponseDto toResponseDto(UnidadeDeSaude unidadeDeSaude) {
        return HierarquicoDoisResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
    }

    @Transactional
    public HierarquicoDoisResponseDto create(HierarquicoDoisRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = new UnidadeDeSaude(dto);
        vincularSuperiorSeInformado(unidadeDeSaude, dto.getAdministracaoSuperior());
        return salvar(unidadeDeSaude);
    }
}
