package br.com.eduafelizardo.mais_saude_publica.domain;

import br.com.eduafelizardo.mais_saude_publica.domain.dto.EnderecoRecord;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Endereco {
    private String cep;
    private String logradouro;
    private String complemento;
    private String unidade;
    private String bairro;
    private String localidade;
    private String uf;
    private String estado;
    private String regiao;
    private String ibge;

    // Construtores
    public Endereco() {
    }

    public Endereco(EnderecoRecord enderecoRecord) {
        this.cep = enderecoRecord.cep();
        this.logradouro = enderecoRecord.logradouro();
        this.complemento = enderecoRecord.complemento();
        this.unidade = enderecoRecord.unidade();
        this.bairro = enderecoRecord.bairro();
        this.localidade = enderecoRecord.localidade();
        this.uf = enderecoRecord.uf();
        this.estado = enderecoRecord.estado();
        this.regiao = enderecoRecord.regiao();
        this.ibge = enderecoRecord.ibge();
    }

    // Getters
    public String getCep() {
        return cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getUnidade() {
        return unidade;
    }

    public String getBairro() {
        return bairro;
    }

    public String getLocalidade() {
        return localidade;
    }

    public String getUf() {
        return uf;
    }

    public String getEstado() {
        return estado;
    }

    public String getRegiao() {
        return regiao;
    }

    public String getIbge() {
        return ibge;
    }

    // Setters
    public void setCep(String cep) {
        this.cep = cep;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public void setLocalidade(String localidade) {
        this.localidade = localidade;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setRegiao(String regiao) {
        this.regiao = regiao;
    }

    public void setIbge(String ibge) {
        this.ibge = ibge;
    }

    @Override
    public String toString() {
        return "Endereco{" +
                "cep='" + cep + '\'' +
                ", logradouro='" + logradouro + '\'' +
                ", complemento='" + complemento + '\'' +
                ", unidade='" + unidade + '\'' +
                ", bairro='" + bairro + '\'' +
                ", localidade='" + localidade + '\'' +
                ", uf='" + uf + '\'' +
                ", estado='" + estado + '\'' +
                ", regiao='" + regiao + '\'' +
                ", ibge='" + ibge + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Endereco endereco = (Endereco) o;

        if (cep != null ? !cep.equals(endereco.cep) : endereco.cep != null) return false;
        if (logradouro != null ? !logradouro.equals(endereco.logradouro) : endereco.logradouro != null) return false;
        if (complemento != null ? !complemento.equals(endereco.complemento) : endereco.complemento != null) return false;
        if (unidade != null ? !unidade.equals(endereco.unidade) : endereco.unidade != null) return false;
        if (bairro != null ? !bairro.equals(endereco.bairro) : endereco.bairro != null) return false;
        if (localidade != null ? !localidade.equals(endereco.localidade) : endereco.localidade != null) return false;
        if (uf != null ? !uf.equals(endereco.uf) : endereco.uf != null) return false;
        if (estado != null ? !estado.equals(endereco.estado) : endereco.estado != null) return false;
        if (regiao != null ? !regiao.equals(endereco.regiao) : endereco.regiao != null) return false;
        return ibge != null ? ibge.equals(endereco.ibge) : endereco.ibge == null;
    }

    @Override
    public int hashCode() {
        int result = cep != null ? cep.hashCode() : 0;
        result = 31 * result + (logradouro != null ? logradouro.hashCode() : 0);
        result = 31 * result + (complemento != null ? complemento.hashCode() : 0);
        result = 31 * result + (unidade != null ? unidade.hashCode() : 0);
        result = 31 * result + (bairro != null ? bairro.hashCode() : 0);
        result = 31 * result + (localidade != null ? localidade.hashCode() : 0);
        result = 31 * result + (uf != null ? uf.hashCode() : 0);
        result = 31 * result + (estado != null ? estado.hashCode() : 0);
        result = 31 * result + (regiao != null ? regiao.hashCode() : 0);
        result = 31 * result + (ibge != null ? ibge.hashCode() : 0);
        return result;
    }
}
