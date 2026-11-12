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
import java.util.Optional;
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
        // Catálogo de programas de saúde (ADR-0118); o acompanhamento de pacientes (PROGRAMA.ACOMPANHAR) vem na P2.
        // Indicadores e BI (ADRs 0142 e 0143): só agregados, na unidade do escopo ou na rede.
        PERMISSOES.put("INDICADOR.CONSULTAR", new Def("Consultar indicadores de produção e gestão (só agregados, sem paciente)", OPERACAO));
        // Imunização (ADR-0147): registrar a dose é da enfermagem (técnico e enfermeiro); o catálogo e o calendário, da gestão.
        PERMISSOES.put("VACINACAO.REGISTRAR", new Def("Registrar, retificar e anular doses de vacina e ver a sala de vacina", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("IMUNIZACAO.GERENCIAR", new Def("Manter o catálogo de imunobiológicos e o calendário vacinal", OPERACAO));
        PERMISSOES.put("PROGRAMA.GERENCIAR", new Def("Cadastrar programas de saúde, categorias e as unidades que os oferecem", OPERACAO));
        // Acompanhamento programático (ADR-0119): estar num programa revela diagnóstico.
        PERMISSOES.put("PROGRAMA.ACOMPANHAR", new Def("Inscrever pacientes em programas de saúde e acompanhar a situação", ACESSO_AO_DADO_DE_SAUDE));
        // Saúde Mental (ADR-0120): o conteúdo é sigiloso no grau mais alto; vínculo e equipe de referência se somam a elas.
        PERMISSOES.put("SAUDE_MENTAL.ACOLHER", new Def("Registrar acolhimento em saúde mental", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("SAUDE_MENTAL.ACOMPANHAR", new Def("Abrir e conduzir o caso em saúde mental: referência, avaliação e saída",
                ACESSO_AO_DADO_DE_SAUDE));
        // Crise em saúde mental (ADR-0123): registrar e conduzir o evento de crise, com ou sem caso.
        PERMISSOES.put("SAUDE_MENTAL.CRISE", new Def("Registrar e conduzir o evento de crise em saúde mental", ACESSO_AO_DADO_DE_SAUDE));
        // Vigilância epidemiológica (ADR-0124): notificar é de qualquer profissional assistencial; investigar e o catálogo, da vigilância.
        PERMISSOES.put("VIGILANCIA.NOTIFICAR", new Def("Notificar agravos e declarar a notificação negativa da semana", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("VIGILANCIA.INVESTIGAR", new Def("Investigar, classificar e encerrar notificações, inclusive de agravo sensível",
                ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("VIGILANCIA.GERENCIAR", new Def("Manter o catálogo de agravos e as fichas específicas", OPERACAO));
        // Controle de vetores (ADR-0162): a vistoria é do agente de endemias; as ações e os bloqueios, da coordenação de zoonoses.
        PERMISSOES.put("ZOONOSES.VISTORIAR", new Def("Registrar vistorias de controle vetorial, focos e o tratamento", OPERACAO));
        PERMISSOES.put("ZOONOSES.GERENCIAR", new Def("Planejar e registrar ações de controle vetorial e ver os casos sem bloqueio", OPERACAO));
        // Vigilância Sanitária (ADR-0199): o fiscal inspeciona; a coordenação licencia, recebe denúncias e mantém os roteiros.
        PERMISSOES.put("VISA.INSPECIONAR", new Def("Cadastrar estabelecimentos regulados e registrar inspeções sanitárias com as irregularidades", OPERACAO));
        PERMISSOES.put("VISA.GERENCIAR", new Def("Conceder licenças sanitárias, registrar e concluir denúncias e manter os roteiros de inspeção", OPERACAO));
        // Intersetorialidade (ADR-0154): o catálogo de instituições externas é cadastro, sem paciente.
        PERMISSOES.put("INTERSETORIAL.GERENCIAR", new Def("Manter o catálogo de instituições externas (CRAS, CREAS, escolas, Conselho Tutelar...)",
                OPERACAO));
        // Emergências e Desastres (ADR-0156): registrar o evento e o impacto; ativar é da autoridade sanitária ou do gestor.
        PERMISSOES.put("EMERGENCIA.REGISTRAR", new Def("Registrar evento emergencial, território e avaliação de impacto", OPERACAO));
        PERMISSOES.put("EMERGENCIA.ATIVAR", new Def("Converter o evento em emergência, mudar o nível, registrar ESPIN e decreto e encerrar",
                OPERACAO));
        // A coordenação da resposta (ADR-0158): ações, mobilização e capacidade emergencial ativada.
        PERMISSOES.put("EMERGENCIA.COORDENAR", new Def("Coordenar a resposta: ações emergenciais, mobilização e capacidade emergencial", OPERACAO));
        // Comunicação (ADR-0168): quem publica não é quem revisa a orientação técnica.
        PERMISSOES.put("COMUNICACAO.PUBLICAR", new Def("Criar e publicar comunicados e publicar conteúdos aprovados", OPERACAO));
        PERMISSOES.put("CONTEUDO.EDITAR", new Def("Escrever conteúdos educativos e enviá-los à revisão técnica", OPERACAO));
        PERMISSOES.put("CONTEUDO.REVISAR", new Def("Fazer a revisão técnica dos conteúdos educativos", OPERACAO));
        PERMISSOES.put("CONTINGENCIA.GERENCIAR", new Def("Manter os tipos e níveis de emergência e, depois, os planos de contingência", OPERACAO));
        // Saúde Bucal (ADR-0134): a avaliação e o diagnóstico são do cirurgião-dentista (Lei 5.081/1966).
        PERMISSOES.put("ODONTOLOGIA.REGISTRAR", new Def("Registrar a avaliação odontológica (cirurgião-dentista)", ACESSO_AO_DADO_DE_SAUDE));
        // Plano de tratamento odontológico (ADR-0137): abrir, conduzir e concluir é do cirurgião-dentista.
        PERMISSOES.put("ODONTOLOGIA.PLANEJAR", new Def("Abrir, conduzir e concluir o plano de tratamento odontológico (cirurgião-dentista)",
                ACESSO_AO_DADO_DE_SAUDE));
        // Estoque e almoxarifado (ADR-0177): materiais gerais; medicamento é da Farmácia e vacina, da Sala de Vacina.
        PERMISSOES.put("ESTOQUE.CONSULTAR", new Def("Consultar o saldo, o livro e os alertas do estoque de materiais", OPERACAO));
        PERMISSOES.put("ESTOQUE.MOVIMENTAR", new Def("Registrar entrada, saída por consumo, perda e ajuste de materiais", OPERACAO));
        PERMISSOES.put("ESTOQUE.REQUISITAR", new Def("Pedir materiais ao almoxarifado central para a unidade", OPERACAO));
        PERMISSOES.put("ESTOQUE.ATENDER", new Def("Atender e negar as requisições na central de abastecimento", OPERACAO));
        PERMISSOES.put("ESTOQUE.GERENCIAR", new Def("Manter o catálogo de materiais e o estoque mínimo", OPERACAO));
        // Transporte (ADR-0178): a frota, a situação dos veículos e o plantão das viaturas.
        PERMISSOES.put("TRANSPORTE.CONSULTAR", new Def("Consultar a frota, a situação dos veículos e os plantões", OPERACAO));
        PERMISSOES.put("TRANSPORTE.GERENCIAR", new Def("Cadastrar veículos, registrar manutenção e montar os plantões das viaturas", OPERACAO));
        // Transporte eletivo e TFD (ADR-0190): a unidade pede; a comissão de TFD autoriza (lê o laudo); a frota programa as viagens.
        PERMISSOES.put("TRANSPORTE.SOLICITAR", new Def("Pedir transporte eletivo para o paciente (consulta, exame, hemodiálise, alta, TFD)", OPERACAO));
        PERMISSOES.put("TFD.AUTORIZAR", new Def("Autorizar e negar o tratamento fora do domicílio (comissão de TFD)", ACESSO_AO_DADO_DE_SAUDE));
        // Gestão da Rede (ADR-0179): oferta e cotas da PPI; a Regulação consome a cota na autorização.
        PERMISSOES.put("REDE.CONSULTAR", new Def("Consultar a oferta assistencial e as cotas da PPI", OPERACAO));
        PERMISSOES.put("REDE.GERENCIAR", new Def("Cadastrar a oferta da executante e pactuar as cotas da PPI", OPERACAO));
        PERMISSOES.put("PACIENTE.CONSULTAR", new Def("Consultar cadastro de pacientes", OPERACAO));
        PERMISSOES.put("PACIENTE.CADASTRAR", new Def("Cadastrar e atualizar pacientes", OPERACAO));
        PERMISSOES.put("AGENDAMENTO.GERENCIAR", new Def("Agendar, confirmar e cancelar", OPERACAO));
        PERMISSOES.put("ATENDIMENTO.GERENCIAR", new Def("Abrir (acolher), atualizar e concluir atendimentos", OPERACAO));
        PERMISSOES.put("FARMACIA.CONSULTAR", new Def("Consultar estoque, lotes e extratos", OPERACAO));
        PERMISSOES.put("FARMACIA.GERENCIAR_ESTOQUE", new Def("Medicamentos, entrada de lote, perda e ajuste", OPERACAO));
        PERMISSOES.put("FARMACIA.DISPENSAR", new Def("Dispensar medicamento ao paciente", OPERACAO));
        PERMISSOES.put("FARMACIA.TRANSFERIR", new Def("Enviar, receber e cancelar transferências", OPERACAO));
        PERMISSOES.put("PRONTUARIO.CONSULTAR", new Def("Ler o prontuário e os registros clínicos", ACESSO_AO_DADO_DE_SAUDE));
        // Fora de todos os papéis padrão (ADR-0076): auditoria clínica e regulação, com toda leitura auditada.
        PERMISSOES.put("PRONTUARIO.CONSULTAR_SEM_VINCULO",
                new Def("Ler o prontuário de qualquer paciente, sem vínculo assistencial", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("TRIAGEM.REGISTRAR", new Def("Classificação de risco (privativa do enfermeiro)", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("EVOLUCAO.REGISTRAR", new Def("Evolução de enfermagem (privativa do enfermeiro)", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("CONSULTA.REGISTRAR", new Def("Consulta médica e prescrição", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("PROCEDIMENTO.REGISTRAR", new Def("Registrar procedimentos e seus desfechos", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("MEDICACAO.ADMINISTRAR", new Def("Checagem de medicação prescrita", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("REGISTRO_CLINICO.RETIFICAR_DE_OUTROS",
                new Def("Retificar registro clínico feito por outro profissional (supervisão)", ACESSO_AO_DADO_DE_SAUDE));
        // Regulação do acesso (ADR-0087): consultar o andamento não mostra o dado clínico; solicitar e regular, sim.
        PERMISSOES.put("REGULACAO.CONSULTAR", new Def("Acompanhar o andamento das solicitações de regulação", OPERACAO));
        PERMISSOES.put("REGULACAO.SOLICITAR", new Def("Solicitar regulação (encaminhar a especialidade, exame ou procedimento)", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("REGULACAO.REGULAR", new Def("Regular a fila: autorizar com vaga, devolver, negar e manter o catálogo", ACESSO_AO_DADO_DE_SAUDE));
        // Regulação de urgência (ADR-0176): o atendente abre e registra os deslocamentos; o médico regulador decide; consultar é só o andamento.
        PERMISSOES.put("URGENCIA.CONSULTAR", new Def("Acompanhar o andamento das ocorrências de urgência, sem dado clínico", OPERACAO));
        PERMISSOES.put("URGENCIA.RECEBER", new Def("Abrir ocorrências na central de urgência e registrar os deslocamentos (TARM e rádio-operador)",
                ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("URGENCIA.REGULAR", new Def("Regular as urgências: gravidade, decisão, destino com vaga zero e encerramento",
                ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("APH.REGISTRAR", new Def("Preencher a ficha clínica do atendimento pré-hospitalar na ocorrência da central", ACESSO_AO_DADO_DE_SAUDE));
        // Laboratório assistencial (ADR-0093).
        PERMISSOES.put("EXAME.SOLICITAR", new Def("Pedir exames laboratoriais e de imagem", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("LABORATORIO.COLETAR", new Def("Registrar a coleta de amostras", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("LABORATORIO.ANALISAR", new Def("Registrar resultados e rejeitar amostras", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("LABORATORIO.LIBERAR", new Def("Liberar e retificar resultados (responsável técnico)", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("LABORATORIO.GERENCIAR", new Def("Manter o catálogo de exames", OPERACAO));
        PERMISSOES.put("IMAGEM.REALIZAR", new Def("Realizar exames de imagem no serviço de imagem (técnico em radiologia)",
                ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("IMAGEM.LAUDAR", new Def("Laudar exames de imagem, registrar adendo e comunicar achado crítico (radiologista)",
                ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("INTERNACAO.CONSULTAR", new Def("Ver o mapa de leitos e as internações", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("INTERNACAO.GERENCIAR", new Def("Internar e trocar paciente de leito", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("INTERNACAO.ALTA", new Def("Dar alta da internação (ato médico)", ACESSO_AO_DADO_DE_SAUDE));
        // Central de regulação de leitos (ADR-0184).
        PERMISSOES.put("INTERNACAO.SOLICITAR_VAGA", new Def("Pedir leito de outra unidade à central de regulação de leitos", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("AIH.SOLICITAR", new Def("Preencher o laudo para solicitação de AIH da internação", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("AIH.AUTORIZAR", new Def("Autorizar, devolver ou negar o laudo de AIH (autorização interna, com o número)", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("AIH.NUMERAR", new Def("Cadastrar as faixas de numeração de AIH recebidas do gestor", OPERACAO));
        PERMISSOES.put("PRESCRICAO.HOSPITALAR", new Def("Prescrever na internação: medicamentos, dieta e cuidados, por 24 horas", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("PRESCRICAO.CHECAR", new Def("Checar os horários da prescrição hospitalar e reaprazar", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("DIETA.CHECAR", new Def("Checar a aceitação da dieta prescrita na internação", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("CENSO.FECHAR", new Def("Fechar e reabrir o censo hospitalar diário da unidade", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("LEITO.REGULAR", new Def("Regular a fila de leitos: reservar leito em outra unidade, vaga zero e negar", ACESSO_AO_DADO_DE_SAUDE));
        // Documentos (ADR-0172): anexar não dá direito de abrir; abrir segue o prontuário.
        PERMISSOES.put("DOCUMENTO.ANEXAR", new Def("Anexar documentos ao paciente, enviar versões e retirar", OPERACAO));
        PERMISSOES.put("LEITO.GERENCIAR", new Def("Cadastrar, bloquear e liberar leitos", OPERACAO));
        // Quem cadastra leitos não muda o limite (ADR-0171): o número é da gestão.
        PERMISSOES.put("LEITO.AUTORIZAR", new Def("Definir quantos leitos a unidade tem autorizados por capacidade", OPERACAO));
        PERMISSOES.put("EQUIPE.GERENCIAR", new Def("Cadastrar equipes e registrar a entrada e a saída de membros", OPERACAO));
        // Território (ADR-0125): o recorte e as associações; ler é de quem está logado.
        PERMISSOES.put("TERRITORIO.GERENCIAR", new Def("Cadastrar segmentos, territórios, áreas e microáreas e atribuir a equipe e o ACS", OPERACAO));
        // Cadastro territorial (ADR-0127): composição familiar e condições de moradia são dado pessoal; leitura auditada.
        PERMISSOES.put("TERRITORIO.CADASTRAR", new Def("Cadastrar imóveis, domicílios e famílias do território", ACESSO_AO_DADO_DE_SAUDE));
        PERMISSOES.put("ESCALA.GERENCIAR", new Def("Montar a escala: turnos, vagas, designação, troca e cópia de semana", OPERACAO));
        // Fora de todos os papéis padrão (ADR-0071): quem administra o sistema não audita a si mesmo.
        PERMISSOES.put("AUDITORIA.CONSULTAR", new Def("Consultar a trilha de auditoria", ADMINISTRACAO_DO_SISTEMA));
    }

    private record PapelPadrao(String nome, String descricao, List<String> permissoes) {
    }

    static final Map<String, PapelPadrao> PAPEIS = new LinkedHashMap<>();

    static {
        PAPEIS.put(ADMINISTRADOR_PLATAFORMA, new PapelPadrao("Administrador da plataforma",
                "Usuários, acessos e estrutura. Não lê dado de saúde (ADR-0054).",
                List.of("USUARIO.GERENCIAR", "ACESSO.GERENCIAR", "ORGANIZACAO.GERENCIAR", "COMUNICACAO.PUBLICAR", "CONTEUDO.EDITAR", "CONTEUDO.REVISAR",
                        "LEITO.AUTORIZAR")));
        PAPEIS.put("GESTOR", new PapelPadrao("Gestor",
                "Gestão de RH, setor administrativo, estoque e acessos no seu escopo.",
                List.of("DOCUMENTO.ANEXAR", "ACESSO.GERENCIAR", "RH.CONSULTAR", "RH.GERENCIAR", "ADMINISTRATIVO.CONSULTAR",
                        "ADMINISTRATIVO.GERENCIAR", "PACIENTE.CONSULTAR", "FARMACIA.CONSULTAR", "EQUIPE.GERENCIAR",
                        "ESCALA.GERENCIAR", "PROGRAMA.GERENCIAR", "VIGILANCIA.GERENCIAR", "TERRITORIO.GERENCIAR",
                        "INDICADOR.CONSULTAR", "IMUNIZACAO.GERENCIAR", "INTERSETORIAL.GERENCIAR", "EMERGENCIA.REGISTRAR", "EMERGENCIA.ATIVAR",
                        "EMERGENCIA.COORDENAR", "CONTINGENCIA.GERENCIAR", "ZOONOSES.GERENCIAR", "COMUNICACAO.PUBLICAR", "CONTEUDO.EDITAR",
                        "CONTEUDO.REVISAR", "LEITO.AUTORIZAR", "URGENCIA.CONSULTAR",
                        "ESTOQUE.CONSULTAR", "ESTOQUE.GERENCIAR",
                        "TRANSPORTE.CONSULTAR", "TRANSPORTE.GERENCIAR", "REDE.CONSULTAR", "REDE.GERENCIAR", "AIH.NUMERAR", "TFD.AUTORIZAR",
                        "VISA.GERENCIAR")));
        PAPEIS.put("RECEPCAO", new PapelPadrao("Recepção",
                "Cadastro de pacientes, agenda e abertura de atendimentos.",
                List.of("DOCUMENTO.ANEXAR", "PACIENTE.CONSULTAR", "PACIENTE.CADASTRAR", "AGENDAMENTO.GERENCIAR", "ATENDIMENTO.GERENCIAR",
                        "REGULACAO.CONSULTAR",
                        // Informar a família onde o paciente está internado (ADR-0098).
                        "INTERNACAO.CONSULTAR",
                        // Pedir o transporte da consulta, do exame ou da sessão (ADR-0190).
                        "TRANSPORTE.SOLICITAR")));
        PAPEIS.put("MEDICO", new PapelPadrao("Médico",
                "Consulta, prescrição e procedimentos.",
                List.of("DOCUMENTO.ANEXAR", "PACIENTE.CONSULTAR", "AGENDAMENTO.GERENCIAR", "ATENDIMENTO.GERENCIAR", "PRONTUARIO.CONSULTAR",
                        "CONSULTA.REGISTRAR", "PROCEDIMENTO.REGISTRAR", "REGULACAO.SOLICITAR", "REGULACAO.CONSULTAR",
                        "EXAME.SOLICITAR", "INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", "INTERNACAO.SOLICITAR_VAGA", "AIH.SOLICITAR", "PRESCRICAO.HOSPITALAR",
                        // Acompanhamento programático (ADR-0119) e saúde mental (ADR-0120).
                        "PROGRAMA.ACOMPANHAR", "SAUDE_MENTAL.ACOLHER", "SAUDE_MENTAL.ACOMPANHAR", "SAUDE_MENTAL.CRISE", "VIGILANCIA.NOTIFICAR")));
        PAPEIS.put("MEDICO_REGULADOR", new PapelPadrao("Médico regulador",
                "Regula a fila da Central de Regulação do Acesso no seu escopo (ADR-0087).",
                List.of("PACIENTE.CONSULTAR", "REGULACAO.CONSULTAR", "REGULACAO.REGULAR", "REDE.CONSULTAR")));
        // Regulação de urgência (ADR-0176): a central do 192, pela Portaria 2.048/2002.
        PAPEIS.put("ATENDENTE_REGULACAO_URGENCIA", new PapelPadrao("Atendente de regulação (TARM)",
                "Atende o chamado, abre a ocorrência e registra os deslocamentos do recurso na central de urgência (ADR-0176).",
                List.of("PACIENTE.CONSULTAR", "URGENCIA.CONSULTAR", "URGENCIA.RECEBER", "TRANSPORTE.CONSULTAR")));
        PAPEIS.put("MEDICO_REGULADOR_URGENCIA", new PapelPadrao("Médico regulador de urgência",
                "Classifica a gravidade, decide a conduta, indica o destino com vaga zero e encerra as ocorrências (ADR-0176).",
                List.of("PACIENTE.CONSULTAR", "URGENCIA.CONSULTAR", "URGENCIA.REGULAR", "TRANSPORTE.CONSULTAR", "INTERNACAO.SOLICITAR_VAGA")));
        PAPEIS.put("EQUIPE_INTERVENCAO_SAMU", new PapelPadrao("Equipe de intervenção do SAMU",
                "Médico intervencionista, enfermeiro e técnico da viatura: a ficha clínica do atendimento na cena (ADR-0188).",
                List.of("PACIENTE.CONSULTAR", "URGENCIA.CONSULTAR", "APH.REGISTRAR", "TRANSPORTE.CONSULTAR")));
        // Transporte (ADR-0178).
        PAPEIS.put("COORDENADOR_DE_FROTA", new PapelPadrao("Coordenador de frota",
                "Frota, manutenção e plantão das viaturas, com a tripulação (ADR-0178).",
                List.of("TRANSPORTE.CONSULTAR", "TRANSPORTE.GERENCIAR")));
        PAPEIS.put("COMISSAO_TFD", new PapelPadrao("Comissão de TFD",
                "Analisa o laudo e autoriza ou nega o tratamento fora do domicílio (Portaria SAS 55/1999, ADR-0190).",
                List.of("PACIENTE.CONSULTAR", "TFD.AUTORIZAR", "TRANSPORTE.CONSULTAR")));
        PAPEIS.put("ENFERMEIRO", new PapelPadrao("Enfermeiro",
                "Classificação de risco, evolução, procedimentos e medicação.",
                List.of("DOCUMENTO.ANEXAR", "PACIENTE.CONSULTAR", "ATENDIMENTO.GERENCIAR", "PRONTUARIO.CONSULTAR", "TRIAGEM.REGISTRAR",
                        "EVOLUCAO.REGISTRAR", "PROCEDIMENTO.REGISTRAR", "MEDICACAO.ADMINISTRAR",
                        // Pedido de exames em protocolos da atenção básica e do pré-natal (ADR-0093).
                        "EXAME.SOLICITAR", "LABORATORIO.COLETAR",
                        // Admissão, troca de leito e liberação depois da higienização (ADR-0098); a alta é do médico.
                        "INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "LEITO.GERENCIAR", "PRESCRICAO.CHECAR",
                        // Acompanhamento programático (ADR-0119) e saúde mental (ADR-0120).
                        "PROGRAMA.ACOMPANHAR", "SAUDE_MENTAL.ACOLHER", "SAUDE_MENTAL.ACOMPANHAR", "SAUDE_MENTAL.CRISE", "VIGILANCIA.NOTIFICAR",
                        // Cadastro domiciliar e familiar (ADR-0127) e vacinação (ADR-0147).
                        "TERRITORIO.CADASTRAR", "VACINACAO.REGISTRAR")));
        PAPEIS.put("COORDENADOR_DE_ENFERMAGEM", new PapelPadrao("Coordenador de enfermagem",
                "O que o enfermeiro faz, mais retificar registro clínico de outro profissional.",
                List.of("PACIENTE.CONSULTAR", "ATENDIMENTO.GERENCIAR", "PRONTUARIO.CONSULTAR", "TRIAGEM.REGISTRAR",
                        "EVOLUCAO.REGISTRAR", "PROCEDIMENTO.REGISTRAR", "MEDICACAO.ADMINISTRAR",
                        "REGISTRO_CLINICO.RETIFICAR_DE_OUTROS", "INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "PRESCRICAO.CHECAR",
                        "LEITO.GERENCIAR", "CENSO.FECHAR", "EQUIPE.GERENCIAR", "ESCALA.GERENCIAR", "PROGRAMA.ACOMPANHAR",
                        "SAUDE_MENTAL.ACOLHER", "SAUDE_MENTAL.ACOMPANHAR", "SAUDE_MENTAL.CRISE", "VIGILANCIA.NOTIFICAR",
                        "TERRITORIO.GERENCIAR", "TERRITORIO.CADASTRAR", "INDICADOR.CONSULTAR", "VACINACAO.REGISTRAR",
                        "IMUNIZACAO.GERENCIAR", "EMERGENCIA.REGISTRAR", "EMERGENCIA.COORDENAR", "ESTOQUE.CONSULTAR", "ESTOQUE.REQUISITAR",
                        "TRANSPORTE.SOLICITAR")));
        // Agente comunitário de saúde (ADR-0127): a função já existia nas equipes (ADR-0103); faltava o papel de acesso.
        PAPEIS.put("AGENTE_COMUNITARIO_SAUDE", new PapelPadrao("Agente comunitário de saúde",
                "Cadastro domiciliar e familiar da microárea e cadastro de pacientes.",
                List.of("PACIENTE.CONSULTAR", "PACIENTE.CADASTRAR", "TERRITORIO.CADASTRAR")));
        PAPEIS.put("TECNICO_DE_ENFERMAGEM", new PapelPadrao("Técnico de enfermagem",
                "Procedimentos e medicação. Sem classificação de risco nem evolução (COFEN 661/2021).",
                List.of("PACIENTE.CONSULTAR", "PRONTUARIO.CONSULTAR", "PROCEDIMENTO.REGISTRAR", "MEDICACAO.ADMINISTRAR",
                        "LABORATORIO.COLETAR", "INTERNACAO.CONSULTAR", "VIGILANCIA.NOTIFICAR", "VACINACAO.REGISTRAR",
                        // Checagem dos horários da prescrição hospitalar (ADR-0187).
                        "PRESCRICAO.CHECAR")));
        PAPEIS.put("NUTRICIONISTA", new PapelPadrao("Nutricionista",
                "Acompanha a dieta prescrita na internação e checa a aceitação (ADR-0187).",
                List.of("PACIENTE.CONSULTAR", "INTERNACAO.CONSULTAR", "DIETA.CHECAR")));
        PAPEIS.put("TECNICO_DE_LABORATORIO", new PapelPadrao("Técnico de laboratório",
                "Coleta, recebe e analisa amostras; registra resultados (ADR-0093).",
                List.of("PACIENTE.CONSULTAR", "LABORATORIO.COLETAR", "LABORATORIO.ANALISAR")));
        PAPEIS.put("RESPONSAVEL_TECNICO_LABORATORIO", new PapelPadrao("Responsável técnico do laboratório",
                "Analisa, libera e retifica resultados e mantém o catálogo de exames (ADR-0093).",
                List.of("PACIENTE.CONSULTAR", "LABORATORIO.COLETAR", "LABORATORIO.ANALISAR", "LABORATORIO.LIBERAR",
                        "LABORATORIO.GERENCIAR")));
        PAPEIS.put("TECNICO_EM_RADIOLOGIA", new PapelPadrao("Técnico em radiologia",
                "Realiza os exames de imagem no serviço de imagem (ADR-0182).",
                List.of("PACIENTE.CONSULTAR", "IMAGEM.REALIZAR")));
        PAPEIS.put("MEDICO_RADIOLOGISTA", new PapelPadrao("Médico radiologista",
                "Lauda os exames de imagem, registra adendo e comunica achado crítico (ADR-0182).",
                List.of("PACIENTE.CONSULTAR", "IMAGEM.LAUDAR", "PRONTUARIO.CONSULTAR")));
        PAPEIS.put("REGULADOR_DE_LEITOS", new PapelPadrao("Regulador de leitos",
                "Central de regulação de leitos entre unidades: fila, vagas compatíveis, reserva com prazo, vaga zero e negação (ADR-0184).",
                List.of("PACIENTE.CONSULTAR", "INTERNACAO.CONSULTAR", "LEITO.REGULAR", "TRANSPORTE.CONSULTAR")));
        PAPEIS.put("MEDICO_AUTORIZADOR", new PapelPadrao("Médico autorizador",
                "Autorização interna das AIH: fila de laudos, autorizar com o número, devolver para correção e negar (ADR-0186).",
                List.of("PACIENTE.CONSULTAR", "INTERNACAO.CONSULTAR", "PRONTUARIO.CONSULTAR", "AIH.AUTORIZAR")));
        PAPEIS.put("GESTOR_DE_LEITOS", new PapelPadrao("Gestor de leitos (NIR)",
                "Núcleo Interno de Regulação: mapa de leitos, internação, troca de leito, bloqueio e liberação (ADR-0098).",
                List.of("PACIENTE.CONSULTAR", "INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "LEITO.GERENCIAR", "CENSO.FECHAR")));
        // Saúde Mental (ADR-0120): as funções já existiam nas equipes (ADR-0103); faltava o papel de acesso.
        PAPEIS.put("PSICOLOGO", new PapelPadrao("Psicólogo",
                "Acolhimento e acompanhamento em saúde mental; lê o prontuário com vínculo.",
                List.of("DOCUMENTO.ANEXAR", "PACIENTE.CONSULTAR", "PRONTUARIO.CONSULTAR", "PROGRAMA.ACOMPANHAR", "SAUDE_MENTAL.ACOLHER",
                        "SAUDE_MENTAL.ACOMPANHAR", "SAUDE_MENTAL.CRISE", "VIGILANCIA.NOTIFICAR")));
        PAPEIS.put("ASSISTENTE_SOCIAL", new PapelPadrao("Assistente social",
                "Acolhimento e acompanhamento psicossocial; lê o prontuário com vínculo e mantém o catálogo intersetorial.",
                List.of("DOCUMENTO.ANEXAR", "PACIENTE.CONSULTAR", "PRONTUARIO.CONSULTAR", "PROGRAMA.ACOMPANHAR", "SAUDE_MENTAL.ACOLHER",
                        "SAUDE_MENTAL.ACOMPANHAR", "SAUDE_MENTAL.CRISE", "VIGILANCIA.NOTIFICAR", "INTERSETORIAL.GERENCIAR",
                        // A alta hospitalar e o TFD passam pelo serviço social (ADR-0190).
                        "TRANSPORTE.SOLICITAR")));
        // Vigilância epidemiológica (ADR-0124): investiga com acesso ao prontuário; o detalhe dos agravos sensíveis é dela.
        PAPEIS.put("VIGILANCIA_EPIDEMIOLOGICA", new PapelPadrao("Vigilância epidemiológica",
                "Investiga, classifica e encerra notificações e mantém o catálogo de agravos.",
                List.of("PACIENTE.CONSULTAR", "PRONTUARIO.CONSULTAR", "VIGILANCIA.NOTIFICAR", "VIGILANCIA.INVESTIGAR", "VIGILANCIA.GERENCIAR",
                        "IMUNIZACAO.GERENCIAR", "EMERGENCIA.REGISTRAR")));
        // Controle de vetores (ADR-0162): o agente de endemias vistoria; a vigilância ambiental coordena o controle de zoonoses.
        PAPEIS.put("AGENTE_DE_ENDEMIAS", new PapelPadrao("Agente de endemias",
                "Vistoria de imóveis no controle vetorial: depósitos, focos e tratamento focal (Lei 11.350/2006).",
                List.of("ZOONOSES.VISTORIAR")));
        PAPEIS.put("VIGILANCIA_AMBIENTAL", new PapelPadrao("Vigilância ambiental",
                "Coordenação de zoonoses e vetores: vistorias, índices do ciclo, ações de controle e bloqueios.",
                List.of("ZOONOSES.VISTORIAR", "ZOONOSES.GERENCIAR", "EMERGENCIA.REGISTRAR")));
        // Vigilância Sanitária (ADR-0199): a autoridade sanitária fiscaliza; a coordenação licencia e julga (a decisão do processo, na V3b).
        PAPEIS.put("FISCAL_SANITARIO", new PapelPadrao("Fiscal sanitário",
                "Autoridade sanitária: inspeciona estabelecimentos, registra irregularidades e intima a corrigir (Lei 6.437/1977).",
                List.of("VISA.INSPECIONAR")));
        PAPEIS.put("COORDENACAO_VISA", new PapelPadrao("Coordenação da vigilância sanitária",
                "Licenciamento sanitário, denúncias, roteiros de inspeção e acompanhamento das inspeções.",
                List.of("VISA.INSPECIONAR", "VISA.GERENCIAR", "EMERGENCIA.REGISTRAR")));
        // Saúde Bucal (ADR-0134): as funções já existiam na eSB (ADR-0103); faltavam os papéis de acesso.
        PAPEIS.put("CIRURGIAO_DENTISTA", new PapelPadrao("Cirurgião-dentista",
                "Avaliação, diagnóstico e procedimentos odontológicos (Lei 5.081/1966); lê o prontuário com vínculo.",
                List.of("PACIENTE.CONSULTAR", "AGENDAMENTO.GERENCIAR", "ATENDIMENTO.GERENCIAR", "PRONTUARIO.CONSULTAR",
                        "ODONTOLOGIA.REGISTRAR", "ODONTOLOGIA.PLANEJAR", "PROCEDIMENTO.REGISTRAR", "REGULACAO.SOLICITAR", "REGULACAO.CONSULTAR",
                        "PROGRAMA.ACOMPANHAR", "VIGILANCIA.NOTIFICAR")));
        PAPEIS.put("TECNICO_SAUDE_BUCAL", new PapelPadrao("Técnico em saúde bucal",
                "Procedimentos permitidos, sob supervisão do dentista (Lei 11.889/2008). Sem avaliação nem diagnóstico.",
                List.of("PACIENTE.CONSULTAR", "PRONTUARIO.CONSULTAR", "PROCEDIMENTO.REGISTRAR", "PROGRAMA.ACOMPANHAR")));
        // Estoque e almoxarifado (ADR-0177).
        PAPEIS.put("ALMOXARIFE", new PapelPadrao("Almoxarife",
                "Entrada, saída por consumo, perda e ajuste de materiais e insumos gerais (ADR-0177).",
                List.of("ESTOQUE.CONSULTAR", "ESTOQUE.MOVIMENTAR", "ESTOQUE.REQUISITAR", "ESTOQUE.ATENDER")));
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
        Set<String> novasPermissoes = new HashSet<>();
        for (Map.Entry<String, Def> e : PERMISSOES.entrySet()) {
            if (permissaoRepository.findByCodigo(e.getKey()).isEmpty()) {
                permissaoRepository.save(new Permissao(e.getKey(), e.getValue().descricao(), e.getValue().dimensao()));
                novasPermissoes.add(e.getKey());
            }
        }
        int novosPapeis = 0;
        int acrescimos = 0;
        for (Map.Entry<String, PapelPadrao> e : PAPEIS.entrySet()) {
            Optional<Papel> existente = papelRepository.findByCodigo(e.getKey());
            if (existente.isEmpty()) {
                Set<Permissao> permissoes = new HashSet<>(permissaoRepository.findByCodigoIn(e.getValue().permissoes()));
                papelRepository.save(new Papel(e.getKey(), e.getValue().nome(), e.getValue().descricao(), permissoes));
                novosPapeis++;
                continue;
            }
            // Permissão que acabou de nascer entra nos papéis padrão que já existiam e a preveem (ADR-0087).
            // Só na criação dela: depois, o que a administração tirou do papel continua tirado.
            List<String> acrescentar = e.getValue().permissoes().stream().filter(novasPermissoes::contains).toList();
            if (!acrescentar.isEmpty()) {
                Papel papel = existente.get();
                papel.getPermissoes().addAll(permissaoRepository.findByCodigoIn(acrescentar));
                papelRepository.save(papel);
                acrescimos += acrescentar.size();
            }
        }
        if (novasPermissoes.size() + novosPapeis + acrescimos > 0) {
            log.info("Catálogo de acesso: {} permissão(ões) e {} papel(éis) padrão criados, {} permissão(ões) acrescentada(s) "
                    + "a papéis existentes (ADR-0066, ADR-0087).", novasPermissoes.size(), novosPapeis, acrescimos);
        }
    }
}
