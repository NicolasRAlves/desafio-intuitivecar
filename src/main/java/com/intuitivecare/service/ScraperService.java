package com.intuitivecare.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScraperService {

    private static final String URL = "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos";

    public List<String> obterLinksPDFs() throws IOException {
        Document doc = Jsoup.connect(URL).get();
        List<String> pdfLinks = new ArrayList<>();

        for (Element link : doc.select("a[href$=.pdf]")) {
            String href = link.absUrl("href");


            if (href.contains("Anexo_I") || href.contains("Anexo_II")) {
                pdfLinks.add(href);
            }
        }

        return pdfLinks;
    }

}
