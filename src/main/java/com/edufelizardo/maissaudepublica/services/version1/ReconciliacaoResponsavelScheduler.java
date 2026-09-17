package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rede de segurança do vínculo fraco por CPF entre UnidadeDeSaude e Profissional (ver ADR-0014).
 * A reconciliação principal é síncrona (em {@link ProfissionalService#create} e em
 * {@link UnidadeSaudeService#create}) — este job só cobre condições de corrida e dados
 * corrigidos manualmente no banco, então não precisa rodar com frequência alta.
 */
@Component
public class ReconciliacaoResponsavelScheduler {

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Scheduled(cron = "${reconciliacao.responsavel.cron:0 0 * * * *}")
    @Transactional
    public void reconciliar() {
        for (UnidadeDeSaude unidade : unidadeDeSaudeRepository.findAll()) {
            if (unidade.getResponsavel() == null && unidade.getResponsavelCpf() != null) {
                profissionalRepository.findByCpf(unidade.getResponsavelCpf())
                        .ifPresent(unidade::setResponsavel);
            }
        }
    }
}
