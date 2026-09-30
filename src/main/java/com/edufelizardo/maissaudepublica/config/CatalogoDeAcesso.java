package com.edufelizardo.maissaudepublica.config;

import com.edufelizardo.maissaudepublica.models.Papel;
import com.edufelizardo.maissaudepublica.models.Permissao;
import com.edufelizardo.maissaudepublica.models.enuns.DimensaoPermissao;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.PermissaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.edufelizardo.maissaudepublica.models.enuns.DimensaoPermissao.ACESSO_AO_DADO_DE_SAUDE;
import static com.edufelizardo.maissaudepublica.models.enuns.DimensaoPermissao.ADMINISTRACAO_DO_SISTEMA;
import static com.edufelizardo.maissaudepublica.models.enuns.DimensaoPermissao.OPERACAO;

/**
 * Catálogo de permissões e papéis padrão (ADR-0066), semeado na subida. Idempotente: cria o que
 * falta e não mexe no que já existe — um papel padrão ajustado pela administração não é sobrescrito.
 *
 * <p>Os papéis de enfermagem seguem a Lei 7.498/86 e a Resolução COFEN 661/2021: classificação de risco
 * (triagem) e evolução de enfermagem são privativas do enfermeiro. O administrador da plataforma não
 * recebe acesso a dado de saúde (ADR-0054).
 */
@Component
@Order(0)
@Slf4j
public class CatalogoDeAcesso implements ApplicationRunner {

    public static final String ADMINISTRADOR_PLATAFORMA = "ADMINISTRADOR_PLATAFORMA";

    private record Def(String descricao, DimensaoPermissao dimensao) {
    }

    /** Permissões no formato RECURSO.ACAO, em ordem de exibição. */
    static final Map<String, Def> PERMISSOES = new LinkedHashMap<>();

    static {
        PERMISSOES.put("USUARIO.GERENCIAR", new Def("Criar, bloquear e reativar usuários", ADMINISTRACAO_DO_SISTEMA));
        PERMISSOES.put("ACESSO.GERENCIAR", new Def("Criar papéis e conceder ou revogar acesso", ADMINISTRACAO_DO_SISTEMA));
        PERMISSOES.put("ORGANIZACAO.GERENCIAR", new Def("Cadastrar unidades de saúde e setores", ADMINISTRACAO_DO_SISTEMA));
        PERMISSOES.put("RH.CONSULTAR", new Def("Consultar profissionais e dados de RH", OPERACAO));
        PERMISSOES.put("RH.GERENCIAR", new Def("Registrar e alterar dados de RH", OPERACAO));
        PERMISSOES.put("ADMINISTRATIVO.CONSULTAR", new Def("Consultar o setor administrativo", OPERACAO));
        PERMISSOES.put("ADMINISTRATIVO.GERENCIAR", new Def("Registrar e alterar o setor administrativo", OPERACAO));
        PERMISSOES.put("PACIENTE.CONSULTAR", new Def("Consultar cadastro de pacientes", OPERACAO));
        PERMISSOES.put("PACIENTE.CADASTRAR", new Def("Cadastrar e atualizar pacientes", OPERACAO));
        PERMISSOES.put("AGENDAMENTO.GERENCIAR", new Def("Agendar, confirmar e cancelar", OPERACAO));
        PERMISSOES.put("ATENDIMENTO.GERENCIAR", new Def("Abrir (acolher), atualizar e concluir atendimentos", OPERACAO));
        PERMISSOES.put("FARMACIA.CONSULTAR", new Def("Consultar estoque, lotes e extratos", OPERACAO));
        PERMISSOES.put("FARMACIA.GERENCIAR_ESTOQUE", new Def("Medicamentos, entrada de lote, perda e ajuste", OPERACAO));
        PERMISSOES.put("FARMACIA.DISPENSAR", new Def("Dispensar medicamento ao paciente", OPERACAO));
        PERMISSOES.put("FARMACIA.TRANSFERIR", new Def("Enviar, receber e cancelar transferências", OPERACAO));
        PERMISSOES.put("PRONTUARIO.CONSULTAR", new Def("Ler o prontuário e os registros clínicos", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("TRIAGEM.REGISTRAR", new Def("Classificação de risco (privativa do enfermeiro)", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("EVOLUCAO.REGISTRAR", new Def("Evolução de enfermagem (privativa do enfermeiro)", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("CONSULTA.REGISTRAR", new Def("Consulta médica e prescrição", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("PROCEDIMENTO.REGISTRAR", new Def("Registrar procedimentos e seus desfechos", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("MEDICACAO.ADMINISTRAR", new Def("Checagem de medicação prescrita", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("REGISTRO_CLINICO.RETIFICAR_DE_OUTROS",
                new Def("Retificar registro clínico feito por outro profissional (supervisão)", ACESSO_AO_DADO_DE_SAUDE));
        // Fora de todos os papéis padrão (ADR-0071): quem administra o sistema não audita a si mesmo.
        PERMISSOES.put("AUDITORIA.CONSULTAR", new Def("Consultar a trilha de auditoria", ADMINISTRACAO_DO_SISTEMA));
    }

    private record PapelPadrao(String nome, String descricao, List<String> permissoes) {
    }

    static final Map<String, PapelPadrao> PAPEIS = new LinkedHashMap<>();

    static {
        PAPEIS.put(ADMINISTRADOR_PLATAFORMA, new PapelPadrao("Administrador da plataforma",
                "Usuários, acessos e estrutura. Não lê dado de saúde (ADR-0054).",
                List.of("USUARIO.GERENCIAR", "ACESSO.GERENCIAR", "ORGANIZACAO.GERENCIAR")));
        PAPEIS.put("GESTOR", new PapelPadrao("Gestor",
                "Gestão de RH, setor administrativo, estoque e acessos no seu escopo.",
                List.of("ACESSO.GERENCIAR", "RH.CONSULTAR", "RH.GERENCIAR", "ADMINISTRATIVO.CONSULTAR",
                        "ADMINISTRATIVO.GERENCIAR", "PACIENTE.CONSULTAR", "FARMACIA.CONSULTAR")));
        PAPEIS.put("RECEPCAO", new PapelPadrao("Recepção",
                "Cadastro de pacientes, agenda e abertura de atendimentos.",
                List.of("PACIENTE.CONSULTAR", "PACIENTE.CADASTRAR", "AGENDAMENTO.GERENCIAR", "ATENDIMENTO.GERENCIAR")));
        PAPEIS.put("MEDICO", new PapelPadrao("Médico",
                "Consulta, prescrição e procedimentos.",
                List.of("PACIENTE.CONSULTAR", "AGENDAMENTO.GERENCIAR", "ATENDIMENTO.GERENCIAR", "PRONTUARIO.CONSULTAR",
                        "CONSULTA.REGISTRAR", "PROCEDIMENTO.REGISTRAR")));
        PAPEIS.put("ENFERMEIRO", new PapelPadrao("Enfermeiro",
                "Classificação de risco, evolução, procedimentos e medicação.",
                List.of("PACIENTE.CONSULTAR", "ATENDIMENTO.GERENCIAR", "PRONTUARIO.CONSULTAR", "TRIAGEM.REGISTRAR",
                        "EVOLUCAO.REGISTRAR", "PROCEDIMENTO.REGISTRAR", "MEDICACAO.ADMINISTRAR")));
        PAPEIS.put("COORDENADOR_DE_ENFERMAGEM", new PapelPadrao("Coordenador de enfermagem",
                "O que o enfermeiro faz, mais retificar registro clínico de outro profissional.",
                List.of("PACIENTE.CONSULTAR", "ATENDIMENTO.GERENCIAR", "PRONTUARIO.CONSULTAR", "TRIAGEM.REGISTRAR",
                        "EVOLUCAO.REGISTRAR", "PROCEDIMENTO.REGISTRAR", "MEDICACAO.ADMINISTRAR",
                        "REGISTRO_CLINICO.RETIFICAR_DE_OUTROS")));
        PAPEIS.put("TECNICO_DE_ENFERMAGEM", new PapelPadrao("Técnico de enfermagem",
                "Procedimentos e medicação. Sem classificação de risco nem evolução (COFEN 661/2021).",
                List.of("PACIENTE.CONSULTAR", "PRONTUARIO.CONSULTAR", "PROCEDIMENTO.REGISTRAR", "MEDICACAO.ADMINISTRAR")));
        PAPEIS.put("FARMACEUTICO", new PapelPadrao("Farmacêutico",
                "Estoque, dispensação e transferências; lê a prescrição.",
                List.of("PACIENTE.CONSULTAR", "PRONTUARIO.CONSULTAR", "FARMACIA.CONSULTAR", "FARMACIA.GERENCIAR_ESTOQUE",
                        "FARMACIA.DISPENSAR", "FARMACIA.TRANSFERIR")));
    }

    /** Papel semeado por este catálogo (os demais foram criados pela administração). */
    public static boolean ehPapelPadrao(String codigo) {
        return PAPEIS.containsKey(codigo);
    }

    @Autowired
    private PermissaoRepository permissaoRepository;

    @Autowired
    private PapelRepository papelRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int novasPermissoes = 0;
        for (Map.Entry<String, Def> e : PERMISSOES.entrySet()) {
            if (permissaoRepository.findByCodigo(e.getKey()).isEmpty()) {
                permissaoRepository.save(new Permissao(e.getKey(), e.getValue().descricao(), e.getValue().dimensao()));
                novasPermissoes++;
            }
        }
        int novosPapeis = 0;
        for (Map.Entry<String, PapelPadrao> e : PAPEIS.entrySet()) {
            if (papelRepository.findByCodigo(e.getKey()).isEmpty()) {
                Set<Permissao> permissoes = new HashSet<>(permissaoRepository.findByCodigoIn(e.getValue().permissoes()));
                papelRepository.save(new Papel(e.getKey(), e.getValue().nome(), e.getValue().descricao(), permissoes));
                novosPapeis++;
            }
        }
        if (novasPermissoes + novosPapeis > 0) {
            log.info("Catálogo de acesso: {} permissão(ões) e {} papel(éis) padrão criados (ADR-0066).", novasPermissoes, novosPapeis);
        }
    }
}
