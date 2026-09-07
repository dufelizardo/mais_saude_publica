package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MunicipalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.MunicipalResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MunicipalService extends AbstractHierarquicoService<MunicipalResponseDto> {

    @Override
    protected TipoUnidadeDeSaude getTipo() {
        return TipoUnidadeDeSaude.MUNICIPAL;
    }

    @Override
    protected MunicipalResponseDto toResponseDto(UnidadeDeSaude unidadeDeSaude) {
        return MunicipalResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
    }

    @Transactional
    public MunicipalResponseDto create(MunicipalRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = new UnidadeDeSaude(dto);
        vincularSuperiorSeInformado(unidadeDeSaude, dto.getAdministracaoSuperior());
        return salvar(unidadeDeSaude);
    }
}
