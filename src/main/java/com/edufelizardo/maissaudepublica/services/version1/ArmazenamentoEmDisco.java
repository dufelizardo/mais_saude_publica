package com.edufelizardo.maissaudepublica.services.version1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Arquivos num volume próprio (ADR-0172), fora do banco. Grava num temporário e move, para nunca sobrar um arquivo pela metade. */
@Service
public class ArmazenamentoEmDisco implements ArmazenamentoDeArquivos {

    private final Path raiz;

    public ArmazenamentoEmDisco(@Value("${app.documentos.diretorio}") String diretorio) {
        this.raiz = Path.of(diretorio);
    }

    @Override
    public void gravar(String chave, byte[] conteudo) {
        Path destino = caminho(chave);
        try {
            Files.createDirectories(destino.getParent());
            Path temporario = Files.createTempFile(destino.getParent(), "envio", ".tmp");
            Files.write(temporario, conteudo);
            Files.move(temporario, destino, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gravar o arquivo.", e);
        }
    }

    @Override
    public byte[] ler(String chave) {
        try {
            return Files.readAllBytes(caminho(chave));
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler o arquivo.", e);
        }
    }

    private Path caminho(String chave) {
        return raiz.resolve(chave.substring(0, 2)).resolve(chave);
    }
}
