package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EnderecoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.HierarquicoZeroRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeDeSaudeAtivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeDeSaudeNomeUpdateRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.HierarquicoZeroResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HierarquicoZeroServiceTest {
    @Mock
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @InjectMocks
    private HierarquicoZeroService hierarquicoZeroService;

    private UnidadeDeSaude unidadeDeSaude;
    private Endereco endereco;
    private final String nomeInstituicao = "Unidade Teste";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mockando o Endereço
        endereco = mock(Endereco.class);
        lenient().when(endereco.getCep()).thenReturn("12345-678");
        lenient().when(endereco.getLogradouro()).thenReturn("Rua Exemplo");
        lenient().when(endereco.getNumeroLogradouro()).thenReturn("123");
        lenient().when(endereco.getComplemento()).thenReturn("Edifício Sede");
        lenient().when(endereco.getBairro()).thenReturn("Centro");
        lenient().when(endereco.getCidade()).thenReturn("Brasília");
        lenient().when(endereco.getEstado()).thenReturn("DF");
        lenient().when(endereco.getDdd()).thenReturn("");

        // Mockando a Unidade de Saúde
        unidadeDeSaude = mock(UnidadeDeSaude.class);
        lenient().when(unidadeDeSaude.getNome()).thenReturn(nomeInstituicao);
        lenient().when(unidadeDeSaude.getTipo()).thenReturn(0);
        lenient().when(unidadeDeSaude.getEndereco()).thenReturn(endereco);
        lenient().when(unidadeDeSaude.getEmail()).thenReturn("nao_informado@gov.br");

        // Mockando horários de funcionamento e atendimento
        lenient().when(unidadeDeSaude.getHorarioFuncionamento()).thenReturn(createHorarioFuncionamento());
        lenient().when(unidadeDeSaude.getHorarioAtendimento()).thenReturn(createHorarioAtendimento());

        // Configurando o repositório para retornar a unidade mockada
        lenient().when(unidadeDeSaudeRepository.findByTipo(TipoUnidadeDeSaude.ADMINISTRACAO1.getTipo()))
                .thenReturn(Optional.of(unidadeDeSaude));
    }


    @Test
    void testGetAllHierarquiasZeroService_ShouldReturnListOfUnidadeDeSaude() {
        // Arrange
        when(unidadeDeSaudeRepository.findByTipo(TipoUnidadeDeSaude.ADMINISTRACAO1.getTipo()))
                .thenReturn(Optional.of(unidadeDeSaude));

        // Act
        List<HierarquicoZeroResponseDto> result = hierarquicoZeroService.getAllHierarquiasZeroService();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(nomeInstituicao, result.get(0).getNome());
        assertEquals("12345-678", result.get(0).getEndereco().getCep());
        verify(unidadeDeSaudeRepository, times(1)).findByTipo(TipoUnidadeDeSaude.ADMINISTRACAO1.getTipo());
    }

    @Test
    void testFindByNomeHierarquicoZeroService_ShouldReturnUnidadeDeSaude() {
        // Arrange
        when(unidadeDeSaudeRepository.findByNomeAndTipo(nomeInstituicao, TipoUnidadeDeSaude.ADMINISTRACAO1.getTipo()))
                .thenReturn(Optional.of(unidadeDeSaude)); // Stubbing necessário para o teste

        // Act
        HierarquicoZeroResponseDto result = hierarquicoZeroService.findByNomeHierarquicoZeroService(nomeInstituicao);

        // Assert
        assertEquals(nomeInstituicao, result.getNome());
        assertEquals("12345-678", result.getEndereco().getCep()); // Verifica o cep do endereço
        verify(unidadeDeSaudeRepository, times(1)).findByNomeAndTipo(nomeInstituicao, TipoUnidadeDeSaude.ADMINISTRACAO1.getTipo());
    }


    @Test
    void testFindByNomeHierarquicoZeroService_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(unidadeDeSaudeRepository.findByNomeAndTipo(nomeInstituicao, TipoUnidadeDeSaude.ADMINISTRACAO1.getTipo()))
                .thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class, () ->
                hierarquicoZeroService.findByNomeHierarquicoZeroService(nomeInstituicao));
    }

    @Test
    void testCreateHierarquicoZeroService_ShouldSaveUnidadeDeSaude() {
        // Arrange
        when(unidadeDeSaudeRepository.save(any(UnidadeDeSaude.class))).thenReturn(unidadeDeSaude);

        // Crie e preencha o DTO
        HierarquicoZeroRequestDto dto = new HierarquicoZeroRequestDto();
        dto.setNome(nomeInstituicao);
        dto.setTipo(TipoUnidadeDeSaude.ADMINISTRACAO1.getTipo());

        EnderecoRequestDto enderecoDto = new EnderecoRequestDto();
        enderecoDto.setCep("70058-900");
        enderecoDto.setLogradouro("Esplanada dos Ministérios");
        enderecoDto.setNumeroLogradouro("Bloco G");
        enderecoDto.setComplemento("Edifício Sede");
        enderecoDto.setBairro("Aviao");
        enderecoDto.setCidade("Brasília");
        enderecoDto.setEstado("DF");
        enderecoDto.setDdd("");

        dto.setEndereco(enderecoDto); // Certifique-se de que o endereço está sendo adicionado ao DTO

        // Act
        HierarquicoZeroResponseDto result = hierarquicoZeroService.createHierarquicoZeroService(dto);

        // Assert
        assertNotNull(result);
        assertEquals(nomeInstituicao, result.getNome());
        verify(unidadeDeSaudeRepository, times(1)).save(any(UnidadeDeSaude.class));
    }

//    @Test
//    void testUpdateHierarquicoZeroNomeService_ShouldUpdateNome() {
//        // Arrange
//        UnidadeDeSaudeNomeUpdateRequestDto dto = new UnidadeDeSaudeNomeUpdateRequestDto();
//        dto.setNome("Novo Nome");
//
//        // Mock da unidade de saúde que será retornada
//        UnidadeDeSaude unidadeDeSaudeMock = mock(UnidadeDeSaude.class);
//        when(unidadeDeSaudeMock.getNome()).thenReturn("Unidade Teste"); // Nome inicial
//        when(unidadeDeSaudeRepository.findByNome(nomeInstituicao)).thenReturn(Optional.of(unidadeDeSaudeMock));
//
//        // Ato de setar o nome atualizado
//        doAnswer(invocation -> {
//            // O que acontece quando o método setNome é chamado
//            String novoNome = invocation.getArgument(0);
//            when(unidadeDeSaudeMock.getNome()).thenReturn(novoNome); // Atualiza o nome no mock
//            return null; // Método void
//        }).when(unidadeDeSaudeMock).setNome(anyString());
//
//        // Mock do repositório para que o save retorne a unidade com o novo nome
//        when(unidadeDeSaudeRepository.save(any(UnidadeDeSaude.class))).thenReturn(unidadeDeSaudeMock);
//
//        // Act
//        HierarquicoZeroResponseDto result = hierarquicoZeroService.updateHierarquicoZeroNomeService(nomeInstituicao, dto);
//
//        // Assert
//        assertEquals("Novo Nome", result.getNome());
//        verify(unidadeDeSaudeRepository, times(1)).findByNome(nomeInstituicao);
//        verify(unidadeDeSaudeMock, times(1)).setNome("Novo Nome"); // Verifica se o nome foi atualizado
//        verify(unidadeDeSaudeRepository, times(1)).save(unidadeDeSaudeMock); // Verifica se o save foi chamado
//    }

    @Test
    void testUpdateHierarquicoZeroNomeService_ShouldThrowResourceNotFoundException() {
        // Arrange
        UnidadeDeSaudeNomeUpdateRequestDto dto = new UnidadeDeSaudeNomeUpdateRequestDto();
        dto.setNome("Novo Nome");

        // Simula o comportamento do repositório
        when(unidadeDeSaudeRepository.findByNome(nomeInstituicao)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class, () ->
                hierarquicoZeroService.updateHierarquicoZeroNomeService(nomeInstituicao, dto));
    }


//    @Test
//    void testDisableHierarquicoZeroService_ShouldDisableUnidadeDeSaude() {
//        // Arrange
//        UnidadeDeSaudeAtivoRequestDto dto = new UnidadeDeSaudeAtivoRequestDto();
//        dto.setAtivo(false);
//
//        when(unidadeDeSaudeRepository.findByNome(nomeInstituicao)).thenReturn(Optional.of(unidadeDeSaude));
//        when(unidadeDeSaudeRepository.save(any(UnidadeDeSaude.class))).thenReturn(unidadeDeSaude);
//
//        // Act
//        HierarquicoZeroResponseDto result = hierarquicoZeroService.deletHierarquicoZeroService(nomeInstituicao, dto);
//
//        // Assert
//        assertFalse(result.isAtivo());
//        verify(unidadeDeSaudeRepository, times(1)).findByNome(nomeInstituicao);
//        verify(unidadeDeSaudeRepository, times(1)).save(unidadeDeSaude);
//    }

    private Map<DayOfWeek, String> createHorarioFuncionamento() {
        Map<DayOfWeek, String> horario = new HashMap<>();
        horario.put(DayOfWeek.MONDAY, "8:00 AM - 9:00 PM");
        horario.put(DayOfWeek.TUESDAY, "8:00 AM - 9:00 PM");
        horario.put(DayOfWeek.WEDNESDAY, "8:00 AM - 9:00 PM");
        horario.put(DayOfWeek.THURSDAY, "8:00 AM - 9:00 PM");
        horario.put(DayOfWeek.FRIDAY, "8:00 AM - 9:00 PM");
        return horario; // Retorna o mapa preenchido
    }

    private Map<DayOfWeek, String> createHorarioAtendimento() {
        Map<DayOfWeek, String> horario = new HashMap<>();
        horario.put(DayOfWeek.MONDAY, "8:00 AM - 6:00 PM");
        horario.put(DayOfWeek.TUESDAY, "8:00 AM - 6:00 PM");
        horario.put(DayOfWeek.WEDNESDAY, "8:00 AM - 6:00 PM");
        horario.put(DayOfWeek.THURSDAY, "8:00 AM - 6:00 PM");
        horario.put(DayOfWeek.FRIDAY, "8:00 AM - 6:00 PM");
        return horario; // Retorna o mapa preenchido
    }
}