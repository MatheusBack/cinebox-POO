package br.com.cinebox.model.service;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FotoPerfilService {
    public record Foto(byte[] bytes, String tipo) {}

    public Foto validar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) return null;
        if (arquivo.getSize() > 2 * 1024 * 1024)
            throw new RegraNegocioException("A foto precisa ter no máximo 2 MB.");
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(arquivo.getBytes()))) {
            if (input == null) throw new RegraNegocioException("Não foi possível ler a foto.");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new RegraNegocioException("Envie uma imagem PNG ou JPEG válida.");
            ImageReader reader = readers.next();
            try {
                String formato = reader.getFormatName().toLowerCase(java.util.Locale.ROOT);
                if (!formato.equals("png") && !formato.equals("jpeg") && !formato.equals("jpg"))
                    throw new RegraNegocioException("Use uma foto PNG ou JPEG; SVG e outros formatos não são aceitos.");
                reader.setInput(input, true, true);
                int largura = reader.getWidth(0), altura = reader.getHeight(0);
                if (largura < 1 || altura < 1 || largura > 2048 || altura > 2048)
                    throw new RegraNegocioException("A foto deve ter no máximo 2048 × 2048 pixels.");
                BufferedImage imagem = reader.read(0);
                if (imagem == null) throw new RegraNegocioException("A foto está corrompida.");
                // Regrava para retirar metadados e não confiar em extensão/MIME informados pelo cliente.
                String saida = formato.equals("png") ? "png" : "jpg";
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                if (!ImageIO.write(imagem, saida, bytes)) throw new RegraNegocioException("Formato de imagem não suportado.");
                if (bytes.size() > 2 * 1024 * 1024) throw new RegraNegocioException("A imagem processada ultrapassa 2 MB; use uma foto menor.");
                return new Foto(bytes.toByteArray(), saida.equals("png") ? "image/png" : "image/jpeg");
            } finally { reader.dispose(); }
        } catch (IOException | IllegalArgumentException e) {
            throw new RegraNegocioException("Não foi possível ler a foto. Envie um PNG ou JPEG válido.");
        }
    }
}
