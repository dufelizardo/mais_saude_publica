package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegionalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RegionalResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegionalService extends AbstractHierarquicoService<RegionalResponseDto> {

    @Override
    protected List<TipoUnidadeDeSaude> getTiposAceitos() {
        return List.of(TipoUnidadeDeSaude.REGIONAL);
    }

    @Override
    protected RegionalResponseDto toResponseDto(UnidadeDeSaude unidadeDeSaude) {
        return RegionalResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
    }

    @Transactional
    public RegionalResponseDto create(RegionalRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = new UnidadeDeSaude(dto);
        vincularSuperiorSeInformado(unidadeDeSaude, dto.getAdministracaoSuperior());
        return salvar(unidadeDeSaude);
    }
}
