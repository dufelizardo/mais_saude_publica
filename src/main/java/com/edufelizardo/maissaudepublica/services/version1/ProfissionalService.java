package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProfissionalDesligamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProfissionalContatoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProfissionalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProfissionalResponseDto;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * CRUD do Profissional (RH), com reconciliação síncrona do vínculo fraco por CPF com
 * UnidadeDeSaude — ver ADR-0014. Não estende {@link AbstractHierarquicoService}, que é
 * específico do domínio de UnidadeDeSaude.
 */
@Service
public class ProfissionalService {

    private static final int MAX_TENTATIVAS_GERAR_MATRICULA = 5;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    public List<ProfissionalResponseDto> getAll() {
        return profissionalRepository.findAll()
                .stream()
                .map(ProfissionalResponseDto::fromProfissional)
                .collect(Collectors.toList());
    }

    public ProfissionalResponseDto findByCpf(String cpf) {
        return ProfissionalResponseDto.fromProfissional(buscarProfissionalPorCpf(cpf));
    }

    /**
     * Busca por matrícula (chave única de verdade do domínio, ver ADR-0017) — usada por domínios
     * fora do RH que referenciam Profissional só por matrícula (ver ADR-0034), como o Setor
     * Administrativo, e precisam resolver o nome antes de confirmar uma ação.
     */
    public ProfissionalResponseDto findByMatricula(String matricula) {
        Profissional profissional = profissionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
        return ProfissionalResponseDto.fromProfissional(profissional);
    }

    @Transactional
    public ProfissionalResponseDto create(ProfissionalRequestDto dto) {
        Profissional profissional = new Profissional(dto);
        profissional.setMatricula(gerarMatriculaUnica());
        profissional = profissionalRepository.save(profissional);
        reconciliarUnidadesPendentes(profissional);
        return ProfissionalResponseDto.fromProfissional(profissional);
    }

    @Transactional
    public ProfissionalResponseDto updateContato(String cpf, ProfissionalContatoRequestDto dto) {
        Profissional profissional = buscarProfissionalPorCpf(cpf);
        profissional.setEndereco(new Endereco(dto.getEndereco()));
        profissional.setTelefones(dto.getTelefones());
        profissional.setEmail(dto.getEmail());
        profissional = profissionalRepository.save(profissional);
        return ProfissionalResponseDto.fromProfissional(profissional);
    }

    /**
     * Desliga ou reabilita uma ficha com este CPF, derivando o status da presença de
     * dataDesligamento (ver ADR-0017): informada → ativo=false, grava a data; ausente →
     * ativo=true, limpa a data. Não existe mais um parâmetro "ativo" separado — a data sozinha
     * já expressa a intenção. Reabilitar limpa dataDesligamento — decisão do usuário: uma
     * recontratação cria uma ficha nova (com matrícula nova), então "reabilitar" aqui é só o
     * caso raro de desligamento revertido na mesma ficha, sem sentido carregar uma data de saída
     * antiga.
     */
    @Transactional
    public ProfissionalResponseDto desabilitar(String cpf, ProfissionalDesligamentoRequestDto dto) {
        Profissional profissional = buscarProfissionalParaAlterarStatus(cpf);
        boolean ativo = dto.getDataDesligamento() == null;
        profissional.setAtivo(ativo);
        profissional.setDataDesligamento(dto.getDataDesligamento());
        profissional = profissionalRepository.save(profissional);
        return ProfissionalResponseDto.fromProfissional(profissional);
    }

    /**
     * Vincula este Profissional recém-criado a toda UnidadeDeSaude que já tinha o CPF dele
     * informado como {@code responsavelCpf}, mas ainda sem {@code responsavel} resolvido —
     * metade "profissional chega depois" da reconciliação (ver ADR-0014).
     */
    private void reconciliarUnidadesPendentes(Profissional profissional) {
        List<UnidadeDeSaude> unidadesPendentes =
                unidadeDeSaudeRepository.findByResponsavelCpfAndResponsavelIsNull(profissional.getCpf());
        unidadesPendentes.forEach(unidade -> unidade.setResponsavel(profissional));
        unidadeDeSaudeRepository.saveAll(unidadesPendentes);
    }

    private Profissional buscarProfissionalPorCpf(String cpf) {
        return profissionalRepository.findByCpfAndAtivoTrue(cpf)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com o CPF " + cpf + " em nossos registros."));
    }

    /**
     * Acha a ficha certa pra alternar o status: a ATIVA, se existir (caso comum — desligar quem
     * está trabalhando); senão, a mais recentemente desligada (caso raro — reverter um
     * desligamento feito por engano). Não usa {@link #buscarProfissionalPorCpf}, que só enxerga
     * fichas ativas e nunca acharia nada pra reabilitar (ver ADR-0017).
     */
    private Profissional buscarProfissionalParaAlterarStatus(String cpf) {
        List<Profissional> fichas = profissionalRepository.findByCpf(cpf);

        return fichas.stream()
                .filter(Profissional::isAtivo)
                .findFirst()
                .or(() -> fichas.stream()
                        .max(Comparator.comparing(
                                Profissional::getDataDesligamento,
                                Comparator.nullsLast(Comparator.naturalOrder()))))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com o CPF " + cpf + " em nossos registros."));
    }

    /**
     * Gera uma matrícula de 14 dígitos + hífen + 2 dígitos verificadores (soma dos 14 primeiros,
     * módulo 100), verificando unicidade antes de devolver (ver ADR-0017). Checagem prévia em vez
     * de tentar salvar e reagir a uma violação de unicidade — evita marcar a transação como
     * rollback-only por causa de uma colisão que na prática é praticamente impossível (10^14
     * combinações).
     */
    private String gerarMatriculaUnica() {
        for (int tentativa = 0; tentativa < MAX_TENTATIVAS_GERAR_MATRICULA; tentativa++) {
            String candidata = gerarMatricula();
            if (!profissionalRepository.existsByMatricula(candidata)) {
                return candidata;
            }
        }
        throw new IllegalStateException(
                "Não foi possível gerar uma matrícula única após " + MAX_TENTATIVAS_GERAR_MATRICULA + " tentativas.");
    }

    private static String gerarMatricula() {
        long base = ThreadLocalRandom.current().nextLong(0, 100_000_000_000_000L);
        String baseFormatada = String.format("%014d", base);
        int somaDigitos = baseFormatada.chars().map(Character::getNumericValue).sum();
        String digitoVerificador = String.format("%02d", somaDigitos % 100);
        return baseFormatada + "-" + digitoVerificador;
    }
}
