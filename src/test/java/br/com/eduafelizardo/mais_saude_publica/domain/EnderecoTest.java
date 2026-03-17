package br.com.eduafelizardo.mais_saude_publica.domain;

import br.com.eduafelizardo.mais_saude_publica.domain.dto.EnderecoRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes da classe Endereco")
class EnderecoTest {

    @Test
    @DisplayName("Deve criar Endereco a partir de EnderecoRecord")
    void testCriarEnderecoAPartirDeRecord() {
        // Arrange
        EnderecoRecord record = new EnderecoRecord(
                "01310100",
                "Avenida Paulista",
                "",
                "",
                "Bela Vista",
                "São Paulo",
                "SP",
                "São Paulo",
                "Sudeste",
                "3550308"
        );

        // Act
        Endereco endereco = new Endereco(record);

        // Assert
        assertAll("Endereco criado corretamente",
                () -> assertEquals("01310100", endereco.getCep()),
                () -> assertEquals("Avenida Paulista", endereco.getLogradouro()),
                () -> assertEquals("Bela Vista", endereco.getBairro()),
                () -> assertEquals("São Paulo", endereco.getLocalidade()),
                () -> assertEquals("SP", endereco.getUf()),
                () -> assertEquals("São Paulo", endereco.getEstado()),
                () -> assertEquals("Sudeste", endereco.getRegiao()),
                () -> assertEquals("3550308", endereco.getIbge())
        );
    }

    @Test
    @DisplayName("Deve validar igualdade entre dois Enderecos com mesmos dados")
    void testIgualdadeEnderecos() {
        // Arrange
        EnderecoRecord record1 = new EnderecoRecord(
                "01310100", "Avenida Paulista", "", "", "Bela Vista",
                "São Paulo", "SP", "São Paulo", "Sudeste", "3550308"
        );
        EnderecoRecord record2 = new EnderecoRecord(
                "01310100", "Avenida Paulista", "", "", "Bela Vista",
                "São Paulo", "SP", "São Paulo", "Sudeste", "3550308"
        );

        // Act
        Endereco endereco1 = new Endereco(record1);
        Endereco endereco2 = new Endereco(record2);

        // Assert
        assertEquals(endereco1, endereco2);
    }

    @Test
    @DisplayName("Deve gerar String corretamente com toString()")
    void testToString() {
        // Arrange
        EnderecoRecord record = new EnderecoRecord(
                "01310100", "Avenida Paulista", "", "", "Bela Vista",
                "São Paulo", "SP", "São Paulo", "Sudeste", "3550308"
        );
        Endereco endereco = new Endereco(record);

        // Act
        String str = endereco.toString();

        // Assert
        assertNotNull(str);
        assertTrue(str.contains("01310100"));
        assertTrue(str.contains("Avenida Paulista"));
    }

    @Test
    @DisplayName("Deve permitir atualização de dados via setter")
    void testSettersEnderecoUpdate() {
        // Arrange
        EnderecoRecord record = new EnderecoRecord(
                "01310100", "Avenida Paulista", "", "", "Bela Vista",
                "São Paulo", "SP", "São Paulo", "Sudeste", "3550308"
        );
        Endereco endereco = new Endereco(record);

        // Act
        endereco.setComplemento("Complemento novo");

        // Assert
        assertEquals("Complemento novo", endereco.getComplemento());
    }
}

