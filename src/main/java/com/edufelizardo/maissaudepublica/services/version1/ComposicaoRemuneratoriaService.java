package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.AjusteIndividual;
import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.Lotacao;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.RegraAnuenio;
import com.edufelizardo.maissaudepublica.models.TabelaSalarial;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AjusteIndividualResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ComposicaoRemuneratoriaResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AjusteIndividualRepository;
import com.edufelizardo.maissaudepublica.repositories.LotacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.RegraAnuenioRepository;
import com.edufelizardo.maissaudepublica.repositories.TabelaSalarialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Composição derivada pra exibição — soma TabelaSalarial vigente + anuênio calculado pela
 * RegraAnuenio + AjusteIndividual vigentes (ver docs/rh/MODELO-RH.md seção 2.4). Não persiste
 * nada, não é a Folha de pagamento (essa continua sendo campos de registro, fase 5).
 */
@Service
public class ComposicaoRemuneratoriaService {

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private LotacaoRepository lotacaoRepository;

    @Autowired
    private TabelaSalarialRepository tabelaSalarialRepository;

    @Autowired
    private RegraAnuenioRepository regraAnuenioRepository;

    @Autowired
    private AjusteIndividualRepository ajusteIndividualRepository;

    public ComposicaoRemuneratoriaResponseDto calcular(String matricula) {
        Profissional profissional = profissionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));

        Lotacao lotacaoVigente = lotacaoRepository.findByProfissional_MatriculaAndDataFimIsNull(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma lotação vigente para o profissional de matrícula " + matricula + " em nossos registros."));

        Cargo cargo = lotacaoVigente.getCargo();

        TabelaSalarial tabelaVigente = tabelaSalarialRepository
                .findFirstByCargo_UuidAndDataVigenciaLessThanEqualOrderByDataVigenciaDesc(cargo.getUuid(), LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um valor de tabela salarial vigente para o cargo " + cargo.getNome() + " em nossos registros."));

        BigDecimal valorBase = tabelaVigente.getValorBase();

        Integer anosCompletos = null;
        BigDecimal percentualAnuenio = null;
        BigDecimal valorAnuenio = BigDecimal.ZERO;
        if (profissional.getDataAdmissao() != null) {
            anosCompletos = Period.between(profissional.getDataAdmissao(), LocalDate.now()).getYears();
            RegraAnuenio regra = regraAnuenioRepository.findByCategoria_Uuid(cargo.getCategoria().getUuid()).orElse(null);
            if (regra != null) {
                percentualAnuenio = regra.getPercentualPorAno();
                int anosConsiderados = regra.getTetoAnos() != null ? Math.min(anosCompletos, regra.getTetoAnos()) : anosCompletos;
                valorAnuenio = percentualAnuenio
                        .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(anosConsiderados))
                        .multiply(valorBase)
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }

        LocalDate hoje = LocalDate.now();
        List<AjusteIndividual> ajustesVigentes = ajusteIndividualRepository
                .findByProfissional_MatriculaOrderByDataInicioDesc(matricula)
                .stream()
                .filter(a -> !a.getDataInicio().isAfter(hoje) && (a.getDataFim() == null || !a.getDataFim().isBefore(hoje)))
                .collect(Collectors.toList());

        BigDecimal totalAjustes = ajustesVigentes.stream()
                .map(AjusteIndividual::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = valorBase.add(valorAnuenio).add(totalAjustes);

        return new ComposicaoRemuneratoriaResponseDto(
                profissional.getMatricula(),
                profissional.getNome(),
                cargo.getNome(),
                cargo.getCategoria().getNome(),
                valorBase,
                anosCompletos,
                percentualAnuenio,
                valorAnuenio,
                ajustesVigentes.stream().map(AjusteIndividualResponseDto::fromAjusteIndividual).collect(Collectors.toList()),
                totalAjustes,
                total
        );
    }
}
