package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EstadualRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EstadualResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstadualService extends AbstractHierarquicoService<EstadualResponseDto> {

    @Override
    protected TipoUnidadeDeSaude getTipo() {
        return TipoUnidadeDeSaude.ESTADUAL;
    }

    @Override
    protected EstadualResponseDto toResponseDto(UnidadeDeSaude unidadeDeSaude) {
        return EstadualResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
    }

    @Transactional
    public EstadualResponseDto create(EstadualRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = new UnidadeDeSaude(dto);
        vincularSuperiorSeInformado(unidadeDeSaude, dto.getAdministracaoSuperior());
        return salvar(unidadeDeSaude);
    }
}
