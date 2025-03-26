package com.intuitivecare.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class DownloadService {

    private static final String DOWNLOAD_DIR = "downloads/";

    public void baixarPDFs(List<String> urls) throws IOException {
        Files.createDirectories(Path.of(DOWNLOAD_DIR));

        ExecutorService executor = Executors.newFixedThreadPool(urls.size());

        for (String url : urls) {
            executor.submit(() -> {
                try {
                    String fileName = url.substring(url.lastIndexOf("/") + 1);
                    Path filePath = Path.of(DOWNLOAD_DIR, fileName);

                    try (InputStream in = new URL(url).openStream()) {
                        Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Baixado: " + fileName);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao baixar " + url + ": " + e.getMessage());
                }
            });
        }


        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                throw new IOException("Timeout ao baixar arquivos");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            throw new IOException("Interrompido durante o download", e);
        }
    }
}