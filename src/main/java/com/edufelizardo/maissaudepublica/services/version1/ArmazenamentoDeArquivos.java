package com.edufelizardo.maissaudepublica.services.version1;

/** Onde os arquivos dos documentos ficam (ADR-0172). A chave é gerada pelo sistema; trocar de armazenamento é trocar esta implementação. */
public interface ArmazenamentoDeArquivos {

    void gravar(String chave, byte[] conteudo);

    byte[] ler(String chave);
}
