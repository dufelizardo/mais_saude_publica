package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeDeSaudeAtivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeDeSaudeEnderecoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeDeSaudeHorarioDeAtendimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeDeSaudeHorarioDeFuncionamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeDeSaudeNomeUpdateRequestDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Reúne o CRUD comum aos níveis hierárquicos de UnidadeDeSaude. Cada nível
 * só precisa informar quais {@link TipoUnidadeDeSaude} aceita e como mapear a entidade
 * para o DTO de resposta correspondente. A maioria dos níveis aceita um único tipo, mas
 * "Unidade de Saúde" aceita UBS e HOSPITAL sob o mesmo controller/service (ver ADR-0013).
 */
public abstract class AbstractHierarquicoService<RES> {

    @Autowired
    protected UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    protected abstract List<TipoUnidadeDeSaude> getTiposAceitos();

    protected abstract RES toResponseDto(UnidadeDeSaude unidadeDeSaude);

    public List<RES> getAll() {
        return unidadeDeSaudeRepository.findByTipoIn(getTiposAceitos())
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public RES findByNome(String nome) {
        return toResponseDto(buscarUnidadeDeSaudePorNome(nome));
    }

    @Transactional
    public RES updateNome(@Valid String nome, UnidadeDeSaudeNomeUpdateRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);
        unidadeDeSaude.setNome(dto.getNome());
        return salvar(unidadeDeSaude);
    }

    @Transactional
    public RES updateContato(@Valid String nome, UnidadeDeSaudeEnderecoRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);
        unidadeDeSaude.setEndereco(new Endereco(dto.getEndereco()));
        unidadeDeSaude.setSaudeTelefones(dto.getTelefones());
        unidadeDeSaude.setEmail(dto.getEmail());
        return salvar(unidadeDeSaude);
    }

    @Transactional
    public RES updateHorarioFuncionamento(@Valid String nome, UnidadeDeSaudeHorarioDeFuncionamentoRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);
        unidadeDeSaude.setHorarioFuncionamento(dto.getHorarioFuncionamento());
        return salvar(unidadeDeSaude);
    }

    @Transactional
    public RES updateHorarioAtendimento(@Valid String nome, UnidadeDeSaudeHorarioDeAtendimentoRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);
        unidadeDeSaude.setHorarioAtendimento(dto.getHorarioAtendimento());
        return salvar(unidadeDeSaude);
    }

    @Transactional
    public RES desabilitar(String nome, UnidadeDeSaudeAtivoRequestDto dto) {
        UnidadeDeSaude unidadeDeSaude = buscarUnidadeDeSaudePorNome(nome);
        unidadeDeSaude.setAtivo(dto.isAtivo());
        return salvar(unidadeDeSaude);
    }

    /**
     * Vincula a unidade superior informada, quando o nível hierárquico admite uma (Estadual,
     * Municipal, Regional e Unidade de Saúde). O nível Federal, que não tem superior, simplesmente
     * não chama este método.
     *
     * Valida que a unidade encontrada é de fato do nível imediatamente superior esperado
     * (Estadual→Federal, Municipal→Estadual, Regional→Municipal, UBS/HOSPITAL→Municipal — ver
     * AQUAQE-22 e ADR-0013). Chaveado pelo tipo real da entidade sendo salva, não pelo tipo do
     * service, porque "Unidade de Saúde" aceita dois tipos (UBS e HOSPITAL) sob o mesmo service.
     */
    protected void vincularSuperiorSeInformado(UnidadeDeSaude unidadeDeSaude, String nomeSuperior) {
        if (nomeSuperior != null && !nomeSuperior.isEmpty()) {
            UnidadeDeSaude superior = unidadeDeSaudeRepository.findByNome(nomeSuperior)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Unidade superior com o nome " + nomeSuperior + " não foi encontrada."));

            TipoUnidadeDeSaude tipoSuperiorEsperado = tipoSuperiorEsperadoPara(unidadeDeSaude.getTipo());
            if (tipoSuperiorEsperado != null && superior.getTipo() != tipoSuperiorEsperado) {
                throw new ResourceUnprocessableEntityException(
                        "A unidade superior \"" + nomeSuperior + "\" precisa ser do nível "
                                + tipoSuperiorEsperado + ", mas é do nível " + superior.getTipo() + ".");
            }

            unidadeDeSaude.setUnidadeSuperior(superior);
        }
    }

    private static TipoUnidadeDeSaude tipoSuperiorEsperadoPara(TipoUnidadeDeSaude tipo) {
        return switch (tipo) {
            case ESTADUAL -> TipoUnidadeDeSaude.FEDERAL;
            case MUNICIPAL -> TipoUnidadeDeSaude.ESTADUAL;
            case REGIONAL -> TipoUnidadeDeSaude.MUNICIPAL;
            case UBS, HOSPITAL, UPA, LABORATORIO, CAPS, CENTRO_ESPECIALIDADES, CENTRO_REABILITACAO,
                 POLICLINICA -> TipoUnidadeDeSaude.MUNICIPAL;
            default -> null;
        };
    }

    /**
     * Vincula a supervisão regional informada (vínculo lateral, não-hierárquico — ver ADR-0013).
     * Diferente de {@link #vincularSuperiorSeInformado}, o tipo esperado é sempre REGIONAL,
     * independente do tipo da unidade sendo salva.
     */
    protected void vincularSupervisaoRegionalSeInformado(UnidadeDeSaude unidadeDeSaude, String nomeSupervisaoRegional) {
        if (nomeSupervisaoRegional != null && !nomeSupervisaoRegional.isEmpty()) {
            UnidadeDeSaude supervisaoRegional = unidadeDeSaudeRepository.findByNome(nomeSupervisaoRegional)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Unidade de supervisão regional com o nome " + nomeSupervisaoRegional + " não foi encontrada."));

            if (supervisaoRegional.getTipo() != TipoUnidadeDeSaude.REGIONAL) {
                throw new ResourceUnprocessableEntityException(
                        "A unidade de supervisão regional \"" + nomeSupervisaoRegional + "\" precisa ser do nível "
                                + TipoUnidadeDeSaude.REGIONAL + ", mas é do nível " + supervisaoRegional.getTipo() + ".");
            }

            unidadeDeSaude.setSupervisaoRegional(supervisaoRegional);
        }
    }

    /**
     * Garante que a unidade sendo salva é de fato de um dos tipos aceitos por este service — o
     * "tipo" do payload é informado pelo cliente e não é reconciliado automaticamente com o
     * endpoint chamado, então sem esta checagem seria possível, por exemplo, criar uma unidade com
     * tipo=FEDERAL através do endpoint /api/v1/estadual/ (ver AQUAQE-214).
     */
    protected RES salvar(UnidadeDeSaude unidadeDeSaude) {
        if (!getTiposAceitos().contains(unidadeDeSaude.getTipo())) {
            throw new ResourceUnprocessableEntityException(
                    "O tipo \"" + unidadeDeSaude.getTipo() + "\" não corresponde ao nível esperado por este "
                            + "endpoint (\"" + getTiposAceitos() + "\").");
        }
        unidadeDeSaude = unidadeDeSaudeRepository.save(unidadeDeSaude);
        return toResponseDto(unidadeDeSaude);
    }

    protected UnidadeDeSaude buscarUnidadeDeSaudePorNome(String nome) {
        return unidadeDeSaudeRepository.findByNomeAndTipoIn(nome, getTiposAceitos())
                .stream()
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar " + nome + " em nossos registros."));
    }
}
