package com.example.demo.services;

import com.example.demo.util.ByteArrayMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Genera documentos Office EN BLANCO (Word/Excel) válidos para "Crear documento".
 * Cada documento es ÚNICO (lleva un UUID embebido) para que NO choque con el
 * control de hash duplicado del repositorio (dos blanks idénticos colisionarían).
 * El nombre se usa como texto/título inicial.
 */
@Component
public class BlankOfficeTemplates {

    private static final String XMLH = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";

    public MultipartFile plantilla(String tipo, String nombreLogico) {
        String t = tipo == null ? "" : tipo.toLowerCase();
        String uid = UUID.randomUUID().toString();
        if (t.equals("docx") || t.equals("word")) {
            return new ByteArrayMultipartFile("archivo", conExt(nombreLogico, "docx"),
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    construirDocx(nombreLogico, uid));
        }
        if (t.equals("xlsx") || t.equals("excel")) {
            return new ByteArrayMultipartFile("archivo", conExt(nombreLogico, "xlsx"),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    construirXlsx(uid));
        }
        throw new IllegalArgumentException("Tipo no soportado: " + tipo + " (usa 'docx' o 'xlsx').");
    }

    public String tipoCatalogo(String tipo) {
        String t = tipo == null ? "" : tipo.toLowerCase();
        return (t.equals("docx") || t.equals("word")) ? "WORD" : "EXCEL";
    }

    // ── Generación OOXML mínima válida ───────────────────────────────────────

    private static final String NS_MAIN_W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
    private static final String NS_MAIN_X = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private static final String NS_REL = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
    private static final String NS_PKG_REL = "http://schemas.openxmlformats.org/package/2006/relationships";
    private static final String CT = "http://schemas.openxmlformats.org/package/2006/content-types";

    private byte[] construirDocx(String nombre, String uid) {
        String texto = (nombre == null || nombre.isBlank()) ? "Documento nuevo" : nombre;
        Map<String, String> parts = new LinkedHashMap<>();
        parts.put("[Content_Types].xml", XMLH
                + "<Types xmlns=\"" + CT + "\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
                + "<Override PartName=\"/word/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml\"/>"
                + "</Types>");
        parts.put("_rels/.rels", XMLH
                + "<Relationships xmlns=\"" + NS_PKG_REL + "\">"
                + "<Relationship Id=\"rId1\" Type=\"" + NS_REL + "/officeDocument\" Target=\"word/document.xml\"/>"
                + "</Relationships>");
        parts.put("word/_rels/document.xml.rels", XMLH
                + "<Relationships xmlns=\"" + NS_PKG_REL + "\">"
                + "<Relationship Id=\"rId1\" Type=\"" + NS_REL + "/styles\" Target=\"styles.xml\"/>"
                + "</Relationships>");
        parts.put("word/styles.xml", XMLH
                + "<w:styles xmlns:w=\"" + NS_MAIN_W + "\">"
                + "<w:docDefaults><w:rPrDefault><w:rPr>"
                + "<w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\" w:cs=\"Calibri\"/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/>"
                + "</w:rPr></w:rPrDefault><w:pPrDefault/></w:docDefaults>"
                + "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/><w:qFormat/></w:style>"
                + "</w:styles>");
        parts.put("word/document.xml", XMLH
                + "<w:document xmlns:w=\"" + NS_MAIN_W + "\">"
                + "<w:body><!--uid:" + uid + "-->"
                + "<w:p><w:r><w:t xml:space=\"preserve\">" + escape(texto) + "</w:t></w:r></w:p>"
                + "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
                + "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>"
                + "</w:body></w:document>");
        return zip(parts);
    }

    private byte[] construirXlsx(String uid) {
        Map<String, String> parts = new LinkedHashMap<>();
        parts.put("[Content_Types].xml", XMLH
                + "<Types xmlns=\"" + CT + "\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                + "</Types>");
        parts.put("_rels/.rels", XMLH
                + "<Relationships xmlns=\"" + NS_PKG_REL + "\">"
                + "<Relationship Id=\"rId1\" Type=\"" + NS_REL + "/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>");
        parts.put("xl/workbook.xml", XMLH
                + "<workbook xmlns=\"" + NS_MAIN_X + "\" xmlns:r=\"" + NS_REL + "\">"
                + "<sheets><sheet name=\"Hoja1\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
        parts.put("xl/_rels/workbook.xml.rels", XMLH
                + "<Relationships xmlns=\"" + NS_PKG_REL + "\">"
                + "<Relationship Id=\"rId1\" Type=\"" + NS_REL + "/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"" + NS_REL + "/styles\" Target=\"styles.xml\"/>"
                + "</Relationships>");
        parts.put("xl/styles.xml", XMLH
                + "<styleSheet xmlns=\"" + NS_MAIN_X + "\">"
                + "<fonts count=\"1\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
                + "<fills count=\"1\"><fill><patternFill patternType=\"none\"/></fill></fills>"
                + "<borders count=\"1\"><border/></borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/></cellXfs>"
                + "</styleSheet>");
        parts.put("xl/worksheets/sheet1.xml", XMLH
                + "<worksheet xmlns=\"" + NS_MAIN_X + "\">"
                + "<!--uid:" + uid + "--><dimension ref=\"A1\"/><sheetData/></worksheet>");
        return zip(parts);
    }

    private byte[] zip(Map<String, String> parts) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream z = new ZipOutputStream(baos)) {
            for (Map.Entry<String, String> e : parts.entrySet()) {
                z.putNextEntry(new ZipEntry(e.getKey()));
                z.write(e.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                z.closeEntry();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el documento en blanco", ex);
        }
        return baos.toByteArray();
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String conExt(String nombre, String ext) {
        String base = (nombre == null || nombre.isBlank()) ? "documento" : nombre;
        return base.toLowerCase().endsWith("." + ext) ? base : base + "." + ext;
    }
}
