/* (C) 2024 Peter Conrad <conrad@quisquis.de>
 *
 * This file is part of ReGen.
 *
 * ReGen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ReGen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cyrano.regen.command;

import it.cyrano.regen.MyVersionProvider;
import it.cyrano.regen.data.Product;
import it.cyrano.regen.data.TradeParty;
import it.cyrano.regen.util.YamlHelper;
import lombok.extern.slf4j.Slf4j;
import org.mustangproject.FileAttachment;
import org.mustangproject.Invoice;
import org.mustangproject.Item;
import org.mustangproject.ZUGFeRD.*;
import picocli.CommandLine;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "invoice", description = "Generates a new ZUGfERD invoice '<invoice-number>.pdf'. Refuses to overwrite an existing file of the same name.",
                     mixinStandardHelpOptions = true, versionProvider = MyVersionProvider.class)
@Slf4j
public class NewInvoice implements Callable<Integer> {
    static final SimpleDateFormat YMD = new SimpleDateFormat("yyyy-MM-dd");

    static class Due {
        @CommandLine.Option(names = { "-pu", "--payable-until" }, description = "Due date")
        LocalDate dueDate;

        @CommandLine.Option(names = { "-pw", "--payable-within" }, description = "Due after this many days")
        Integer dueDays;
    }

    static class ItemPrice {
        @CommandLine.Option(names = { "-in", "--net-price" }, description = "Item's net price")
        BigDecimal net;

        @CommandLine.Option(names = { "-ig", "--gross-price" }, description = "Item's gross price")
        BigDecimal gross;
    }

    static class InvoiceItem {
        @CommandLine.Option(names = { "-ip", "--product" }, description = "Item's product ID in database", required = true)
        String product;

        @CommandLine.ArgGroup
        ItemPrice price;

        @CommandLine.Option(names = { "-iq", "--quantity" }, description = "Item's quantity", required = true)
        BigDecimal quantity;
    }

    static class Attachment {
        @CommandLine.Option(names = { "-a", "--attach" }, description = "Attachment filename", required = true)
        String filename;

        @CommandLine.Option(names = { "-at", "--attachment-type" }, description = "Attachment mime type")
        String mimeType;

        @CommandLine.Option(names = { "-ar", "--attachment-relation" }, defaultValue = "Alternative",
                            description = "Attachment relation")
        String relation;
    }

    @CommandLine.Option(names = { "-C", "--currency" }, defaultValue = "EUR",
                        description = "Currency for all amounts")
    String currency;

    @CommandLine.Option(names = { "-R", "--reference-number" }, required = true,
                        description = "Reference number (aka Leitweg-ID)")
    String reference;

    @CommandLine.Option(names = { "-N", "--invoice-number" },
                        description = "Invoice number, defaults to invoice date in YYYY-MM-DD format")
    String number;

    @CommandLine.Option(names = { "-P", "--products" }, required = true,
                        description = "Filename of products database (in yaml format)")
    String productsFile;

    @CommandLine.Option(names = { "-D", "--invoice-date" },
                        description = "Invoice date, default: today")
    LocalDate invoiceDate;

    @CommandLine.ArgGroup(multiplicity = "1")
    Due dueDate;

    @CommandLine.Option(names = { "-dd", "--delivery-date" }, required = true,
                        description = "Delivery date, can be 'YYYY-MM-DD', 'YYYY-MM', 'YYYY' or a period with start and end dates 'YYYY-MM-DD...YYYY-MM-DD'")
    String delivered;

    @CommandLine.Option(names = { "-s", "--sender" }, required = true,
                        description = "Filename containing sender data in YAML format")
    String sender;

    @CommandLine.Option(names = { "-r", "--recipient" }, required = true,
                        description = "Filename containing recipient data in YAML format")
    String recipient;

    @CommandLine.ArgGroup(multiplicity = "1..", exclusive = false)
    List<InvoiceItem> items;

    @CommandLine.ArgGroup(multiplicity = "0..", exclusive = false)
    List<Attachment> attachments;

    @CommandLine.Option(names = { "-X", "--xsl" },
                        description = "Filename containing stylesheet to preprocess the generated XML before calling the external PDF generator program")
    String stylesheet;

    @CommandLine.Option(names = { "-G", "--generator" }, required = true,
                        description = "Path to a program that reads (preprocessed) input on stdin and writes a PDF invoice to stdout")
    String generator;

    @Override
    public Integer call() throws IOException, ParseException, TransformerException {
        var products = loadProducts();
        var invoice = generateInvoice(products);
        var filename = generateZugferdPdf(invoice);
        System.out.println("Generated " + filename);
        return 0;
    }

    Map<String, Product> loadProducts() throws IOException {
        Map<String, Product> entries = new HashMap<>();
        YamlHelper.read(productsFile, HashMap.class)
                  .forEach((k,v) -> {
                      if (v instanceof Product p && k instanceof String s) {
                          entries.put(s, p);
                      } else {
                          log.warn("Ignoring wrong type for product {}", k);
                      }
                });
        return entries;
    }

    Invoice generateInvoice(Map<String, Product> products) throws IOException, ParseException {
        var invoice = new Invoice();
        invoice.setCurrency(currency);
        if (invoiceDate == null) { invoiceDate = LocalDate.now(); }
        invoice.setIssueDate(toDate(invoiceDate));
        addDeliveryDate(invoice);
        if (dueDate.dueDate != null) {
            invoice.setDueDate(toDate(dueDate.dueDate));
        } else {
            Calendar cal = new GregorianCalendar();
            cal.setTime(toDate(invoiceDate));
            cal.add(Calendar.DAY_OF_MONTH, dueDate.dueDays);
            invoice.setDueDate(cal.getTime());
        }
        invoice.setNumber(number != null ? number : generateInvoiceNumber());
        invoice.setSender(YamlHelper.read(sender, TradeParty.class).get());
        invoice.setRecipient(YamlHelper.read(recipient, TradeParty.class).get());
        invoice.setReferenceNumber(reference);

        for (var item : items) {
            var product = products.get(item.product);
            if (product == null) {
                throw new IllegalArgumentException("Unknown product ID in item: " + item.product);
            }
            var _item = new Item();
            _item.setProduct(product.get());
            if (item.price != null && item.price.net != null) {
                _item.setPrice(item.price.net);
            } else if (item.price != null && item.price.gross != null) {
                _item.setGrossPrice(item.price.gross);
            } else if (product.getNetPricePerUnit() != null) {
                _item.setPrice(product.getNetPricePerUnit());
            } else if (product.getGrossPricePerUnit() != null) {
                _item.setGrossPrice(product.getGrossPricePerUnit());
            }
            _item.setQuantity(item.quantity);
            invoice.addItem(_item);
        }
        return invoice;
    }

    String generateInvoiceNumber() {
        return YMD.format(toDate(invoiceDate));
    }

    Date toDate(LocalDate date) {
        var time = date.atTime(LocalTime.MIDNIGHT);
        return new Date(time.atZone(TimeZone.getDefault().toZoneId()).toEpochSecond() * 1000);
    }

    void addDeliveryDate(Invoice invoice) throws ParseException {
        Calendar start;
        Calendar end;
        switch (delivered.length()) {
            case 10:
                invoice.setDeliveryDate(YMD.parse(delivered));
                return;
            case 4:
                start = new GregorianCalendar();
                start.setTime(YMD.parse(delivered + "-01-01"));
                end = new GregorianCalendar();
                end.setTime(start.getTime());
                end.add(Calendar.YEAR, 1);
                break;
            case 7:
                start = new GregorianCalendar();
                start.setTime(YMD.parse(delivered + "-01"));
                end = new GregorianCalendar();
                end.setTime(start.getTime());
                end.add(Calendar.MONTH, 1);
                break;
            case 23:
                start = new GregorianCalendar();
                start.setTime(YMD.parse(delivered.substring(0, 10)));
                end = new GregorianCalendar();
                end.setTime(YMD.parse(delivered.substring(13)));
                end.add(Calendar.DAY_OF_MONTH, 1);
                break;
            default:
                throw new IllegalArgumentException("Bad delivery date");
        }
        end.add(Calendar.MILLISECOND, -1);
        invoice.setDetailedDeliveryPeriod(start.getTime(), end.getTime());
    }

    Path generateZugferdPdf(Invoice invoice) throws IOException, TransformerException {
        var xml = generateXml(invoice);
        if (stylesheet != null) {
            xml = transform(xml);
        }
        var pdf = generatePdf(xml);
        try (var ze = new ZUGFeRDExporterFromA3()) {
            ze.setProfile(Profiles.getByName("XRechnung"));
            ze.load(pdf.toString());
            ze.setProducer("ReGen");
            ze.setCreator(System.getProperty("user.name"));
            if (attachments != null) {
                for (var att : attachments) {
                    ze.attachFile(createAttachment(att));
                }
            }
            ze.setTransaction(invoice);
            Path outFile = Paths.get(invoice.getNumber() + ".pdf");
            Path tmpFile = Files.createTempFile(Paths.get("."), invoice.getNumber(), ".tmp");
            ze.export(tmpFile.toString());
            if (Files.exists(outFile)) {
                throw new IllegalStateException("Won't overwrite existing invoice " + outFile + "!");
            }
            Files.move(tmpFile, outFile, StandardCopyOption.ATOMIC_MOVE);
            return outFile;
        }
    }

    byte[] generateXml(Invoice invoice) {
        ZUGFeRD2PullProvider zf2p = new ZUGFeRD2PullProvider();
        zf2p.setProfile(Profiles.getByName("XRechnung"));
        zf2p.generateXML(invoice);
        return zf2p.getXML();
    }

    Path generatePdf(byte[] invoice) throws IOException {
        var process = Runtime.getRuntime().exec(new String[] { generator });
        var tempPdf = Files.createTempFile("invoice-", ".pdf");
        try {
            process.getOutputStream().write(invoice);
            process.getOutputStream().close();
            byte[] buffer = new byte[1024];
            int read;
            var is = process.getInputStream();
            var es = process.getErrorStream();
            try (var os = Files.newOutputStream(tempPdf)) {
                while (is.available() > 0 || es.available() > 0 || process.isAlive()) {
                    if (is.available() > 0) {
                        read = is.read(buffer, 0, Math.min(buffer.length, is.available()));
                        os.write(buffer, 0, read);
                    } else if (es.available() > 0) {
                        read = es.read(buffer, 0, Math.min(buffer.length, es.available()));
                        System.err.write(buffer, 0, read);
                    } else {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ignore) {}
                    }
                }
            }
            if (process.exitValue() != 0) {
                throw new IllegalStateException("Generator process failed with error code " + process.exitValue());
            }
            return tempPdf;
        } catch (RuntimeException | IOException e) {
            Files.deleteIfExists(tempPdf);
            throw e;
        } finally {
            process.destroy();
        }
    }

    byte[] transform(byte[] xml) throws TransformerException, IOException {
        var xslt = new StreamSource(new File(stylesheet));
        var transformer = TransformerFactory.newInstance().newTransformer(xslt);
        try (var os = new ByteArrayOutputStream()) {
            var result = new StreamResult(os);
            transformer.transform(new StreamSource(new ByteArrayInputStream(xml)), result);
            return os.toByteArray();
        }
    }

    FileAttachment createAttachment(Attachment attachment) throws IOException {
        var att = new FileAttachment(attachment.filename,
                                     attachment.mimeType != null ? attachment.mimeType : detectMimeType(attachment.filename),
                                     attachment.relation, null);
        var path = Paths.get(attachment.filename);
        var size = Files.size(path);
        if (size > Integer.MAX_VALUE) { throw new IllegalArgumentException("Can't attach file " + path + " - too large"); }
        try (var is = Files.newInputStream(path)) {
            var bytes = new byte[(int) size];
            var read = is.read(bytes);
            if (read != bytes.length) {
                throw new IllegalStateException("Incomplete read of " + path);
            }
            att.setData(bytes);
        }
        return att;
    }

    String detectMimeType(String filename) {
        String lc = filename.toLowerCase();
        if (lc.endsWith(".pdf")) { return "application/pdf"; }
        if (lc.endsWith(".txt")) { return "text/plain"; }
        if (lc.endsWith(".html")) { return "text/html"; }
        if (lc.endsWith(".xml")) { return "application/xml"; }
        if (lc.endsWith(".json")) { return "application/json"; }
        log.warn("Failed to determine MIME type of {}", filename);
        return "application/octet-stream";
    }
}
