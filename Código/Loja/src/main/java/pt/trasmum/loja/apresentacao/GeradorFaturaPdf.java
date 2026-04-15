package pt.trasmum.loja.apresentacao;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import pt.trasmum.loja.app.AppContext;
import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.core.ConfiguracaoTerminal;
import pt.trasmum.loja.dominio.vendas.*;
import pt.trasmum.loja.dominio.vendas.Venda.MetodoPagamento;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gera um PDF formato A5 (recibo/fatura) para uma venda finalizada.
 * Os PDFs são guardados em ./faturas/ relativo ao directório de execução.
 */
public class GeradorFaturaPdf {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color COR_PRIMARIA = new Color(30, 42, 74);   // #1e2a4a
    private static final Color COR_LINHA_ALT = new Color(249, 250, 251);

    private final ConfiguracaoTerminal config;

    public GeradorFaturaPdf(ConfiguracaoTerminal config) {
        this.config = config;
    }

    /**
     * Gera o PDF e devolve o caminho absoluto do ficheiro criado.
     *
     * @param troco valor de troco (< 0 se pagamento não foi em numerário)
     */
    public Path gerar(Fatura fatura, Venda venda, double troco) throws IOException {
        Path dir = Paths.get("faturas");
        Files.createDirectories(dir);
        String nomeFicheiro = fatura.getNumeroFatura().replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".pdf";
        Path ficheiro = dir.resolve(nomeFicheiro);

        Document doc = new Document(PageSize.A5);
        try (FileOutputStream fos = new FileOutputStream(ficheiro.toFile())) {
            PdfWriter.getInstance(doc, fos);
            doc.open();
            escrever(doc, fatura, venda, troco);
            doc.close(); // obrigatório: escreve o cross-reference table e o EOF
        } catch (DocumentException e) {
            throw new IOException("Erro ao gerar PDF", e);
        }
        return ficheiro.toAbsolutePath();
    }

    // ─────────────────────────────────────────────────────────────────

    private void escrever(Document doc, Fatura fatura, Venda venda, double troco)
            throws DocumentException {

        Font fTitulo  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COR_PRIMARIA);
        Font fNegrito = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Font fNormal  = FontFactory.getFont(FontFactory.HELVETICA,      10, Color.BLACK);
        Font fPequeno = FontFactory.getFont(FontFactory.HELVETICA,       8, Color.GRAY);
        Font fBranco  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

        // ── Cabeçalho ──────────────────────────────────────────────
        Paragraph pNomeLoja = new Paragraph(config.getNomeLoja(), fTitulo);
        pNomeLoja.setAlignment(Element.ALIGN_CENTER);
        doc.add(pNomeLoja);

        Paragraph pIdLoja = new Paragraph("ID Loja: " + config.getIdLoja(), fPequeno);
        pIdLoja.setAlignment(Element.ALIGN_CENTER);
        doc.add(pIdLoja);

        doc.add(Chunk.NEWLINE);
        doc.add(separador());
        doc.add(Chunk.NEWLINE);

        // ── Dados da fatura ────────────────────────────────────────
        PdfPTable tblInfo = new PdfPTable(2);
        tblInfo.setWidthPercentage(100);
        tblInfo.setWidths(new float[]{1.3f, 1f});
        tblInfo.setSpacingAfter(10);
        celulaInfo(tblInfo, "Fatura N.º:",  fatura.getNumeroFatura(),              fNegrito, fNormal);
        celulaInfo(tblInfo, "Data:",         fatura.getDataEmissao().format(FMT),   fNegrito, fNormal);
        if (fatura.getNifCliente() != null && !fatura.getNifCliente().isBlank()) {
            celulaInfo(tblInfo, "NIF Cliente:", fatura.getNifCliente(), fNegrito, fNormal);
        }
        doc.add(tblInfo);

        // ── Tabela de artigos ──────────────────────────────────────
        PdfPTable tblArtigos = new PdfPTable(4);
        tblArtigos.setWidthPercentage(100);
        tblArtigos.setWidths(new float[]{4f, 1f, 1.6f, 1.6f});
        tblArtigos.setSpacingAfter(6);

        for (String cab : new String[]{"Artigo", "Qtd.", "P. Unit.", "Total"}) {
            PdfPCell c = new PdfPCell(new Phrase(cab, fBranco));
            c.setBackgroundColor(COR_PRIMARIA);
            c.setPadding(5);
            c.setBorder(Rectangle.NO_BORDER);
            tblArtigos.addCell(c);
        }

        // Agrega por (produto, preço unitário) — igual à lógica do controlador.
        // Linhas do mesmo produto a preços diferentes (lote em desconto vs. normal)
        // ficam em linhas separadas; linhas com o mesmo preço colapsam.
        record Chave(String nome, double preco) {}
        Map<Chave, double[]> mapa = new LinkedHashMap<>(); // [qtd, subtotal]
        for (LinhaVenda l : venda.getLinhas()) {
            String nome  = resolverNomeProduto(l.getIdLote());
            double preco = round2(l.getPrecoUnitario());
            mapa.compute(new Chave(nome, preco), (k, v) -> {
                if (v == null) return new double[]{l.getQuantidade(), round2(l.calcularSubtotal())};
                v[0] += l.getQuantidade();
                v[1]  = round2(v[1] + round2(l.calcularSubtotal()));
                return v;
            });
        }

        boolean par = false;
        for (Map.Entry<Chave, double[]> e : mapa.entrySet()) {
            Color bg = par ? COR_LINHA_ALT : Color.WHITE;
            double[] v = e.getValue();
            celulaArtigo(tblArtigos, e.getKey().nome,                          Element.ALIGN_LEFT,   fNormal, bg);
            celulaArtigo(tblArtigos, String.valueOf((int) v[0]),                Element.ALIGN_CENTER, fNormal, bg);
            celulaArtigo(tblArtigos, String.format("%.2f €", e.getKey().preco), Element.ALIGN_RIGHT,  fNormal, bg);
            celulaArtigo(tblArtigos, String.format("%.2f €", v[1]),             Element.ALIGN_RIGHT,  fNormal, bg);
            par = !par;
        }
        doc.add(tblArtigos);

        // ── Totais e pagamento ─────────────────────────────────────
        double total = round2(mapa.values().stream().mapToDouble(v -> v[1]).sum());

        PdfPTable tblTotais = new PdfPTable(2);
        tblTotais.setWidthPercentage(50);
        tblTotais.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tblTotais.setWidths(new float[]{1.6f, 1f});
        tblTotais.setSpacingAfter(10);

        celulaTotais(tblTotais, "TOTAL:",      String.format("%.2f €", total), fNegrito);
        celulaTotais(tblTotais, "Pagamento:",
                venda.getMetodoPagamento() == MetodoPagamento.MULTIBANCO ? "Multibanco" : "Numerário",
                fNormal);
        if (troco >= 0) {
            celulaTotais(tblTotais, "Troco:", String.format("%.2f €", troco), fNormal);
        }
        doc.add(tblTotais);

        // ── Rodapé ─────────────────────────────────────────────────
        doc.add(separador());
        Paragraph rodape = new Paragraph("Obrigado pela sua compra!", fPequeno);
        rodape.setAlignment(Element.ALIGN_CENTER);
        rodape.setSpacingBefore(4);
        doc.add(rodape);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private String resolverNomeProduto(int idLote) {
        Lote lote = AppContext.getInstance().loteRepo.buscarPorId(idLote);
        if (lote == null) return "Artigo";
        Produto p = AppContext.getInstance().produtoRepo.buscarPorId(lote.getIdProduto());
        return p != null ? p.getNome() : "Artigo";
    }

    private Chunk separador() {
        return new Chunk(new LineSeparator(1f, 100f, COR_PRIMARIA, Element.ALIGN_CENTER, -2f));
    }

    private void celulaInfo(PdfPTable tbl, String label, String valor, Font fLabel, Font fValor) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, fLabel));
        c1.setBorder(Rectangle.NO_BORDER); c1.setPadding(2);
        tbl.addCell(c1);
        PdfPCell c2 = new PdfPCell(new Phrase(valor, fValor));
        c2.setBorder(Rectangle.NO_BORDER); c2.setPadding(2);
        tbl.addCell(c2);
    }

    private void celulaArtigo(PdfPTable tbl, String texto, int alinhamento, Font font, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(texto, font));
        c.setHorizontalAlignment(alinhamento);
        c.setBackgroundColor(bg);
        c.setPadding(4);
        c.setBorderColor(new Color(229, 231, 235));
        c.setBorderWidth(0.5f);
        tbl.addCell(c);
    }

    private void celulaTotais(PdfPTable tbl, String label, String valor, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font));
        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c1.setBorder(Rectangle.NO_BORDER); c1.setPadding(2);
        tbl.addCell(c1);
        PdfPCell c2 = new PdfPCell(new Phrase(valor, font));
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setBorder(Rectangle.NO_BORDER); c2.setPadding(2);
        tbl.addCell(c2);
    }
}
