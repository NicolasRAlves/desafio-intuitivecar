package com.intuitivecare.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipService {

    private static final String ZIP_FILE = "downloads/anexos.zip";

    public String compactarPDFs(List<String> fileNames) {
        File dir = new File("downloads");

        try (FileOutputStream fos = new FileOutputStream(ZIP_FILE);
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {

            for (String fileName : fileNames) {
                File file = new File(dir, fileName);
                if (!file.exists()) {
                    System.out.println("Arquivo não encontrado: " + fileName);
                    continue;
                }

                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zipOut.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zipOut.write(buffer, 0, len);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao compactar arquivos: " + e.getMessage(), e);
        }

        System.out.println("Arquivos compactados em: " + ZIP_FILE);
        return ZIP_FILE;
    }
}
