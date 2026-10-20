package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Area;
import com.edufelizardo.maissaudepublica.models.Domicilio;
import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.Imovel;
import com.edufelizardo.maissaudepublica.models.MembroNucleo;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.NucleoFamiliar;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DomicilioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FamiliaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ImovelRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MembroFamiliaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MudancaFamiliaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RendaFamiliaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ResponsavelFamiliaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SaidaMembroFamiliaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CadastroTerritorialDto;
import com.edufelizardo.maissaudepublica.models.enuns.FinalidadeTerritorio;
import com.edufelizardo.maissaudepublica.models.enuns.Parentesco;
import com.edufelizardo.maissaudepublica.models.enuns.TipoImovel;
import com.edufelizardo.maissaudepublica.repositories.AreaRepository;
import com.edufelizardo.maissaudepublica.repositories.DomicilioRepository;
import com.edufelizardo.maissaudepublica.repositories.ImovelRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroNucleoRepository;
import com.edufelizardo.maissaudepublica.repositories.MicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.NucleoFamiliarRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ResponsabilidadeTerritorialRepository;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Cadastro territorial, fatia F3a (ADR-0127): imóvel na microárea (o ponto, quando há, dentro dela), domicílio com as
 * condições, família com início e fim no domicílio e os membros, que são os pacientes que já existem. Cadastrar pede
 * {@value #CADASTRAR} na unidade da equipe responsável pela área; ler família e domicílio pede o cadastro ou a gestão do
 * território no escopo, e o detalhe entra na auditoria (dado pessoal).
 */
@Service
public class CadastroTerritorialService {

    public static final String CADASTRAR = "TERRITORIO.CADASTRAR";
    static final String[] LER = {CADASTRAR, TerritorioService.GERENCIAR};

    @Autowired
    private ImovelRepository imovelRepository;

    @Autowired
    private DomicilioRepository domicilioRepository;

    @Autowired
    private NucleoFamiliarRepository nucleoRepository;

    @Autowired
    private MembroNucleoRepository membroRepository;

    @Autowired
    private MicroareaRepository microareaRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private ResponsabilidadeTerritorialRepository responsabilidadeRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /** Imóveis da área (ou de uma microárea dela), com as contagens. */
    @Transactional(readOnly = true)
    public List<CadastroTerritorialDto.Imovel> imoveis(UUID areaId, UUID microareaId) {
        Area area = areaRepository.findById(areaId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma área com o id " + areaId + " em nossos registros."));
        controleDeAcesso.exigirVisivel(unidadeDaArea(area), LER);
        List<Imovel> imoveis = imovelRepository.findByMicroarea_Area_Uuid(areaId).stream()
                .filter(i -> microareaId == null || i.getMicroarea().getUuid().equals(microareaId))
                .sorted(Comparator.comparing((Imovel i) -> i.getMicroarea().getCodigo())
                        .thenComparing(i -> Objects.toString(i.getEndereco() != null ? i.getEndereco().getLogradouro() : "", ""), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(i -> Objects.toString(i.getEndereco() != null ? i.getEndereco().getNumeroLogradouro() : "", "")))
                .toList();
        Map<UUID, List<Domicilio>> domicilios = domicilioRepository.findByImovel_UuidIn(imoveis.stream().map(Imovel::getUuid).toList()).stream()
                .collect(Collectors.groupingBy(d -> d.getImovel().getUuid()));
        List<UUID> idsDomicilios = domicilios.values().stream().flatMap(List::stream).map(Domicilio::getUuid).toList();
        List<NucleoFamiliar> nucleos = idsDomicilios.isEmpty() ? List.of() : nucleoRepository.findByDomicilio_UuidInOrderByInicioDesc(idsDomicilios).stream()
                .filter(NucleoFamiliar::vigente).toList();
        Map<UUID, Long> pessoasPorNucleo = nucleos.isEmpty() ? Map.of() : membroRepository.findByNucleo_UuidInOrderByInicioAsc(nucleos.stream().map(NucleoFamiliar::getUuid).toList())
                .stream().filter(MembroNucleo::vigente).collect(Collectors.groupingBy(m -> m.getNucleo().getUuid(), Collectors.counting()));
        return imoveis.stream().map(i -> {
            List<NucleoFamiliar> doImovel = nucleos.stream().filter(n -> n.getDomicilio().getImovel().getUuid().equals(i.getUuid())).toList();
            return imovel(i, domicilios.getOrDefault(i.getUuid(), List.of()).size(), doImovel.size(),
                    (int) doImovel.stream().mapToLong(n -> pessoasPorNucleo.getOrDefault(n.getUuid(), 0L)).sum());
        }).toList();
    }

    @Transactional(readOnly = true)
    public CadastroTerritorialDto.ImovelDetalhe imovel(UUID uuid) {
        Imovel i = buscarImovel(uuid);
        controleDeAcesso.exigirVisivel(unidadeDaArea(i.getMicroarea().getArea()), LER);
        ContextoAuditoria.registro(i.getUuid());
        return detalhe(i);
    }

    @Transactional(readOnly = true)
    public CadastroTerritorialDto.FamiliaDoPaciente doPaciente(UUID pacienteId) {
        Paciente p = buscarPaciente(pacienteId);
        ContextoAuditoria.paciente(p.getUuid());
        List<MembroNucleo> historico = membroRepository.findByPaciente_UuidOrderByInicioDesc(p.getUuid());
        MembroNucleo atual = historico.stream().filter(MembroNucleo::vigente).findFirst().orElse(null);
        if (atual != null) {
            controleDeAcesso.exigirVisivel(unidadeDaArea(atual.getNucleo().getDomicilio().getImovel().getMicroarea().getArea()), LER);
        } else {
            controleDeAcesso.exigirAlguma(LER);
        }
        List<CadastroTerritorialDto.Moradia> moradias = historico.stream().map(m -> {
            Imovel i = m.getNucleo().getDomicilio().getImovel();
            return new CadastroTerritorialDto.Moradia(m.getNucleo().getUuid(), textoEndereco(i.getEndereco(), m.getNucleo().getDomicilio().getIdentificacao()),
                    i.getMicroarea().getCodigo(), m.getInicio(), m.getFim());
        }).toList();
        if (atual == null) {
            return new CadastroTerritorialDto.FamiliaDoPaciente(p.getUuid(), null, null, null, null, null, null, moradias);
        }
        Imovel i = atual.getNucleo().getDomicilio().getImovel();
        return new CadastroTerritorialDto.FamiliaDoPaciente(p.getUuid(), familia(atual.getNucleo()), i.getUuid(),
                textoEndereco(i.getEndereco(), atual.getNucleo().getDomicilio().getIdentificacao()), i.getMicroarea().getArea().getUuid(),
                i.getMicroarea().getArea().getCodigo(), i.getMicroarea().getCodigo(), moradias);
    }

    // ── Imóvel e domicílio ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public CadastroTerritorialDto.ImovelDetalhe criarImovel(ImovelRequestDto dto) {
        Imovel i = new Imovel();
        i.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        aplicar(i, dto);
        i = imovelRepository.save(i);
        ContextoAuditoria.registro(i.getUuid());
        return detalhe(i);
    }

    /** Edita tipo, endereço, ponto, microárea (dentro da mesma área) e situação. Imóvel com domicílio continua de moradia. */
    @Transactional
    public CadastroTerritorialDto.ImovelDetalhe atualizarImovel(UUID uuid, ImovelRequestDto dto) {
        Imovel i = buscarImovel(uuid);
        exigirCadastro(i.getMicroarea().getArea());
        UUID areaAntes = i.getMicroarea().getArea().getUuid();
        aplicar(i, dto);
        if (!i.getMicroarea().getArea().getUuid().equals(areaAntes)) {
            throw new ResourceUnprocessableEntityException("O imóvel não muda de área pela edição; ele fica em outra microárea da mesma área.");
        }
        if (i.getTipo() != TipoImovel.DOMICILIO && !domicilioRepository.findByImovel_UuidOrderByIdentificacaoAsc(i.getUuid()).isEmpty()) {
            throw new ResourceUnprocessableEntityException("O imóvel tem domicílio cadastrado; ele continua sendo de moradia.");
        }
        ContextoAuditoria.registro(i.getUuid());
        return detalhe(imovelRepository.save(i));
    }

    @Transactional
    public CadastroTerritorialDto.ImovelDetalhe criarDomicilio(DomicilioRequestDto dto) {
        Imovel i = buscarImovel(dto.getImovelId());
        exigirCadastro(i.getMicroarea().getArea());
        if (i.getTipo() != TipoImovel.DOMICILIO) {
            throw new ResourceUnprocessableEntityException("O imóvel é " + i.getTipo() + "; domicílio só em imóvel de moradia.");
        }
        if (!i.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O imóvel está inativo.");
        }
        Domicilio d = new Domicilio();
        d.setImovel(i);
        aplicar(d, dto);
        d = domicilioRepository.save(d);
        ContextoAuditoria.registro(d.getUuid());
        return detalhe(i);
    }

    @Transactional
    public CadastroTerritorialDto.ImovelDetalhe atualizarDomicilio(UUID uuid, DomicilioRequestDto dto) {
        Domicilio d = buscarDomicilio(uuid);
        exigirCadastro(d.getImovel().getMicroarea().getArea());
        if (!dto.getImovelId().equals(d.getImovel().getUuid())) {
            throw new ResourceUnprocessableEntityException("O imóvel do domicílio não muda pela edição.");
        }
        aplicar(d, dto);
        if (!d.isAtivo() && !familiasVigentes(d).isEmpty()) {
            throw new ResourceUnprocessableEntityException("O domicílio tem família morando; registre a mudança antes de inativar.");
        }
        domicilioRepository.save(d);
        ContextoAuditoria.registro(d.getUuid());
        return detalhe(d.getImovel());
    }

    // ── Família ────────────────────────────────────────────────────────────────────────────────────

    /** Família nova no domicílio: o responsável entra como membro, e os outros com o parentesco. */
    @Transactional
    public CadastroTerritorialDto.ImovelDetalhe criarFamilia(FamiliaRequestDto dto) {
        Domicilio d = buscarDomicilio(dto.getDomicilioId());
        exigirCadastro(d.getImovel().getMicroarea().getArea());
        exigirMoradiaAtiva(d);
        LocalDate inicio = data(dto.getInicio(), "O início");
        Paciente responsavel = buscarPaciente(dto.getResponsavelId());
        List<MembroFamiliaRequestDto> outros = dto.getMembros() == null ? List.of() : dto.getMembros();
        Set<UUID> vistos = new HashSet<>(Set.of(responsavel.getUuid()));
        for (MembroFamiliaRequestDto m : outros) {
            if (m.getParentesco() == Parentesco.RESPONSAVEL) {
                throw new ResourceBadRequestException("O responsável é um só; informe o parentesco dos outros membros com ele.");
            }
            if (!vistos.add(m.getPacienteId())) {
                throw new ResourceBadRequestException("A mesma pessoa aparece duas vezes na família.");
            }
        }
        exigirSemFamilia(responsavel);
        NucleoFamiliar n = nucleoRepository.save(new NucleoFamiliar(null, d, responsavel, dto.getRendaFamiliar(), inicio, null, null, UsuarioAutenticado.cpf()));
        membroRepository.save(new MembroNucleo(null, n, responsavel, Parentesco.RESPONSAVEL, inicio, null, null, UsuarioAutenticado.cpf()));
        for (MembroFamiliaRequestDto m : outros) {
            Paciente p = buscarPaciente(m.getPacienteId());
            exigirSemFamilia(p);
            membroRepository.save(new MembroNucleo(null, n, p, m.getParentesco(), inicio, null, null, UsuarioAutenticado.cpf()));
        }
        ContextoAuditoria.registro(n.getUuid());
        ContextoAuditoria.detalhe("Família de " + responsavel.getNome() + " com " + (outros.size() + 1) + " pessoa(s).");
        return detalhe(d.getImovel());
    }

    @Transactional
    public CadastroTerritorialDto.ImovelDetalhe atualizarRenda(UUID familiaId, RendaFamiliaRequestDto dto) {
        NucleoFamiliar n = buscarFamiliaVigente(familiaId);
        exigirCadastro(n.getDomicilio().getImovel().getMicroarea().getArea());
        n.setRendaFamiliar(dto.getRendaFamiliar());
        nucleoRepository.save(n);
        ContextoAuditoria.registro(n.getUuid());
        return detalhe(n.getDomicilio().getImovel());
    }

    @Transactional
    public CadastroTerritorialDto.ImovelDetalhe incluirMembro(UUID familiaId, MembroFamiliaRequestDto dto) {
        NucleoFamiliar n = buscarFamiliaVigente(familiaId);
        exigirCadastro(n.getDomicilio().getImovel().getMicroarea().getArea());
        if (dto.getParentesco() == Parentesco.RESPONSAVEL) {
            throw new ResourceBadRequestException("Para mudar o responsável, use a troca de responsável.");
        }
        Paciente p = buscarPaciente(dto.getPacienteId());
        exigirSemFamilia(p);
        LocalDate inicio = data(dto.getInicio(), "A entrada");
        if (inicio.isBefore(n.getInicio())) {
            throw new ResourceBadRequestException("A entrada não pode ser antes do início da família (" + n.getInicio() + ").");
        }
        membroRepository.save(new MembroNucleo(null, n, p, dto.getParentesco(), inicio, null, null, UsuarioAutenticado.cpf()));
        ContextoAuditoria.registro(n.getUuid());
        ContextoAuditoria.paciente(p.getUuid());
        return detalhe(n.getDomicilio().getImovel());
    }

    /**
     * Saída de uma pessoa, com motivo. O responsável só sai por último (antes, troque o responsável; 422); saindo o
     * último, a família se encerra.
     */
    @Transactional
    public CadastroTerritorialDto.ImovelDetalhe registrarSaida(UUID membroId, SaidaMembroFamiliaRequestDto dto) {
        MembroNucleo m = buscarMembro(membroId);
        NucleoFamiliar n = m.getNucleo();
        exigirCadastro(n.getDomicilio().getImovel().getMicroarea().getArea());
        if (!m.vigente()) {
            throw new ResourceUnprocessableEntityException("Esta pessoa já saiu da família.");
        }
        LocalDate fim = data(dto.getFim(), "A saída");
        if (fim.isBefore(m.getInicio())) {
            throw new ResourceBadRequestException("A saída precisa ser depois da entrada (" + m.getInicio() + ").");
        }
        List<MembroNucleo> vigentes = membroRepository.findByNucleo_UuidAndFimIsNull(n.getUuid());
        boolean responsavel = n.getResponsavel().getUuid().equals(m.getPaciente().getUuid());
        if (responsavel && vigentes.size() > 1) {
            throw new ResourceUnprocessableEntityException(m.getPaciente().getNome()
                    + " é o responsável; defina outro responsável antes da saída.");
        }
        String motivo = dto.getMotivo().trim();
        m.setFim(fim);
        m.setMotivoSaida(motivo);
        membroRepository.save(m);
        if (vigentes.size() == 1) {
            n.setFim(fim);
            n.setMotivoFim("Família sem membros: " + motivo);
            nucleoRepository.save(n);
        }
        ContextoAuditoria.registro(n.getUuid());
        ContextoAuditoria.paciente(m.getPaciente().getUuid());
        return detalhe(n.getDomicilio().getImovel());
    }

    @Transactional
    public CadastroTerritorialDto.ImovelDetalhe trocarResponsavel(UUID familiaId, ResponsavelFamiliaRequestDto dto) {
        NucleoFamiliar n = buscarFamiliaVigente(familiaId);
        exigirCadastro(n.getDomicilio().getImovel().getMicroarea().getArea());
        if (dto.getParentescoDoAnterior() == Parentesco.RESPONSAVEL) {
            throw new ResourceBadRequestException("Informe o parentesco do responsável anterior com o novo.");
        }
        MembroNucleo novo = buscarMembro(dto.getMembroId());
        if (!novo.getNucleo().getUuid().equals(n.getUuid()) || !novo.vigente()) {
            throw new ResourceUnprocessableEntityException("O novo responsável precisa ser membro vigente desta família.");
        }
        if (novo.getPaciente().getUuid().equals(n.getResponsavel().getUuid())) {
            throw new ResourceConflictException(novo.getPaciente().getNome() + " já é o responsável.");
        }
        membroRepository.findByNucleo_UuidAndFimIsNull(n.getUuid()).stream()
                .filter(m -> m.getPaciente().getUuid().equals(n.getResponsavel().getUuid()))
                .findFirst().ifPresent(anterior -> {
                    anterior.setParentesco(dto.getParentescoDoAnterior());
                    membroRepository.save(anterior);
                });
        novo.setParentesco(Parentesco.RESPONSAVEL);
        membroRepository.save(novo);
        n.setResponsavel(novo.getPaciente());
        nucleoRepository.save(n);
        ContextoAuditoria.registro(n.getUuid());
        return detalhe(n.getDomicilio().getImovel());
    }

    /**
     * Mudança da família inteira: a família se encerra no domicílio de origem e recomeça no destino com os mesmos membros,
     * parentescos e renda. Para uma pessoa só, saída e entrada em outra família.
     */
    @Transactional
    public CadastroTerritorialDto.ImovelDetalhe mudar(UUID familiaId, MudancaFamiliaRequestDto dto) {
        NucleoFamiliar origem = buscarFamiliaVigente(familiaId);
        exigirCadastro(origem.getDomicilio().getImovel().getMicroarea().getArea());
        Domicilio destino = buscarDomicilio(dto.getDomicilioId());
        exigirCadastro(destino.getImovel().getMicroarea().getArea());
        exigirMoradiaAtiva(destino);
        if (destino.getUuid().equals(origem.getDomicilio().getUuid())) {
            throw new ResourceUnprocessableEntityException("A família já mora neste domicílio.");
        }
        LocalDate data = data(dto.getData(), "A mudança");
        if (data.isBefore(origem.getInicio())) {
            throw new ResourceBadRequestException("A mudança não pode ser antes do início da família (" + origem.getInicio() + ").");
        }
        String para = textoEndereco(destino.getImovel().getEndereco(), destino.getIdentificacao());
        String motivo = "Mudança para " + para + (dto.getMotivo() == null || dto.getMotivo().isBlank() ? "" : ": " + dto.getMotivo().trim());
        motivo = motivo.length() > 500 ? motivo.substring(0, 500) : motivo;
        List<MembroNucleo> membros = membroRepository.findByNucleo_UuidAndFimIsNull(origem.getUuid());
        for (MembroNucleo m : membros) {
            m.setFim(data);
            m.setMotivoSaida(motivo);
            membroRepository.saveAndFlush(m);
        }
        origem.setFim(data);
        origem.setMotivoFim(motivo);
        nucleoRepository.save(origem);
        NucleoFamiliar novo = nucleoRepository.save(new NucleoFamiliar(null, destino, origem.getResponsavel(), origem.getRendaFamiliar(), data, null, null,
                UsuarioAutenticado.cpf()));
        for (MembroNucleo m : membros) {
            membroRepository.save(new MembroNucleo(null, novo, m.getPaciente(), m.getParentesco(), data, null, null, UsuarioAutenticado.cpf()));
        }
        ContextoAuditoria.registro(novo.getUuid());
        ContextoAuditoria.detalhe(motivo);
        return detalhe(destino.getImovel());
    }

    /** Copia o endereço do imóvel para o cadastro do paciente: ação explícita, o cadastro não muda sozinho. */
    @Transactional
    public CadastroTerritorialDto.ImovelDetalhe atualizarEnderecoDoPaciente(UUID membroId) {
        MembroNucleo m = buscarMembro(membroId);
        Imovel i = m.getNucleo().getDomicilio().getImovel();
        exigirCadastro(i.getMicroarea().getArea());
        controleDeAcesso.exigirAlguma("PACIENTE.CADASTRAR");
        if (!m.vigente()) {
            throw new ResourceUnprocessableEntityException("Esta pessoa não mora mais aqui.");
        }
        Endereco e = i.getEndereco();
        String identificacao = m.getNucleo().getDomicilio().getIdentificacao();
        String complemento = Stream.of(e.getComplemento(), identificacao).filter(s -> s != null && !s.isBlank()).collect(Collectors.joining(", "));
        Paciente p = m.getPaciente();
        String ddd = p.getEndereco() != null ? p.getEndereco().getDdd() : e.getDdd();
        p.setEndereco(new Endereco(e.getCep(), e.getLogradouro(), e.getNumeroLogradouro(), complemento.isBlank() ? null : complemento, e.getBairro(),
                e.getCidade(), e.getEstado(), ddd));
        pacienteRepository.save(p);
        ContextoAuditoria.paciente(p.getUuid());
        ContextoAuditoria.detalhe("Endereço do cadastro atualizado pelo imóvel.");
        return detalhe(i);
    }

    // ── Regras ─────────────────────────────────────────────────────────────────────────────────────

    /**
     * Microárea e ponto: com microárea e ponto, o ponto cai nela quando ela tem desenho (422); sem microárea, o ponto a
     * encontra entre as microáreas desenhadas de território ativo de atenção primária (sem achar, 400).
     */
    private void aplicar(Imovel i, ImovelRequestDto dto) {
        if ((dto.getLatitude() == null) != (dto.getLongitude() == null)) {
            throw new ResourceBadRequestException("Informe a latitude e a longitude juntas, ou nenhuma das duas.");
        }
        boolean temPonto = dto.getLatitude() != null;
        Microarea m;
        if (dto.getMicroareaId() != null) {
            m = microareaRepository.findById(dto.getMicroareaId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar uma microárea com o id " + dto.getMicroareaId() + " em nossos registros."));
            Geometry g = GeometriaTerritorial.jts(m.getGeometria());
            if (temPonto && g != null && !GeometriaTerritorial.contem(g, dto.getLatitude(), dto.getLongitude())) {
                throw new ResourceUnprocessableEntityException("O ponto marcado fica fora da microárea " + m.getCodigo() + ".");
            }
        } else if (temPonto) {
            m = microareaRepository.findAllByOrderByCodigoAsc().stream()
                    .filter(x -> x.getGeometria() != null && x.getArea().getTerritorio().isAtivo()
                            && x.getArea().getTerritorio().getFinalidade() == FinalidadeTerritorio.ATENCAO_PRIMARIA)
                    .filter(x -> GeometriaTerritorial.contem(GeometriaTerritorial.jts(x.getGeometria()), dto.getLatitude(), dto.getLongitude()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceBadRequestException("O ponto não cai em nenhuma microárea desenhada; escolha a microárea."));
        } else {
            throw new ResourceBadRequestException("Informe a microárea ou marque o ponto no mapa.");
        }
        exigirCadastro(m.getArea());
        i.setMicroarea(m);
        i.setTipo(dto.getTipo());
        i.setEndereco(new Endereco(dto.getEndereco()));
        i.setLatitude(dto.getLatitude());
        i.setLongitude(dto.getLongitude());
        i.setAtivo(dto.getAtivo() == null || dto.getAtivo());
    }

    private void aplicar(Domicilio d, DomicilioRequestDto dto) {
        String identificacao = dto.getIdentificacao() == null || dto.getIdentificacao().isBlank() ? null : dto.getIdentificacao().trim();
        if (identificacao != null && !identificacao.equalsIgnoreCase(Objects.toString(d.getIdentificacao(), ""))
                && domicilioRepository.existsByImovel_UuidAndIdentificacaoIgnoreCase(d.getImovel().getUuid(), identificacao)) {
            throw new ResourceConflictException("Já existe o domicílio \"" + identificacao + "\" neste imóvel.");
        }
        d.setIdentificacao(identificacao);
        d.setSituacao(dto.getSituacao());
        d.setLocalizacao(dto.getLocalizacao());
        d.setComodos(dto.getComodos());
        d.setAbastecimentoAgua(dto.getAbastecimentoAgua());
        d.setEscoamentoSanitario(dto.getEscoamentoSanitario());
        d.setDestinoLixo(dto.getDestinoLixo());
        d.setEnergiaEletrica(dto.getEnergiaEletrica());
        d.setAtivo(dto.getAtivo() == null || dto.getAtivo());
    }

    /** A unidade que dá o escopo da área: a da equipe responsável, ou a de gestão do segmento. */
    private UnidadeDeSaude unidadeDaArea(Area a) {
        return responsabilidadeRepository.findByArea_UuidAndFimIsNull(a.getUuid()).map(r -> r.getEquipe().getUnidade())
                .orElse(a.getTerritorio().getSegmento().getUnidadeGestao());
    }

    /** O cadastro é da equipe: sem equipe responsável pela área, 422. */
    private void exigirCadastro(Area a) {
        UnidadeDeSaude unidade = responsabilidadeRepository.findByArea_UuidAndFimIsNull(a.getUuid()).map(r -> r.getEquipe().getUnidade())
                .orElseThrow(() -> new ResourceUnprocessableEntityException("A área " + a.getCodigo()
                        + " não tem equipe responsável; o cadastro domiciliar é feito pela equipe."));
        controleDeAcesso.exigir(CADASTRAR, unidade);
    }

    private void exigirMoradiaAtiva(Domicilio d) {
        if (!d.isAtivo() || !d.getImovel().isAtivo()) {
            throw new ResourceUnprocessableEntityException("O domicílio está inativo.");
        }
    }

    private void exigirSemFamilia(Paciente p) {
        membroRepository.findByPaciente_UuidAndFimIsNull(p.getUuid()).ifPresent(m -> {
            throw new ResourceConflictException(p.getNome() + " já está na família de " + m.getNucleo().getResponsavel().getNome()
                    + "; registre a saída de lá antes.");
        });
    }

    private static LocalDate data(LocalDate informada, String campo) {
        LocalDate d = informada != null ? informada : LocalDate.now();
        if (d.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException(campo + " não pode ser no futuro.");
        }
        return d;
    }

    // ── Montagem ───────────────────────────────────────────────────────────────────────────────────

    private CadastroTerritorialDto.ImovelDetalhe detalhe(Imovel i) {
        List<Domicilio> domicilios = domicilioRepository.findByImovel_UuidOrderByIdentificacaoAsc(i.getUuid());
        List<NucleoFamiliar> nucleos = domicilios.isEmpty() ? List.of()
                : nucleoRepository.findByDomicilio_UuidInOrderByInicioDesc(domicilios.stream().map(Domicilio::getUuid).toList());
        Map<UUID, List<MembroNucleo>> membros = nucleos.isEmpty() ? Map.of()
                : membroRepository.findByNucleo_UuidInOrderByInicioAsc(nucleos.stream().map(NucleoFamiliar::getUuid).toList()).stream()
                .collect(Collectors.groupingBy(m -> m.getNucleo().getUuid()));
        List<CadastroTerritorialDto.Domicilio> lista = new ArrayList<>();
        int familias = 0;
        int pessoas = 0;
        for (Domicilio d : domicilios) {
            List<CadastroTerritorialDto.Familia> atuais = new ArrayList<>();
            List<CadastroTerritorialDto.Familia> anteriores = new ArrayList<>();
            for (NucleoFamiliar n : nucleos) {
                if (!n.getDomicilio().getUuid().equals(d.getUuid())) {
                    continue;
                }
                CadastroTerritorialDto.Familia f = familia(n, membros.getOrDefault(n.getUuid(), List.of()), i.getEndereco());
                if (n.vigente()) {
                    atuais.add(f);
                    familias++;
                    pessoas += f.getMembros().size();
                } else {
                    anteriores.add(f);
                }
            }
            lista.add(new CadastroTerritorialDto.Domicilio(d.getUuid(), i.getUuid(), d.getIdentificacao(), d.getSituacao(), d.getLocalizacao(), d.getComodos(),
                    d.getAbastecimentoAgua(), d.getEscoamentoSanitario(), d.getDestinoLixo(), d.getEnergiaEletrica(), d.isAtivo(), atuais, anteriores));
        }
        Area a = i.getMicroarea().getArea();
        return new CadastroTerritorialDto.ImovelDetalhe(imovel(i, domicilios.size(), familias, pessoas), a.getUuid(), a.getCodigo(), lista);
    }

    private CadastroTerritorialDto.Familia familia(NucleoFamiliar n) {
        return familia(n, membroRepository.findByNucleo_UuidInOrderByInicioAsc(List.of(n.getUuid())), n.getDomicilio().getImovel().getEndereco());
    }

    private static CadastroTerritorialDto.Familia familia(NucleoFamiliar n, List<MembroNucleo> membros, Endereco doImovel) {
        Comparator<MembroNucleo> ordem = Comparator.comparing((MembroNucleo m) -> m.getParentesco() != Parentesco.RESPONSAVEL)
                .thenComparing(m -> m.getPaciente().getNome(), String.CASE_INSENSITIVE_ORDER);
        return new CadastroTerritorialDto.Familia(n.getUuid(), n.getDomicilio().getUuid(), n.getResponsavel().getUuid(), n.getResponsavel().getNome(),
                n.getRendaFamiliar(), n.getInicio(), n.getFim(), n.getMotivoFim(),
                membros.stream().filter(MembroNucleo::vigente).sorted(ordem).map(m -> membro(m, doImovel)).toList(),
                membros.stream().filter(m -> !m.vigente()).sorted(Comparator.comparing(MembroNucleo::getFim).reversed()).map(m -> membro(m, doImovel)).toList());
    }

    private static CadastroTerritorialDto.Membro membro(MembroNucleo m, Endereco doImovel) {
        Paciente p = m.getPaciente();
        return new CadastroTerritorialDto.Membro(m.getUuid(), p.getUuid(), p.getNome(), p.getDataNascimento(), m.getParentesco(), m.getInicio(),
                m.getFim(), m.getMotivoSaida(), m.vigente() && diverge(p.getEndereco(), doImovel));
    }

    /** O cadastro do paciente aponta outro endereço: CEP, logradouro ou número diferentes. */
    static boolean diverge(Endereco paciente, Endereco imovel) {
        if (imovel == null) {
            return false;
        }
        if (paciente == null) {
            return true;
        }
        return !digitos(paciente.getCep()).equals(digitos(imovel.getCep()))
                || !normal(paciente.getLogradouro()).equals(normal(imovel.getLogradouro()))
                || !normal(paciente.getNumeroLogradouro()).equals(normal(imovel.getNumeroLogradouro()));
    }

    private static String digitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    private static String normal(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static CadastroTerritorialDto.Imovel imovel(Imovel i, int domicilios, int familias, int pessoas) {
        return new CadastroTerritorialDto.Imovel(i.getUuid(), i.getMicroarea().getUuid(), i.getMicroarea().getCodigo(), i.getTipo(), i.getEndereco(),
                textoEndereco(i.getEndereco(), null), i.getLatitude(), i.getLongitude(), i.isAtivo(), domicilios, familias, pessoas);
    }

    static String textoEndereco(Endereco e, String identificacao) {
        if (e == null) {
            return identificacao == null ? "" : identificacao;
        }
        String base = Stream.of(e.getLogradouro(), e.getNumeroLogradouro()).filter(s -> s != null && !s.isBlank()).collect(Collectors.joining(", "));
        String resto = Stream.of(e.getComplemento(), identificacao, e.getBairro()).filter(s -> s != null && !s.isBlank()).collect(Collectors.joining(" · "));
        return resto.isEmpty() ? base : base + " · " + resto;
    }

    private List<NucleoFamiliar> familiasVigentes(Domicilio d) {
        return nucleoRepository.findByDomicilio_UuidInOrderByInicioDesc(List.of(d.getUuid())).stream().filter(NucleoFamiliar::vigente).toList();
    }

    private Imovel buscarImovel(UUID uuid) {
        return imovelRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um imóvel com o id " + uuid + " em nossos registros."));
    }

    private Domicilio buscarDomicilio(UUID uuid) {
        return domicilioRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um domicílio com o id " + uuid + " em nossos registros."));
    }

    private NucleoFamiliar buscarFamiliaVigente(UUID uuid) {
        NucleoFamiliar n = nucleoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma família com o id " + uuid + " em nossos registros."));
        if (!n.vigente()) {
            throw new ResourceUnprocessableEntityException("Esta família já não mora neste domicílio.");
        }
        return n;
    }

    private MembroNucleo buscarMembro(UUID uuid) {
        return membroRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a pessoa " + uuid + " em família nos nossos registros."));
    }

    private Paciente buscarPaciente(UUID uuid) {
        return pacienteRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um paciente com o id " + uuid + " em nossos registros."));
    }
}
