package com.intuitivecare.service;

import com.opencsv.CSVWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PdfExtractionService {

    private static final String DOWNLOAD_DIR = "downloads/";
    private static final String PDF_FILE = DOWNLOAD_DIR + "Anexo_I_Rol_2021RN_465.2021_RN627L.2024.pdf";
    private static final String CSV_FILE = DOWNLOAD_DIR + "rol_procedimentos.csv";
    private static final String ZIP_FILE = DOWNLOAD_DIR + "Teste_Nicolas.zip";

    public void extractAndProcessPdf() throws IOException {
        List<String[]> tableData = extractTableFromPdf();
        saveToCsv(tableData);
        zipCsv();
    }

    private List<String[]> extractTableFromPdf() throws IOException {
        File pdfFile = new File(PDF_FILE);
        if (!pdfFile.exists()) {
            throw new IOException("Arquivo PDF não encontrado: " + PDF_FILE);
        }

        List<String[]> tableData = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdfFile)) {
            ObjectExtractor oe = new ObjectExtractor(document);
            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();

            String[] headers = {"PROCEDIMENTO", "RN (ALTERAÇÃO)", "VIGÊNCIA", "OD", "AMB", "HCO", "HSO", "REF", "PAC", "DUT", "SUBGRUPO", "GRUPO", "CAPÍTULO"};
            tableData.add(headers);

            int numberOfPages = document.getNumberOfPages();
            for (int pageNum = 3; pageNum <= numberOfPages; pageNum++) { // Começa da página 3
                Page page = oe.extract(pageNum);
                List<Table> tables = sea.extract(page);

                for (Table table : tables) {
                    List<List<RectangularTextContainer>> rows = table.getRows();

                    for (List<RectangularTextContainer> row : rows) {
                        if (row.size() < 13) continue;


                        String procedimento = row.get(12).getText().trim();
                        String rn = row.get(0).getText().trim();
                        String vigencia = row.get(1).getText().trim();
                        String od = row.get(2).getText().trim();
                        String amb = row.get(3).getText().trim();
                        String hco = row.get(4).getText().trim();
                        String hso = row.get(5).getText().trim();
                        String ref = row.get(6).getText().trim();
                        String pac = row.get(7).getText().trim();
                        String dut = row.get(8).getText().trim();
                        String subgrupo = row.get(9).getText().trim();
                        String grupo = row.get(10).getText().trim();
                        String capitulo = row.get(11).getText().trim();

                        od = od.equalsIgnoreCase("OD") ? "Odontológico" : od;
                        amb = amb.equalsIgnoreCase("AMB") ? "Ambulatorial" : amb;

                        tableData.add(new String[]{procedimento, rn, vigencia, od, amb, hco, hso, ref, pac, dut, subgrupo, grupo, capitulo});
                    }
                }
            }

            System.out.println("Linhas extraídas: " + (tableData.size() - 1));
        }
        return tableData;
    }

    private void saveToCsv(List<String[]> tableData) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE, java.nio.charset.StandardCharsets.UTF_8))) {
            writer.writeAll(tableData);
        }
        System.out.println("CSV gerado: " + CSV_FILE);
    }

    private void zipCsv() throws IOException {
        Path csvPath = Paths.get(CSV_FILE);
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(Paths.get(ZIP_FILE)))) {
            ZipEntry zipEntry = new ZipEntry(csvPath.getFileName().toString());
            zipOut.putNextEntry(zipEntry);
            Files.copy(csvPath, zipOut);
            zipOut.closeEntry();
        }
        System.out.println("Arquivo compactado: " + ZIP_FILE);
    }
}
