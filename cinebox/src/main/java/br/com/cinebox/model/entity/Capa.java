package br.com.cinebox.model.entity;

public enum Capa {
    ORBITA("Órbita"), AREIA("Areia"), VIAGEM("Viagem"), CIDADE("Cidade"),
    CASA("Casa"), ENCONTROS("Encontros"), RITMO("Ritmo"), CONTATO("Contato"), PADRAO("Claquete");
    private final String rotulo;
    Capa(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
    public String getArquivo() { return "/img/capas/" + name().toLowerCase(java.util.Locale.ROOT) + ".svg"; }
}
