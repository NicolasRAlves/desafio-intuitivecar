package com.intuitivecare.controller;

import com.intuitivecare.service.DownloadService;
import com.intuitivecare.service.PdfExtractionService;
import com.intuitivecare.service.ScraperService;
import com.intuitivecare.service.ZipService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/scraper")
public class ScraperController {

    private final ScraperService scraperService;
    private final DownloadService downloadService;
    private final ZipService zipService;
    private final PdfExtractionService pdfExtractionService;

    public ScraperController(ScraperService scraperService, DownloadService downloadService, ZipService zipService, PdfExtractionService pdfExtractionService) {
        this.scraperService = scraperService;
        this.downloadService = downloadService;
        this.zipService = zipService;
        this.pdfExtractionService = pdfExtractionService;
    }

    @GetMapping("/executar")
    public ResponseEntity<Resource> executarScraping() {
        try {
            List<String> pdfLinks = scraperService.obterLinksPDFs();

            List<String> linksDesejados = pdfLinks.stream()
                    .filter(link -> link.contains("Anexo_I_Rol_2021RN_465.2021_RN627L.2024.pdf")
                            || link.contains("Anexo_II_DUT_2021_RN_465.2021_RN628.2025_RN629.2025.pdf"))
                    .collect(Collectors.toList());

            if (linksDesejados.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(null); // Retorna 400 se não houver os anexos
            }

            downloadService.baixarPDFs(linksDesejados);

            List<String> fileNames = linksDesejados.stream()
                    .map(url -> url.substring(url.lastIndexOf("/") + 1))
                    .collect(Collectors.toList());

            String zipPath = zipService.compactarPDFs(fileNames);

            Path zipFilePath = Paths.get(zipPath).normalize();
            Resource resource = new UrlResource(zipFilePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFilePath.getFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/listar")
    public List<String> listarPDFs() {
        try {
            return scraperService.obterLinksPDFs();
        } catch (IOException e) {
            return List.of("Erro ao buscar os PDFs: " + e.getMessage());
        }
    }


    @GetMapping("/transformar")
    public ResponseEntity<Resource> transformarDados() {
        try {
            pdfExtractionService.extractAndProcessPdf();
            Path zipFilePath = Paths.get("downloads/Teste_Nicolas.zip");
            Resource resource = new UrlResource(zipFilePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFilePath.getFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


}