package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AnimalZoonoses;
import com.edufelizardo.maissaudepublica.models.ExameAnimal;
import com.edufelizardo.maissaudepublica.models.Imovel;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.Notificacao;
import com.edufelizardo.maissaudepublica.models.ObservacaoAnimal;
import com.edufelizardo.maissaudepublica.models.VacinacaoAntirrabicaAnimal;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AnimalZoonosesRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DesfechoObservacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ExameAnimalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ObservacaoAnimalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ResultadoExameAnimalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.VacinacaoAntirrabicaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AnimalZoonosesDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ExameAnimalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ObservacaoAnimalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VacinacaoAntirrabicaDto;
import com.edufelizardo.maissaudepublica.models.enuns.DesfechoObservacaoAnimal;
import com.edufelizardo.maissaudepublica.models.enuns.EspecieAnimal;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoExameAnimal;
import com.edufelizardo.maissaudepublica.models.enuns.TipoExameAnimal;
import com.edufelizardo.maissaudepublica.repositories.AnimalZoonosesRepository;
import com.edufelizardo.maissaudepublica.repositories.ExameAnimalRepository;
import com.edufelizardo.maissaudepublica.repositories.ImovelRepository;
import com.edufelizardo.maissaudepublica.repositories.MicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.NotificacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ObservacaoAnimalRepository;
import com.edufelizardo.maissaudepublica.repositories.VacinacaoAntirrabicaAnimalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Animais das zoonoses, fatia V4b da Vigilância (ADR-0163, desenho na ADR-0110): o animal de interesse, a observação do
 * agressor por 10 dias ligada ao atendimento antirrábico notificado, os exames (raiva e leishmaniose visceral canina) e a
 * vacinação antirrábica animal agregada por posto. O tutor não vira paciente.
 */
@Service
public class ZoonosesService {

    private static final String VISTORIAR = ControleVetorialService.VISTORIAR;
    private static final String GERENCIAR = ControleVetorialService.GERENCIAR;
    private static final String INVESTIGAR = "VIGILANCIA.INVESTIGAR";
    /** O período de observação do cão ou do gato agressor (Ministério da Saúde). */
    static final int DIAS_DE_OBSERVACAO = 10;
    private static final Set<TipoExameAnimal> LEISHMANIOSE = Set.of(TipoExameAnimal.LEISHMANIOSE_TR_DPP, TipoExameAnimal.LEISHMANIOSE_ELISA);

    @Autowired private AnimalZoonosesRepository animalRepository;
    @Autowired private ObservacaoAnimalRepository observacaoRepository;
    @Autowired private ExameAnimalRepository exameRepository;
    @Autowired private VacinacaoAntirrabicaAnimalRepository vacinacaoRepository;
    @Autowired private ImovelRepository imovelRepository;
    @Autowired private MicroareaRepository microareaRepository;
    @Autowired private NotificacaoRepository notificacaoRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Animal ─────────────────────────────────────────────────────────────────────────────────────

    /** Os animais ativos; a busca olha o nome, o tutor e o endereço. */
    @Transactional(readOnly = true)
    public List<AnimalZoonosesDto> animais(String busca, EspecieAnimal especie) {
        controleDeAcesso.exigirAlguma(VISTORIAR, GERENCIAR, INVESTIGAR);
        String termo = texto(busca) == null ? null : busca.trim().toLowerCase();
        return animalRepository.findTodos().stream().filter(AnimalZoonoses::isAtivo)
                .filter(a -> especie == null || a.getEspecie() == especie)
                .filter(a -> termo == null || (String.join(" ", vazio(a.getNome()), vazio(a.getTutorNome()), endereco(a))).toLowerCase().contains(termo))
                .map(this::dto).toList();
    }

    /** O imóvel do Território (404) ou o endereço: um dos dois (400). Com o imóvel, a microárea vem dele. */
    @Transactional
    public AnimalZoonosesDto registrarAnimal(AnimalZoonosesRequestDto dto) {
        controleDeAcesso.exigirAlguma(VISTORIAR);
        if (dto.getImovelId() == null && texto(dto.getEndereco()) == null) {
            throw new ResourceBadRequestException("Informe o imóvel do Território ou o endereço do animal.");
        }
        AnimalZoonoses a = new AnimalZoonoses();
        a.setEspecie(dto.getEspecie());
        a.setNome(texto(dto.getNome()));
        a.setDescricao(texto(dto.getDescricao()));
        a.setTutorNome(texto(dto.getTutorNome()));
        a.setTutorTelefone(texto(dto.getTutorTelefone()));
        if (dto.getImovelId() != null) {
            Imovel i = imovelRepository.findById(dto.getImovelId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o imóvel " + dto.getImovelId() + " em nossos registros."));
            a.setImovel(i);
            a.setMicroarea(i.getMicroarea());
        }
        a.setEndereco(texto(dto.getEndereco()));
        a.setAtivo(true);
        a.setRegistradoEm(Instant.now());
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        animalRepository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        return dto(a);
    }

    // ── Observação do agressor ─────────────────────────────────────────────────────────────────────

    /** As observações; abertas filtra; notificacaoId traz as do atendimento antirrábico. */
    @Transactional(readOnly = true)
    public List<ObservacaoAnimalDto> observacoes(Boolean abertas, UUID notificacaoId) {
        controleDeAcesso.exigirAlguma(VISTORIAR, GERENCIAR, INVESTIGAR);
        return observacaoRepository.findTodas().stream()
                .filter(o -> abertas == null || abertas == (o.getDesfecho() == null))
                .filter(o -> notificacaoId == null || (o.getNotificacao() != null && o.getNotificacao().getUuid().equals(notificacaoId)))
                .sorted(Comparator.comparing((ObservacaoAnimal o) -> o.getDesfecho() != null).thenComparing(ObservacaoAnimal::getFimPrevisto))
                .map(this::dto).toList();
    }

    /**
     * Abre a observação: só de cão ou gato (422), sem outra aberta do animal (409); a agressão não é futura (400) e o início
     * não vem antes dela (400); o fim previsto é a agressão mais 10 dias. O atendimento antirrábico é opcional (outro agravo,
     * 422; já com observação aberta, 409).
     */
    @Transactional
    public ObservacaoAnimalDto abrirObservacao(ObservacaoAnimalRequestDto dto) {
        controleDeAcesso.exigirAlguma(VISTORIAR);
        AnimalZoonoses animal = animal(dto.getAnimalId());
        if (animal.getEspecie() != EspecieAnimal.CAO && animal.getEspecie() != EspecieAnimal.GATO) {
            throw new ResourceUnprocessableEntityException("A observação de 10 dias é de cão ou gato; outra espécie vai direto para o exame.");
        }
        if (observacaoRepository.existeAbertaDoAnimal(animal.getUuid())) {
            throw new ResourceConflictException("O animal já está em observação.");
        }
        LocalDate hoje = LocalDate.now();
        if (dto.getDataAgressao().isAfter(hoje)) {
            throw new ResourceBadRequestException("A agressão não pode ser de data futura.");
        }
        LocalDate inicio = dto.getInicio() != null ? dto.getInicio() : hoje;
        if (inicio.isBefore(dto.getDataAgressao())) {
            throw new ResourceBadRequestException("A observação não começa antes da agressão.");
        }
        ObservacaoAnimal o = new ObservacaoAnimal();
        o.setAnimal(animal);
        if (dto.getNotificacaoId() != null) {
            Notificacao n = notificacaoRepository.findById(dto.getNotificacaoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a notificação " + dto.getNotificacaoId() + " em nossos registros."));
            if (!"ATENDIMENTO_ANTIRRABICO".equals(n.getAgravo().getCodigo())) {
                throw new ResourceUnprocessableEntityException("A observação do animal liga-se ao atendimento antirrábico humano.");
            }
            if (observacaoRepository.existeAbertaDaNotificacao(n.getUuid())) {
                throw new ResourceConflictException("Este atendimento já tem um animal em observação.");
            }
            o.setNotificacao(n);
        }
        o.setDataAgressao(dto.getDataAgressao());
        o.setInicio(inicio);
        o.setFimPrevisto(dto.getDataAgressao().plusDays(DIAS_DE_OBSERVACAO));
        o.setObservacao(texto(dto.getObservacao()));
        o.setRegistradoEm(Instant.now());
        o.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        observacaoRepository.save(o);
        ContextoAuditoria.registro(o.getUuid());
        return dto(o);
    }

    /** O desfecho: só da aberta (422); sadio só a partir do fim previsto (422); a data não é futura (400). */
    @Transactional
    public ObservacaoAnimalDto desfecho(UUID id, DesfechoObservacaoRequestDto dto) {
        controleDeAcesso.exigirAlguma(VISTORIAR);
        ObservacaoAnimal o = observacaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a observação " + id + " em nossos registros."));
        if (o.getDesfecho() != null) {
            throw new ResourceUnprocessableEntityException("A observação já foi encerrada.");
        }
        LocalDate data = dto.getData() != null ? dto.getData() : LocalDate.now();
        if (data.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O desfecho não pode ser de data futura.");
        }
        if (dto.getDesfecho() == DesfechoObservacaoAnimal.SADIO && data.isBefore(o.getFimPrevisto())) {
            throw new ResourceUnprocessableEntityException("O animal só é dado como sadio ao fim dos 10 dias, em "
                    + o.getFimPrevisto().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");
        }
        o.setDesfecho(dto.getDesfecho());
        o.setDesfechoEm(data);
        if (texto(dto.getObservacao()) != null) {
            o.setObservacao(texto(dto.getObservacao()));
        }
        observacaoRepository.save(o);
        ContextoAuditoria.registro(o.getUuid());
        ContextoAuditoria.detalhe("Observação do animal agressor: " + dto.getDesfecho() + ".");
        return dto(o);
    }

    // ── Exames ─────────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ExameAnimalDto> exames(TipoExameAnimal tipo, Boolean pendentes) {
        controleDeAcesso.exigirAlguma(VISTORIAR, GERENCIAR, INVESTIGAR);
        return exameRepository.findTodos().stream()
                .filter(e -> tipo == null || e.getTipo() == tipo)
                .filter(e -> pendentes == null || pendentes == (e.getResultado() == null))
                .map(this::dto).toList();
    }

    /** A coleta: data não futura (400); a observação, se houver, é do mesmo animal (422). */
    @Transactional
    public ExameAnimalDto coletar(ExameAnimalRequestDto dto) {
        controleDeAcesso.exigirAlguma(VISTORIAR);
        AnimalZoonoses animal = animal(dto.getAnimalId());
        LocalDate data = dto.getDataColeta() != null ? dto.getDataColeta() : LocalDate.now();
        if (data.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A coleta não pode ser de data futura.");
        }
        ExameAnimal e = new ExameAnimal();
        e.setAnimal(animal);
        if (dto.getObservacaoId() != null) {
            ObservacaoAnimal o = observacaoRepository.findById(dto.getObservacaoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a observação " + dto.getObservacaoId() + " em nossos registros."));
            if (!o.getAnimal().getUuid().equals(animal.getUuid())) {
                throw new ResourceUnprocessableEntityException("A observação é de outro animal.");
            }
            e.setObservacaoAnimal(o);
        }
        e.setTipo(dto.getTipo());
        e.setDataColeta(data);
        e.setObservacao(texto(dto.getObservacao()));
        e.setRegistradoEm(Instant.now());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        exameRepository.save(e);
        ContextoAuditoria.registro(e.getUuid());
        return dto(e);
    }

    /** O resultado; o desfecho do cão só na leishmaniose reagente (422). O resultado pode ser retificado. */
    @Transactional
    public ExameAnimalDto resultado(UUID id, ResultadoExameAnimalRequestDto dto) {
        controleDeAcesso.exigirAlguma(VISTORIAR, GERENCIAR);
        ExameAnimal e = exameRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o exame " + id + " em nossos registros."));
        if (dto.getDesfechoLeishmaniose() != null && (!LEISHMANIOSE.contains(e.getTipo()) || dto.getResultado() != ResultadoExameAnimal.REAGENTE)) {
            throw new ResourceUnprocessableEntityException("O desfecho do cão é do exame de leishmaniose reagente.");
        }
        e.setResultado(dto.getResultado());
        e.setResultadoEm(Instant.now());
        e.setDesfechoLeishmaniose(dto.getDesfechoLeishmaniose());
        exameRepository.save(e);
        ContextoAuditoria.registro(e.getUuid());
        ContextoAuditoria.detalhe("Exame do animal: " + dto.getResultado() + ".");
        return dto(e);
    }

    // ── Vacinação antirrábica animal ───────────────────────────────────────────────────────────────

    /** Os totais do ano (sem ano, o atual), por área, e os registros. */
    @Transactional(readOnly = true)
    public VacinacaoAntirrabicaDto vacinacao(Integer ano) {
        controleDeAcesso.exigirAlguma(VISTORIAR, GERENCIAR, INVESTIGAR, "INDICADOR.CONSULTAR");
        int a = ano != null ? ano : LocalDate.now().getYear();
        List<VacinacaoAntirrabicaAnimal> lista = vacinacaoRepository.findDoAno(a);
        Map<UUID, VacinacaoAntirrabicaDto.Area> areas = new LinkedHashMap<>();
        for (VacinacaoAntirrabicaAnimal v : lista) {
            var area = v.getMicroarea().getArea();
            VacinacaoAntirrabicaDto.Area linha = areas.computeIfAbsent(area.getUuid(), k -> new VacinacaoAntirrabicaDto.Area(area.getUuid(), area.getNome(), 0, 0));
            linha.setCaes(linha.getCaes() + v.getCaes());
            linha.setGatos(linha.getGatos() + v.getGatos());
        }
        return new VacinacaoAntirrabicaDto(a, lista.stream().mapToInt(VacinacaoAntirrabicaAnimal::getCaes).sum(),
                lista.stream().mapToInt(VacinacaoAntirrabicaAnimal::getGatos).sum(),
                areas.values().stream().sorted(Comparator.comparing(VacinacaoAntirrabicaDto.Area::getAreaNome, String.CASE_INSENSITIVE_ORDER)).toList(),
                lista.stream().map(v -> new VacinacaoAntirrabicaDto.Registro(v.getUuid(), v.getData(), v.getMicroarea().getArea().getNome(),
                        v.getMicroarea().getCodigo(), v.getPosto(), v.getCaes(), v.getGatos(), v.getResponsavel())).toList());
    }

    /** O registro do posto: ao menos um animal (400), data não futura (400), microárea existente (404). */
    @Transactional
    public VacinacaoAntirrabicaDto registrarVacinacao(VacinacaoAntirrabicaRequestDto dto) {
        controleDeAcesso.exigirAlguma(VISTORIAR);
        int caes = dto.getCaes() == null ? 0 : dto.getCaes();
        int gatos = dto.getGatos() == null ? 0 : dto.getGatos();
        if (caes + gatos == 0) {
            throw new ResourceBadRequestException("Informe os cães ou os gatos vacinados.");
        }
        LocalDate data = dto.getData() != null ? dto.getData() : LocalDate.now();
        if (data.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A vacinação não pode ser de data futura.");
        }
        Microarea m = microareaRepository.findById(dto.getMicroareaId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a microárea " + dto.getMicroareaId() + " em nossos registros."));
        VacinacaoAntirrabicaAnimal v = new VacinacaoAntirrabicaAnimal(null, data, m, dto.getPosto().trim(), caes, gatos, dto.getResponsavel().trim(),
                Instant.now(), UsuarioAutenticado.cpf());
        vacinacaoRepository.save(v);
        ContextoAuditoria.registro(v.getUuid());
        return vacinacao(data.getYear());
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private AnimalZoonoses animal(UUID id) {
        return animalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o animal " + id + " em nossos registros."));
    }

    private static String endereco(AnimalZoonoses a) {
        return a.getImovel() != null ? ControleVetorialService.endereco(a.getImovel()) : vazio(a.getEndereco());
    }

    private AnimalZoonosesDto dto(AnimalZoonoses a) {
        Microarea m = a.getMicroarea();
        return new AnimalZoonosesDto(a.getUuid(), a.getEspecie(), a.getNome(), a.getDescricao(), a.getTutorNome(), a.getTutorTelefone(),
                a.getImovel() != null ? a.getImovel().getUuid() : null, endereco(a), m != null ? m.getCodigo() : null, m != null ? m.getArea().getNome() : null,
                observacaoRepository.existeAbertaDoAnimal(a.getUuid()), a.getRegistradoEm());
    }

    private ObservacaoAnimalDto dto(ObservacaoAnimal o) {
        AnimalZoonoses a = o.getAnimal();
        return new ObservacaoAnimalDto(o.getUuid(), a.getUuid(), a.getEspecie(), a.getNome(), a.getTutorNome(), a.getTutorTelefone(), endereco(a),
                o.getNotificacao() != null ? o.getNotificacao().getUuid() : null, o.getDataAgressao(), o.getInicio(), o.getFimPrevisto(), o.getDesfecho(),
                o.getDesfechoEm(), o.getObservacao(), o.getDesfecho() == null && LocalDate.now().isAfter(o.getFimPrevisto()));
    }

    private ExameAnimalDto dto(ExameAnimal e) {
        AnimalZoonoses a = e.getAnimal();
        return new ExameAnimalDto(e.getUuid(), a.getUuid(), a.getEspecie(), a.getNome(), endereco(a),
                e.getObservacaoAnimal() != null ? e.getObservacaoAnimal().getUuid() : null, e.getTipo(), e.getDataColeta(), e.getResultado(),
                e.getResultadoEm(), e.getDesfechoLeishmaniose(), e.getObservacao());
    }

    private static String vazio(String s) {
        return s == null ? "" : s;
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
