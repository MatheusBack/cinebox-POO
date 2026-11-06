package br.com.cinebox.model.form;

import jakarta.validation.constraints.*;
import java.util.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import br.com.cinebox.model.entity.Capa;
import org.springframework.format.annotation.DateTimeFormat;


public class ItemListaForm {
    @NotNull(message = "Selecione um filme.")
    private Long filmeId;
    @NotNull(message = "Informe a posição.") @Min(value = 1, message = "A posição mínima é 1.") @Max(value = 9999, message = "Use uma posição até 9999.")
    private Integer posicao;
    @Size(max = 300, message = "Use até 300 caracteres.")
    private String observacao;

    public Long getFilmeId() { return filmeId; }
    public void setFilmeId(Long filmeId) { this.filmeId = filmeId; }
    public Integer getPosicao() { return posicao; }
    public void setPosicao(Integer posicao) { this.posicao = posicao; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
