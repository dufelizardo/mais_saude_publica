package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Documento;
import com.edufelizardo.maissaudepublica.models.TipoDocumento;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.VersaoDocumento;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TipoDocumentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.DocumentosDoPacienteDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TipoDocumentoDto;
import com.edufelizardo.maissaudepublica.models.enuns.DonoDocumento;
import com.edufelizardo.maissaudepublica.repositories.DocumentoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.TipoDocumentoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.VersaoDocumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Documentos, fatia D1 (ADR-0172): anexos do paciente com versões, num armazenamento fora do banco. Abrir segue o prontuário, com o vínculo
 * assistencial e o registro de quem leu. Quem só anexa, como a recepção, vê o que anexou e não abre o arquivo.
 */
@Service
public class DocumentoService {

    public static final String ANEXAR = "DOCUMENTO.ANEXAR";
    public static final String LER = "PRONTUARIO.CONSULTAR";
    private static final long TAMANHO_MAXIMO = 10L * 1024 * 1024;
    private static final byte[] PDF = {'%', 'P', 'D', 'F', '-'};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    /** O arquivo que será guardado. */
    public record ArquivoDoDocumento(VersaoDocumento versao, byte[] conteudo) {
    }

    @Autowired private DocumentoRepository documentoRepository;
    @Autowired private VersaoDocumentoRepository versaoRepository;
    @Autowired private TipoDocumentoRepository tipoRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ArmazenamentoDeArquivos armazenamento;
    @Autowired private ControleDeAcesso controleDeAcesso;
    @Autowired private VinculoAssistencialService vinculoAssistencialService;

    // ── Catálogo ───────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TipoDocumentoDto> tipos() {
        return tipoRepository.findAllByOrderByNomeAsc().stream().map(DocumentoService::dto).toList();
    }

    @Transactional
    public TipoDocumentoDto criarTipo(TipoDocumentoRequestDto dto) {
        String codigo = dto.getCodigo().trim().toUpperCase();
        if (tipoRepository.findByCodigo(codigo).isPresent()) {
            throw new ResourceConflictException("Já existe o tipo de documento " + codigo + ".");
        }
        TipoDocumento t = tipoRepository.save(new TipoDocumento(null, codigo, dto.getNome().trim(), dto.getAtivo() == null || dto.getAtivo()));
        ContextoAuditoria.registro(t.getUuid());
        return dto(t);
    }

    @Transactional
    public TipoDocumentoDto atualizarTipo(UUID uuid, TipoDocumentoRequestDto dto) {
        TipoDocumento t = tipoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o tipo de documento " + uuid + " em nossos registros."));
        String codigo = dto.getCodigo().trim().toUpperCase();
        tipoRepository.findByCodigo(codigo).filter(o -> !o.getUuid().equals(uuid)).ifPresent(o -> {
            throw new ResourceConflictException("Já existe o tipo de documento " + codigo + ".");
        });
        t.setCodigo(codigo);
        t.setNome(dto.getNome().trim());
        t.setAtivo(dto.getAtivo() == null || dto.getAtivo());
        ContextoAuditoria.registro(t.getUuid());
        return dto(tipoRepository.save(t));
    }

    // ── Documentos do paciente ─────────────────────────────────────────────────────────────────────

    /** Quem lê o prontuário vê tudo, com o vínculo assistencial; quem só anexa vê o que anexou. */
    @Transactional
    public DocumentosDoPacienteDto doPaciente(UUID pacienteId) {
        exigirPaciente(pacienteId);
        ContextoAuditoria.paciente(pacienteId);
        boolean leProntuario = controleDeAcesso.temAlguma(LER);
        if (leProntuario) {
            vinculoAssistencialService.exigirVinculo(pacienteId);
        }
        String cpf = UsuarioAutenticado.cpf();
        List<Documento> visiveis = documentoRepository.findDoDono(DonoDocumento.PACIENTE, pacienteId).stream()
                .filter(d -> leProntuario || (cpf != null && cpf.equals(d.getRegistradoPorCpf()))).toList();
        return new DocumentosDoPacienteDto(leProntuario, visiveis.stream().filter(d -> !d.retirado()).map(this::item).toList(),
                visiveis.stream().filter(Documento::retirado).map(this::item).toList());
    }

    /** Anexa com a primeira versão: paciente (404), unidade do acesso (403), tipo ativo (422), título (400), data não futura (400), arquivo válido (400). */
    @Transactional
    public DocumentosDoPacienteDto anexar(UUID pacienteId, UUID tipoId, UUID unidadeId, String titulo, LocalDate dataDocumento, MultipartFile arquivo) {
        exigirPaciente(pacienteId);
        UnidadeDeSaude unidade = unidadeRepository.findById(unidadeId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + unidadeId + " em nossos registros."));
        controleDeAcesso.exigir(ANEXAR, unidade);
        ContextoAuditoria.paciente(pacienteId);
        ContextoAuditoria.unidade(unidade);
        TipoDocumento tipo = tipoRepository.findById(tipoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o tipo de documento " + tipoId + " em nossos registros."));
        if (!tipo.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O tipo " + tipo.getNome() + " está inativo.");
        }
        String tituloLimpo = titulo == null ? "" : titulo.trim();
        if (tituloLimpo.isEmpty() || tituloLimpo.length() > 150) {
            throw new ResourceBadRequestException("Informe o título, com até 150 caracteres.");
        }
        LocalDate data = dataDocumento != null ? dataDocumento : LocalDate.now();
        if (data.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A data do documento não pode ser no futuro.");
        }
        Documento d = new Documento();
        d.setDonoTipo(DonoDocumento.PACIENTE);
        d.setDonoId(pacienteId);
        d.setTipo(tipo);
        d.setUnidade(unidade);
        d.setTitulo(tituloLimpo);
        d.setDataDocumento(data);
        d.setRegistradoEm(LocalDateTime.now());
        d.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        documentoRepository.save(d);
        guardar(d, 1, arquivo);
        ContextoAuditoria.registro(d.getUuid());
        ContextoAuditoria.detalhe("Documento anexado: " + tipo.getNome() + ".");
        return doPaciente(pacienteId);
    }

    /** Envia a versão seguinte; documento retirado não recebe versão (422). */
    @Transactional
    public DocumentosDoPacienteDto novaVersao(UUID documentoId, MultipartFile arquivo) {
        Documento d = doDono(documentoId, Set.of(DonoDocumento.PACIENTE));
        controleDeAcesso.exigir(ANEXAR, d.getUnidade());
        if (d.retirado()) {
            throw new ResourceUnprocessableEntityException("Este documento foi retirado.");
        }
        int ultima = versaoRepository.findByDocumento_UuidOrderByNumeroDesc(documentoId).stream().mapToInt(VersaoDocumento::getNumero).max().orElse(0);
        guardar(d, ultima + 1, arquivo);
        ContextoAuditoria.paciente(d.getDonoId());
        ContextoAuditoria.registro(d.getUuid());
        return doPaciente(d.getDonoId());
    }

    /** Retira da lista, com o motivo; nada se apaga. */
    @Transactional
    public DocumentosDoPacienteDto retirar(UUID documentoId, MotivoRequestDto dto) {
        Documento d = doDono(documentoId, Set.of(DonoDocumento.PACIENTE));
        controleDeAcesso.exigir(ANEXAR, d.getUnidade());
        if (d.retirado()) {
            throw new ResourceUnprocessableEntityException("Este documento já foi retirado.");
        }
        d.setRetiradoEm(LocalDateTime.now());
        d.setMotivoRetirada(dto.getMotivo().trim());
        d.setRetiradoPorCpf(UsuarioAutenticado.cpf());
        documentoRepository.save(d);
        ContextoAuditoria.paciente(d.getDonoId());
        ContextoAuditoria.registro(d.getUuid());
        ContextoAuditoria.detalhe("Documento retirado.");
        return doPaciente(d.getDonoId());
    }

    /** O arquivo da versão, com o vínculo do prontuário. Retirado não abre (422); hash diferente é arquivo alterado (409). */
    @Transactional
    public ArquivoDoDocumento abrir(UUID versaoId) {
        VersaoDocumento v = versaoRepository.findById(versaoId).filter(x -> x.getDocumento().getDonoTipo() == DonoDocumento.PACIENTE)
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a versão " + versaoId + " em nossos registros."));
        Documento d = v.getDocumento();
        ContextoAuditoria.paciente(d.getDonoId());
        ContextoAuditoria.registro(d.getUuid());
        vinculoAssistencialService.exigirVinculo(d.getDonoId());
        if (d.retirado()) {
            throw new ResourceUnprocessableEntityException("Este documento foi retirado.");
        }
        byte[] conteudo = armazenamento.ler(v.getChave());
        if (!sha256(conteudo).equals(v.getSha256())) {
            throw new ResourceConflictException("O arquivo não confere com o registro: foi alterado fora do sistema.");
        }
        return new ArquivoDoDocumento(v, conteudo);
    }

    // ── Outros donos (D2 parcial, ADR-0199) ────────────────────────────────────────────────────────

    /**
     * Anexa ao dono de outro domínio, sem unidade. Quem chama confere o acesso; aqui ficam as mesmas regras do arquivo: tipo
     * ativo (422), título (400), data não futura (400), PDF, JPG ou PNG de até 10 MB (400).
     */
    @Transactional
    public Documento anexarAoDono(DonoDocumento dono, UUID donoId, UUID tipoId, String titulo, LocalDate dataDocumento, MultipartFile arquivo) {
        if (dono == DonoDocumento.PACIENTE) {
            throw new IllegalArgumentException("O documento do paciente passa por anexar, com a unidade.");
        }
        TipoDocumento tipo = tipoRepository.findById(tipoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o tipo de documento " + tipoId + " em nossos registros."));
        if (!tipo.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O tipo " + tipo.getNome() + " está inativo.");
        }
        String tituloLimpo = titulo == null ? "" : titulo.trim();
        if (tituloLimpo.isEmpty() || tituloLimpo.length() > 150) {
            throw new ResourceBadRequestException("Informe o título, com até 150 caracteres.");
        }
        LocalDate data = dataDocumento != null ? dataDocumento : LocalDate.now();
        if (data.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A data do documento não pode ser no futuro.");
        }
        Documento d = new Documento();
        d.setDonoTipo(dono);
        d.setDonoId(donoId);
        d.setTipo(tipo);
        d.setTitulo(tituloLimpo);
        d.setDataDocumento(data);
        d.setRegistradoEm(LocalDateTime.now());
        d.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        documentoRepository.save(d);
        guardar(d, 1, arquivo);
        ContextoAuditoria.registro(d.getUuid());
        ContextoAuditoria.detalhe("Documento anexado: " + tipo.getNome() + ".");
        return d;
    }

    /** Guarda o PDF gerado pelo sistema (um termo, por exemplo) como documento do dono, com o tipo do catálogo pelo código. */
    @Transactional
    public VersaoDocumento guardarGerado(DonoDocumento dono, UUID donoId, String tipoCodigo, String titulo, byte[] conteudo, String nomeArquivo) {
        TipoDocumento tipo = tipoRepository.findByCodigo(tipoCodigo).orElseThrow(() -> new IllegalStateException("Tipo de documento " + tipoCodigo + " fora da carga."));
        Documento d = new Documento();
        d.setDonoTipo(dono);
        d.setDonoId(donoId);
        d.setTipo(tipo);
        d.setTitulo(titulo.length() > 150 ? titulo.substring(0, 150) : titulo);
        d.setDataDocumento(LocalDate.now());
        d.setRegistradoEm(LocalDateTime.now());
        d.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        documentoRepository.save(d);
        return guardar(d, 1, conteudo, nomeArquivo);
    }

    /** Se a versão mais recente do documento confere com o hash registrado (o arquivo não foi alterado fora do sistema). */
    @Transactional(readOnly = true)
    public boolean integro(UUID documentoId, String hashEsperado) {
        List<VersaoDocumento> versoes = versaoRepository.findByDocumento_UuidOrderByNumeroDesc(documentoId);
        if (versoes.isEmpty() || !versoes.get(0).getSha256().equals(hashEsperado)) {
            return false;
        }
        try {
            return sha256(armazenamento.ler(versoes.get(0).getChave())).equals(hashEsperado);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Os documentos ativos do dono, mais recentes primeiro. Quem chama confere o acesso. */
    @Transactional(readOnly = true)
    public List<DocumentosDoPacienteDto.Item> doDono(DonoDocumento dono, UUID donoId) {
        return documentoRepository.findDoDono(dono, donoId).stream().filter(d -> !d.retirado()).map(this::item).toList();
    }

    /** O documento, só se for de um dos donos aceitos (404 nos outros, para não revelar o documento do paciente). */
    @Transactional(readOnly = true)
    public Documento doDono(UUID documentoId, Set<DonoDocumento> donos) {
        Documento d = documento(documentoId);
        if (!donos.contains(d.getDonoTipo())) {
            throw new ResourceNotFoundException("Não foi possível encontrar o documento " + documentoId + " em nossos registros.");
        }
        return d;
    }

    /** Retira o documento de um dos donos aceitos, com o motivo (422 se já retirado). */
    @Transactional
    public void retirarDoDono(UUID documentoId, Set<DonoDocumento> donos, String motivo) {
        Documento d = doDono(documentoId, donos);
        if (d.retirado()) {
            throw new ResourceUnprocessableEntityException("Este documento já foi retirado.");
        }
        d.setRetiradoEm(LocalDateTime.now());
        d.setMotivoRetirada(motivo.trim());
        d.setRetiradoPorCpf(UsuarioAutenticado.cpf());
        documentoRepository.save(d);
        ContextoAuditoria.registro(d.getUuid());
        ContextoAuditoria.detalhe("Documento retirado.");
    }

    /** O arquivo da versão de um dos donos aceitos (404 nos outros). Retirado não abre (422); hash diferente, 409. */
    @Transactional(readOnly = true)
    public ArquivoDoDocumento abrirDoDono(UUID versaoId, Set<DonoDocumento> donos) {
        VersaoDocumento v = versaoRepository.findById(versaoId).filter(x -> donos.contains(x.getDocumento().getDonoTipo()))
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a versão " + versaoId + " em nossos registros."));
        Documento d = v.getDocumento();
        ContextoAuditoria.registro(d.getUuid());
        if (d.retirado()) {
            throw new ResourceUnprocessableEntityException("Este documento foi retirado.");
        }
        byte[] conteudo = armazenamento.ler(v.getChave());
        if (!sha256(conteudo).equals(v.getSha256())) {
            throw new ResourceConflictException("O arquivo não confere com o registro: foi alterado fora do sistema.");
        }
        return new ArquivoDoDocumento(v, conteudo);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private void guardar(Documento d, int numero, MultipartFile arquivo) {
        guardar(d, numero, conteudo(arquivo), nomeDoArquivo(arquivo.getOriginalFilename()));
    }

    private VersaoDocumento guardar(Documento d, int numero, byte[] conteudo, String nome) {
        String tipoConteudo = tipoPeloConteudo(conteudo);
        String chave = UUID.randomUUID().toString();
        armazenamento.gravar(chave, conteudo);
        VersaoDocumento v = new VersaoDocumento(null, d, numero, nome, tipoConteudo, conteudo.length, sha256(conteudo), chave, LocalDateTime.now(), UsuarioAutenticado.cpf());
        return versaoRepository.save(v);
    }

    private static byte[] conteudo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResourceBadRequestException("Envie o arquivo.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new ResourceBadRequestException("O arquivo vai até 10 MB.");
        }
        try {
            return arquivo.getBytes();
        } catch (IOException e) {
            throw new ResourceBadRequestException("Não foi possível ler o arquivo enviado.");
        }
    }

    /** O tipo vem do conteúdo, não da extensão nem do que o navegador declarou. */
    private static String tipoPeloConteudo(byte[] c) {
        if (comeca(c, PDF)) {
            return "application/pdf";
        }
        if (comeca(c, PNG)) {
            return "image/png";
        }
        if (comeca(c, JPG)) {
            return "image/jpeg";
        }
        throw new ResourceBadRequestException("Envie um arquivo PDF, JPG ou PNG.");
    }

    private static boolean comeca(byte[] conteudo, byte[] assinatura) {
        return conteudo.length >= assinatura.length && Arrays.equals(Arrays.copyOf(conteudo, assinatura.length), assinatura);
    }

    /** Só o nome, sem caminho nem caractere de controle. */
    private static String nomeDoArquivo(String original) {
        String nome = original == null ? "" : original.replace('\\', '/');
        nome = nome.substring(nome.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").trim();
        if (nome.isEmpty()) {
            return "documento";
        }
        return nome.length() > 255 ? nome.substring(nome.length() - 255) : nome;
    }

    private static String sha256(byte[] conteudo) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(conteudo));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private DocumentosDoPacienteDto.Item item(Documento d) {
        UnidadeDeSaude u = d.getUnidade();
        return new DocumentosDoPacienteDto.Item(d.getUuid(), d.getTipo().getNome(), d.getTitulo(), d.getDataDocumento(), u != null ? u.getUuid() : null, u != null ? u.getNome() : null,
                d.getRetiradoEm(), d.getMotivoRetirada(), versaoRepository.findByDocumento_UuidOrderByNumeroDesc(d.getUuid()).stream()
                .map(v -> new DocumentosDoPacienteDto.Versao(v.getUuid(), v.getNumero(), v.getNomeArquivo(), v.getTipoConteudo(), v.getTamanho(), v.getEnviadoEm())).toList());
    }

    private static TipoDocumentoDto dto(TipoDocumento t) {
        return new TipoDocumentoDto(t.getUuid(), t.getCodigo(), t.getNome(), t.isAtivo());
    }

    private void exigirPaciente(UUID pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Não foi possível encontrar um paciente com o id " + pacienteId + " em nossos registros.");
        }
    }

    private Documento documento(UUID id) {
        return documentoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o documento " + id + " em nossos registros."));
    }
}
