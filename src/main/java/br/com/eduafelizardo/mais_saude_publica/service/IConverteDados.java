package br.com.eduafelizardo.mais_saude_publica.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface IConverteDados {
    <T> T obterDados(String json, Class<T> classe) throws JsonProcessingException;
}
