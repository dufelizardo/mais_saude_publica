package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeSaudeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeSaudeResponsavelRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeSaudeSupervisaoRegionalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.UnidadeSaudeResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UnidadeSaudeService extends AbstractHierarquicoService<UnidadeSaudeResponseDto> {

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Override
    protected List<TipoUnidadeDeSaude> getTiposAceitos() {
        return List.of(TipoUnidadeDeSaude.UBS, TipoUnidadeDeSaude.HOSPITAL, TipoUnidadeDeSaude.UPA,
                TipoUnidadeDeSaude.LABORATORIO, TipoUnidadeDeSaude.CAPS,
                TipoUnidadeDeSaude.CENTRO_ESPECIALIDADES, TipoUnidadeDeSaude.CENTRO_REABILITACAO,
                TipoUnidadeDeSaude.POLICLINICA);
    }

    @Override
    protected UnidadeSaudeResponseDto toResponseDto(UnidadeDeSaude unidadeDeSaude) {
        return UnidadeSaudeResponseDto.fromHierarquicoResponseDto(unidadeDeSaude);
    }

    @Transactional
    public UnidadeSaudeResponseDto create(UnidadeSaudeRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = new UnidadeDeSaude(dto);
        vincularSuperiorSeInformado(unidadeDeSaude, dto.getAdministracaoSuperior());
        vincularSupervisaoRegionalSeInformado(unidadeDeSaude, dto.getSupervisaoRegional());
        vincularResponsavelSeJaExistir(unidadeDeSaude);
        return salvar(unidadeDeSaude);
    }

    @Transactional
    public UnidadeSaudeResponseDto updateSupervisaoRegional(String nome, UnidadeSaudeSupervisaoRegionalRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);
        vincularSupervisaoRegionalSeInformado(unidadeDeSaude, dto.getSupervisaoRegional());
        return salvar(unidadeDeSaude);
    }

    @Transactional
    public UnidadeSaudeResponseDto updateResponsavel(String nome, UnidadeSaudeResponsavelRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);
        unidadeDeSaude.setResponsavelCpf(dto.getResponsavelCpf());
        unidadeDeSaude.setResponsavel(null);
        vincularResponsavelSeJaExistir(unidadeDeSaude);
        return salvar(unidadeDeSaude);
    }

    /**
     * Metade "unidade chega depois" da reconciliação síncrona (ver ADR-0014): se o
     * {@link Profissional} referenciado pelo CPF já existir, vincula na hora; senão, o vínculo
     * fica pendente até o Profissional ser cadastrado (resolvido por
     * {@link ProfissionalService#create}) ou pelo {@link ReconciliacaoResponsavelScheduler}.
     */
    private void vincularResponsavelSeJaExistir(UnidadeDeSaude unidadeDeSaude) {
        if (unidadeDeSaude.getResponsavelCpf() != null && !unidadeDeSaude.getResponsavelCpf().isEmpty()) {
            profissionalRepository.findByCpfAndAtivoTrue(unidadeDeSaude.getResponsavelCpf())
                    .ifPresent(unidadeDeSaude::setResponsavel);
        }
    }
}
