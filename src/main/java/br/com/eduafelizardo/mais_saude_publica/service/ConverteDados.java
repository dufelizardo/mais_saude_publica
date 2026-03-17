package br.com.eduafelizardo.mais_saude_publica.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class ConverteDados implements IConverteDados {
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public <T> T obterDados(String json, Class<T> classe) throws JsonProcessingException {
        return objectMapper.readValue(json, classe);
    }
}
