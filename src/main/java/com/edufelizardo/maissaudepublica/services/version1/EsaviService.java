package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Agravo;
import com.edufelizardo.maissaudepublica.models.ContraindicacaoVacinal;
import com.edufelizardo.maissaudepublica.models.Imunobiologico;
import com.edufelizardo.maissaudepublica.models.Notificacao;
import com.edufelizardo.maissaudepublica.models.RegistroVacinacao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoNotificacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NotificacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NotificacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SugestaoNotificacaoDto;
import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoCasoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.CondutaEsquemaEsavi;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemNotificacao;
import com.edufelizardo.maissaudepublica.repositories.ContraindicacaoVacinalRepository;
import com.edufelizardo.maissaudepublica.repositories.NotificacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.RegistroVacinacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;

/**
 * ESAVI e erro de imunização, fatia I4 da Imunização (ADR-0150), dentro do fluxo da Vigilância (ADR-0124): a notificação
 * aponta as doses suspeitas do paciente; o ESAVI tem a gravidade e se encerra com a causalidade da OMS e a conduta sobre o
 * esquema, e a conduta "contraindicar" vira a contraindicação vacinal do paciente. As doses aplicadas fora do calendário
 * viram sugestão de notificação de erro de imunização.
 */
@Service
public class EsaviService {

    public static final String ESAVI = "ESAVI";
    public static final String ERRO_IMUNIZACAO = "ERRO_IMUNIZACAO";

    @Autowired private RegistroVacinacaoRepository registroRepository;
    @Autowired private ContraindicacaoVacinalRepository contraindicacaoRepository;
    @Autowired private NotificacaoRepository notificacaoRepository;

    static boolean esavi(Agravo a) {
        return ESAVI.equals(a.getCodigo());
    }

    /** Os agravos que se ligam à dose. */
    static boolean vacinal(Agravo a) {
        return esavi(a) || ERRO_IMUNIZACAO.equals(a.getCodigo());
    }

    /**
     * Na notificação: o ESAVI e o erro de imunização pedem a dose (400), do paciente, vigente (422), e os sintomas não
     * vêm antes dela (400); o ESAVI pede a gravidade (400). Os outros agravos não têm dose nem gravidade (400).
     */
    void aplicarNaNotificacao(Notificacao n, NotificacaoRequestDto dto) {
        Agravo agravo = n.getAgravo();
        List<UUID> ids = dto.getDoseIds() == null ? List.of() : dto.getDoseIds().stream().filter(Objects::nonNull).distinct().toList();
        if (!vacinal(agravo)) {
            if (!ids.isEmpty()) {
                throw new ResourceBadRequestException("Só o ESAVI e o erro de imunização se ligam à dose de vacina.");
            }
            if (dto.getGravidade() != null) {
                throw new ResourceBadRequestException("A gravidade é só do ESAVI.");
            }
            return;
        }
        if (ids.isEmpty()) {
            throw new ResourceBadRequestException("Informe a dose (ou as doses) de vacina da notificação de " + agravo.getNome() + ".");
        }
        if (esavi(agravo) && dto.getGravidade() == null) {
            throw new ResourceBadRequestException("Informe a gravidade do ESAVI: grave ou não grave.");
        }
        if (!esavi(agravo) && dto.getGravidade() != null) {
            throw new ResourceBadRequestException("A gravidade é só do ESAVI.");
        }
        List<RegistroVacinacao> doses = new ArrayList<>();
        for (UUID id : ids) {
            RegistroVacinacao r = registroRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a dose " + id + " em nossos registros."));
            if (!r.getPaciente().getUuid().equals(n.getPaciente().getUuid())) {
                throw new ResourceUnprocessableEntityException("A dose é de outro paciente.");
            }
            if (r.isAnulada()) {
                throw new ResourceUnprocessableEntityException("A dose foi anulada.");
            }
            if (registroRepository.existsByRetificacaoDe_Uuid(r.getUuid())) {
                throw new ResourceUnprocessableEntityException("A dose foi retificada: use a versão vigente.");
            }
            doses.add(r);
        }
        LocalDate primeira = doses.stream().map(r -> r.getAplicadaEm().toLocalDate()).min(Comparator.naturalOrder()).orElseThrow();
        if (n.getDataPrimeirosSintomas() != null && n.getDataPrimeirosSintomas().isBefore(primeira)) {
            throw new ResourceBadRequestException("Os primeiros sintomas não podem ser antes da dose.");
        }
        n.getDoses().addAll(doses);
        n.setGravidadeEsavi(dto.getGravidade());
    }

    /**
     * No encerramento: o ESAVI pede a causalidade e a conduta (400) e devolve a classificação final que a causalidade dá;
     * contraindicar grava a contraindicação de cada vacina das doses. Nos outros agravos, devolve nulo (400 se vierem).
     */
    ClassificacaoCasoEpidemiologico encerrar(Notificacao n, EncerramentoNotificacaoRequestDto dto) {
        if (!esavi(n.getAgravo())) {
            if (dto.getCausalidade() != null || dto.getConduta() != null) {
                throw new ResourceBadRequestException("A causalidade e a conduta sobre o esquema são só do ESAVI.");
            }
            return null;
        }
        if (dto.getCausalidade() == null || dto.getConduta() == null) {
            throw new ResourceBadRequestException("Para encerrar o ESAVI, informe a causalidade e a conduta sobre o esquema.");
        }
        n.setCausalidadeEsavi(dto.getCausalidade());
        n.setCondutaEsavi(dto.getConduta());
        if (dto.getConduta() == CondutaEsquemaEsavi.CONTRAINDICAR) {
            Map<UUID, Imunobiologico> vacinas = new LinkedHashMap<>();
            n.getDoses().forEach(r -> vacinas.putIfAbsent(r.getImunobiologico().getUuid(), r.getImunobiologico()));
            for (Imunobiologico i : vacinas.values()) {
                if (contraindicacaoRepository.findByPaciente_UuidAndImunobiologico_Uuid(n.getPaciente().getUuid(), i.getUuid()).isPresent()) {
                    continue;
                }
                ContraindicacaoVacinal c = new ContraindicacaoVacinal();
                c.setPaciente(n.getPaciente());
                c.setImunobiologico(i);
                c.setNotificacao(n);
                c.setDesde(LocalDate.now());
                c.setMotivo("ESAVI de " + n.getDataNotificacao() + " encerrado com a causalidade " + dto.getCausalidade() + ".");
                c.setRegistradaEm(Instant.now());
                c.setRegistradoPorCpf(UsuarioAutenticado.cpf());
                contraindicacaoRepository.save(c);
            }
        }
        return dto.getCausalidade().classificacao();
    }

    /** As doses da notificação, na ordem da aplicação. */
    List<NotificacaoDto.Dose> doses(Notificacao n) {
        return n.getDoses().stream().sorted(Comparator.comparing(RegistroVacinacao::getAplicadaEm))
                .map(r -> new NotificacaoDto.Dose(r.getUuid(), r.getImunobiologico().getUuid(), r.getImunobiologico().getSigla(), r.getDose().rotulo(),
                        r.getAplicadaEm(), r.getLote(), r.getUnidade().getNome()))
                .toList();
    }

    /**
     * As doses aplicadas desde o instante fora do calendário (abaixo da idade mínima, acima da máxima ou antes do intervalo
     * mínimo), que entraram com justificativa, ainda sem notificação: sugestão de erro de imunização.
     */
    List<SugestaoNotificacaoDto> sugestoes(Agravo erro, Instant desde, Predicate<UnidadeDeSaude> noEscopo) {
        List<RegistroVacinacao> candidatas = registroRepository.findAplicadasComJustificativaDesde(desde).stream()
                .filter(r -> r.getEsquema() != null && r.getPaciente().getDataNascimento() != null && noEscopo.test(r.getUnidade())).toList();
        if (candidatas.isEmpty()) {
            return List.of();
        }
        Set<UUID> notificadas = new HashSet<>(notificacaoRepository.findDosesNotificadas(candidatas.stream().map(RegistroVacinacao::getUuid).toList()));
        List<SugestaoNotificacaoDto> lista = new ArrayList<>();
        for (RegistroVacinacao r : candidatas) {
            if (notificadas.contains(r.getUuid())) {
                continue;
            }
            List<RegistroVacinacao> outras = registroRepository.findVigentesDoPaciente(r.getPaciente().getUuid()).stream()
                    .filter(x -> !x.getUuid().equals(r.getUuid())).toList();
            if (VacinacaoService.foraDoEsquema(r.getEsquema(), r.getPaciente().getDataNascimento(), r.getAplicadaEm(), r.getImunobiologico(), outras) == null) {
                continue;
            }
            lista.add(new SugestaoNotificacaoDto(OrigemNotificacao.VACINACAO, r.getUuid(), r.getPaciente().getUuid(), r.getPaciente().getNome(),
                    r.getUnidade().getUuid(), r.getUnidade().getNome(), erro.getUuid(), erro.getCodigo(), erro.getNome(), null, r.getRegistradoEm()));
        }
        return lista;
    }
}
