package br.com.eduafelizardo.mais_saude_publica.service;

import br.com.eduafelizardo.mais_saude_publica.domain.dto.EnderecoRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes da classe ConverteDados")
class ConverteDadosTest {

    private final ConverteDados converteDados = new ConverteDados();

    @Test
    @DisplayName("Deve converter JSON válido para EnderecoRecord com sucesso")
    void testConverterJsonValido() throws JsonProcessingException {
        // Arrange
        String jsonValido = """
                {
                    "cep": "02942000",
                    "logradouro": "Avenida Paulista",
                    "complemento": "",
                    "unidade": "",
                    "bairro": "Bela Vista",
                    "localidade": "São Paulo",
                    "uf": "SP",
                    "estado": "São Paulo",
                    "regiao": "Sudeste",
                    "ibge": "3550308"
                }
                """;

        // Act
        EnderecoRecord result = converteDados.obterDados(jsonValido, EnderecoRecord.class);

        // Assert
        assertNotNull(result);
        assertEquals("02942000", result.cep());
        assertEquals("Avenida Paulista", result.logradouro());
        assertEquals("Bela Vista", result.bairro());
        assertEquals("São Paulo", result.localidade());
        assertEquals("SP", result.uf());
    }

    @Test
    @DisplayName("Deve converter JSON com propriedades adicionais desconhecidas")
    void testConverterJsonComPropriedadesAdicionais() throws JsonProcessingException {
        // Arrange
        String jsonComPropsAdicionais = """
                {
                    "cep": "01310100",
                    "logradouro": "Avenida Paulista",
                    "complemento": "",
                    "unidade": "",
                    "bairro": "Bela Vista",
                    "localidade": "São Paulo",
                    "uf": "SP",
                    "estado": "São Paulo",
                    "regiao": "Sudeste",
                    "ibge": "3550308",
                    "novaPropriedade": "valor"
                }
                """;

        // Act & Assert - não deve lançar exceção
        assertDoesNotThrow(() -> {
            EnderecoRecord result = converteDados.obterDados(jsonComPropsAdicionais, EnderecoRecord.class);
            assertNotNull(result);
            assertEquals("01310100", result.cep());
        });
    }

    @Test
    @DisplayName("Deve lançar exceção ao converter JSON inválido")
    void testConverterJsonInvalido() {
        // Arrange
        String jsonInvalido = "{json mal formado";

        // Act & Assert
        assertThrows(JsonProcessingException.class, () -> 
            converteDados.obterDados(jsonInvalido, EnderecoRecord.class)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção ao converter para tipo não suportado")
    void testConverterParaTipoInvalido() {
        // Arrange
        String json = """
                {
                    "cep": "02942000"
                }
                """;

        // Act & Assert
        assertThrows(JsonProcessingException.class, () -> 
            converteDados.obterDados(json, Integer.class)
        );
    }
}

