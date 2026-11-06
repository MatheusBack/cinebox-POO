package br.com.cinebox.model.form;

import jakarta.validation.constraints.*;
import java.util.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import br.com.cinebox.model.entity.Capa;
import org.springframework.format.annotation.DateTimeFormat;


public class AvaliacaoForm {
    @NotNull(message = "Selecione um filme.")
    private Long filmeId;
    @NotNull(message = "Informe a nota.") @DecimalMin(value = "0.5", message = "A nota mínima é 0,5.") @DecimalMax(value = "5.0", message = "A nota máxima é 5.")
    private BigDecimal nota;
    @Size(max = 3000, message = "Use até 3000 caracteres.")
    private String comentario;
    @NotNull(message = "Informe a data.") @PastOrPresent(message = "A data não pode estar no futuro.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataAssistido;

    private boolean favorito;

    private boolean spoiler;
    public AvaliacaoForm() { dataAssistido = LocalDate.now(); }
    public Long getFilmeId() { return filmeId; }
    public void setFilmeId(Long filmeId) { this.filmeId = filmeId; }
    public BigDecimal getNota() { return nota; }
    public void setNota(BigDecimal nota) { this.nota = nota; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public LocalDate getDataAssistido() { return dataAssistido; }
    public void setDataAssistido(LocalDate dataAssistido) { this.dataAssistido = dataAssistido; }
    public boolean isFavorito() { return favorito; }
    public void setFavorito(boolean favorito) { this.favorito = favorito; }
    public boolean isSpoiler() { return spoiler; }
    public void setSpoiler(boolean spoiler) { this.spoiler = spoiler; }
}
