package com.github.metcox.apodeixis.web;

import com.intuit.karate.driver.chrome.Chrome;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@Controller
public class PdfController {

    private static final int VIEWPORT_WIDTH = 1920;
    private static final int VIEWPORT_HEIGHT = 1080;

    private static final float POINTS_PER_INCH = 72;
    private static final float POINTS_PER_MM = POINTS_PER_INCH / 25.4f;
    private static final float IMAGE_WIDTH = 297 * POINTS_PER_MM;
    private static final float IMAGE_HEIGHT = 297 * POINTS_PER_MM * VIEWPORT_HEIGHT / VIEWPORT_WIDTH;


    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf() throws Exception {
        String content = Files.readString(Paths.get("sample/demoit.html"));  // TODO use application args instead of 'sample'
        String[] split = content.split("---");
        int pageCount = split.length;

        Map<String, Object> chromeOptions = new HashMap<>();
        chromeOptions.put("headless", true);
        Chrome chrome = Chrome.start(chromeOptions);
        chrome.emulateDevice(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, "");

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (PDDocument doc = new PDDocument()) {
            for (int i = 0; i < pageCount; i++) {
                chrome.setUrl("http://localhost:8080/" + i); // TODO put in configuration
                // get a chance to load more content (docker, shell, source, ...)
                Thread.sleep(500); // TODO put in configuration
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, chrome.screenshot(), "slide" + i);

                PDPage page = new PDPage(new PDRectangle(IMAGE_WIDTH, IMAGE_HEIGHT));
                doc.addPage(page);
                try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    contentStream.drawImage(pdImage, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
                }
            }
            doc.save(output);
        }
        chrome.quit();

        return ok(output.toByteArray());
    }
}
