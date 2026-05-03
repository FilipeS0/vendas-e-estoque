package com.filipe.api.shared.report;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.filipe.api.domain.fiscal.NotaFiscal;
import com.filipe.api.dto.relatorio.*;
import com.filipe.api.dto.venda.VendaResponse;
import com.filipe.api.dto.estoque.EstoqueAtualResponse;
import com.filipe.api.domain.venda.FormaPagamento;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class PdfReportGenerator {

    public byte[] gerarRelatorioFluxoCaixa(FluxoCaixaResponse data, String period) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Relatório de Fluxo de Caixa").setFontSize(18).setBold());
            document.add(new Paragraph("Período: " + period));
            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 3, 3, 3})).useAllAvailableWidth();
            table.addHeaderCell("Data");
            table.addHeaderCell("Entradas");
            table.addHeaderCell("Saídas");
            table.addHeaderCell("Saldo do Dia");

            for (FluxoCaixaResponse.FluxoDiario dia : data.dias()) {
                table.addCell(dia.data().toString());
                table.addCell(String.format("R$ %.2f", dia.entradas()));
                table.addCell(String.format("R$ %.2f", dia.saidas()));
                table.addCell(String.format("R$ %.2f", dia.saldo()));
            }

            document.add(table);
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Total Entradas: R$ " + data.totalEntradas()));
            document.add(new Paragraph("Total Saídas: R$ " + data.totalSaidas()));
            document.add(new Paragraph("Saldo Final: R$ " + data.saldoFinal()).setBold());

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF de Fluxo de Caixa", e);
        }
    }

    public byte[] gerarDanfeNfce(NotaFiscal nota) {
        // Formato para impressora térmica 80mm
        PageSize pageSize = new PageSize(226, 842); // Aproximadamente 80mm de largura
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, pageSize);
            document.setMargins(10, 10, 10, 10);

            document.add(new Paragraph("DOC. AUXILIAR DE NFC-e")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(10));
            
            document.add(new Paragraph("Empresa Exemplo LTDA") // FIXME: Pegar da Configuracao
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8));
            
            document.add(new Paragraph("--------------------------------------------------")
                    .setTextAlignment(TextAlignment.CENTER));

            Table table = new Table(UnitValue.createPercentArray(new float[]{5, 2, 3}))
                    .useAllAvailableWidth();
            
            table.addHeaderCell(new Paragraph("Item").setFontSize(7).setBold());
            table.addHeaderCell(new Paragraph("Qtd").setFontSize(7).setBold());
            table.addHeaderCell(new Paragraph("Vlr").setFontSize(7).setBold());

            nota.getVenda().getItens().forEach(item -> {
                table.addCell(new Paragraph(item.getProduto().getNome()).setFontSize(7));
                table.addCell(new Paragraph(item.getQuantidade().toString()).setFontSize(7));
                table.addCell(new Paragraph(String.format("%.2f", item.getValorTotal())).setFontSize(7));
            });

            document.add(table);

            document.add(new Paragraph("\nTOTAL: R$ " + nota.getVenda().getValorTotal())
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(9));

            document.add(new Paragraph("\nCHAVE DE ACESSO:")
                    .setFontSize(7)
                    .setBold());
            document.add(new Paragraph(nota.getChaveAcesso())
                    .setFontSize(7));

            document.add(new Paragraph("\nProtocolo: " + nota.getProtocolo())
                    .setFontSize(7));
            document.add(new Paragraph("Emissão: " + nota.getDataEmissao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setFontSize(7));

            // QR Code
            String urlQrCode = "https://www.sefaz.gov.br/nfce/qrcode?p=" + nota.getChaveAcesso(); // URL mock
            byte[] qrCodeImg = generateQrCode(urlQrCode, 100, 100);
            ImageData imageData = ImageDataFactory.create(qrCodeImg);
            Image image = new Image(imageData).setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
            document.add(new Paragraph("\n"));
            document.add(image);

            document.add(new Paragraph("\nConsulta via Chave de Acesso em:")
                    .setFontSize(6)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("www.sefaz.gov.br/nfce/consulta")
                    .setFontSize(6)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar DANFE", e);
        }
    }

    public byte[] gerarRelatorioVendas(List<VendaResponse> vendas, String period) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Relatório de Vendas").setFontSize(18).setBold());
            document.add(new Paragraph("Período: " + period));
            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 4, 3, 3})).useAllAvailableWidth();
            table.addHeaderCell("Data/Hora");
            table.addHeaderCell("Cliente");
            table.addHeaderCell("Status");
            table.addHeaderCell("Total");

            BigDecimal totalGeral = BigDecimal.ZERO;

            for (VendaResponse v : vendas) {
                table.addCell(v.dataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                table.addCell(v.cliente() != null ? v.cliente().nome() : "Consumidor");
                table.addCell(v.status().toString());
                table.addCell(String.format("R$ %.2f", v.valorTotal()));
                totalGeral = totalGeral.add(v.valorTotal());
            }

            document.add(table);
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Faturamento Total: R$ " + totalGeral).setBold());

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF de Vendas", e);
        }
    }

    public byte[] gerarRelatorioEstoque(List<EstoqueAtualResponse> estoque) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Relatório de Posição de Estoque").setFontSize(18).setBold());
            document.add(new Paragraph("Gerado em: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPercentArray(new float[]{5, 2, 2, 3})).useAllAvailableWidth();
            table.addHeaderCell("Produto");
            table.addHeaderCell("Qtd Atual");
            table.addHeaderCell("Qtd Mínima");
            table.addHeaderCell("Status");

            for (EstoqueAtualResponse e : estoque) {
                table.addCell(e.produtoNome());
                table.addCell(e.quantidadeAtual().toString());
                table.addCell(e.quantidadeMinima().toString());
                
                String status = e.quantidadeAtual().compareTo(e.quantidadeMinima()) <= 0 ? "BAIXO" : "OK";
                table.addCell(status);
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF de Estoque", e);
        }
    }

    @SuppressWarnings("unchecked")
    public byte[] gerarRelatorioBalancoCaixa(Map<String, Object> data, String period) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Balanço de Caixa").setFontSize(18).setBold());
            document.add(new Paragraph("Período: " + period));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Total Entradas: R$ " + data.get("totalEntradas")));
            document.add(new Paragraph("Total Saídas: R$ " + data.get("totalSaidas")));
            document.add(new Paragraph("Saldo Líquido: R$ " + data.get("saldoLiquido")).setBold());
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Entradas por Forma de Pagamento:").setBold());
            Table table = new Table(UnitValue.createPercentArray(new float[]{5, 5})).useAllAvailableWidth();
            table.addHeaderCell("Forma de Pagamento");
            table.addHeaderCell("Valor");

            Map<FormaPagamento, BigDecimal> totais = (Map<FormaPagamento, BigDecimal>) data.get("totaisPorFormaPagamento");
            for (Map.Entry<FormaPagamento, BigDecimal> entry : totais.entrySet()) {
                table.addCell(entry.getKey().toString());
                table.addCell(String.format("R$ %.2f", entry.getValue()));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF de Balanço de Caixa", e);
        }
    }

    public byte[] gerarRelatorioContasAReceber(ContasAReceberResumoResponse resumo) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Relatório de Contas a Receber (Crediário)").setFontSize(18).setBold());
            document.add(new Paragraph("Gerado em: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Total a Receber: R$ " + resumo.totalAReceber()).setBold());
            document.add(new Paragraph("Total Vencido: R$ " + resumo.totalVencido()).setFontColor(com.itextpdf.kernel.colors.ColorConstants.RED));
            document.add(new Paragraph("Total a Vencer: R$ " + resumo.totalAVencer()));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF de Contas a Receber", e);
        }
    }

    public byte[] gerarRelatorioVendasPorFormaPagamento(List<VendaFormaPagamentoResponse> data, String period) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Vendas por Forma de Pagamento").setFontSize(18).setBold());
            document.add(new Paragraph("Período: " + period));
            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPercentArray(new float[]{5, 2, 3})).useAllAvailableWidth();
            table.addHeaderCell("Forma de Pagamento");
            table.addHeaderCell("Qtd Vendas");
            table.addHeaderCell("Total");

            for (VendaFormaPagamentoResponse item : data) {
                table.addCell(item.formaPagamento());
                table.addCell(item.vendas().toString());
                table.addCell(String.format("R$ %.2f", item.total()));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF de Vendas por Forma de Pagamento", e);
        }
    }

    public byte[] gerarRelatorioRankingProdutos(List<VendaProdutoResponse> data, String period) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Ranking de Produtos Mais Vendidos").setFontSize(18).setBold());
            document.add(new Paragraph("Período: " + period));
            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 5, 2, 2})).useAllAvailableWidth();
            table.addHeaderCell("#");
            table.addHeaderCell("Produto");
            table.addHeaderCell("Quantidade");
            table.addHeaderCell("Total");

            int i = 1;
            for (VendaProdutoResponse item : data) {
                table.addCell(String.valueOf(i++));
                table.addCell(item.produtoNome());
                table.addCell(item.quantidade().toString());
                table.addCell(String.format("R$ %.2f", item.total()));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF de Ranking de Produtos", e);
        }
    }

    private byte[] generateQrCode(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
}
