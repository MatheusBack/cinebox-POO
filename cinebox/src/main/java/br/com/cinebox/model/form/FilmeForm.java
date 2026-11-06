package br.com.cinebox.model.form;

import jakarta.validation.constraints.*;
import java.util.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import br.com.cinebox.model.entity.Capa;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;


public class FilmeForm {
    @NotBlank(message = "Informe o título.") @Size(max = 140, message = "Use até 140 caracteres.")
    private String titulo;
    @NotBlank(message = "Informe a direção.") @Size(max = 100, message = "Use até 100 caracteres.")
    private String diretor;
    @NotNull(message = "Informe o ano.") @Min(value = 1888, message = "O ano mínimo é 1888.") @Max(value = 2100, message = "O ano máximo é 2100.")
    private Integer anoLancamento;
    @NotNull(message = "Informe a duração.") @Min(value = 1, message = "Informe ao menos 1 minuto.") @Max(value = 1440, message = "Use até 1440 minutos.")
    private Integer duracaoMinutos;
    @NotBlank(message = "Escreva uma sinopse.") @Size(max = 2000, message = "Use até 2000 caracteres.")
    private String sinopse;
    @NotNull(message = "Selecione uma capa.")
    private Capa capa;
    private Set<Long> generoIds;
    @Size(max = 200, message = "Use até 200 caracteres nos novos gêneros.")
    private String novoGenero;
    private MultipartFile imagem;
    public FilmeForm() { generoIds = new LinkedHashSet<>(); capa = Capa.PADRAO; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDiretor() { return diretor; }
    public void setDiretor(String diretor) { this.diretor = diretor; }
    public Integer getAnoLancamento() { return anoLancamento; }
    public void setAnoLancamento(Integer anoLancamento) { this.anoLancamento = anoLancamento; }
    public Integer getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(Integer duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }
    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }
    public Capa getCapa() { return capa; }
    public void setCapa(Capa capa) { this.capa = capa; }
    public MultipartFile getImagem() { return imagem; }
    public void setImagem(MultipartFile imagem) { this.imagem = imagem; }
    public String getNovoGenero() { return novoGenero; }
    public void setNovoGenero(String novoGenero) { this.novoGenero = novoGenero; }
    public Set<Long> getGeneroIds() { return generoIds; }
    public void setGeneroIds(Set<Long> generoIds) { this.generoIds = generoIds; }
}
