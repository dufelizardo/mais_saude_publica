package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FederalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FederalResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FederalService extends AbstractHierarquicoService<FederalResponseDto> {

    @Override
    protected TipoUnidadeDeSaude getTipo() {
        return TipoUnidadeDeSaude.FEDERAL;
    }

    @Override
    protected FederalResponseDto toResponseDto(UnidadeDeSaude unidadeDeSaude) {
        return FederalResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
    }

    @Transactional
    public FederalResponseDto create(FederalRequestDto dto) {
        return salvar(new UnidadeDeSaude(dto));
    }
}
