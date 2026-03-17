package br.com.eduafelizardo.mais_saude_publica;


import br.com.eduafelizardo.mais_saude_publica.domain.Endereco;
import br.com.eduafelizardo.mais_saude_publica.domain.dto.EnderecoRecord;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpClient.newHttpClient;

public class Principal {
    public static void main(String[] args) {

        String viaCep = "https://viacep.com.br/ws/02942000/json/";


        HttpClient client = newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(viaCep))
                .build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


        Gson gson = new Gson();

        String json = response.body();
        System.out.println(json);

        EnderecoRecord enderecoRecord = gson.fromJson(json, EnderecoRecord.class);
        System.out.println(enderecoRecord);
        Endereco endereco = new Endereco(enderecoRecord);
        System.out.println(endereco);
    }
}
