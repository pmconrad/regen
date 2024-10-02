# ReGen - Rechnungs-Generator

This is a simple program for generating ZUGfERD-compatible machine-readable invoices.

It is not meant to be a one-size-fits-all solution, but only to fit my own needs (or rather the needs of typical European bureaucracy imposed on me).

You will almost certainly have to modify things in order to make it fit your needs, should you decide to use it. Note that this software is licensed under the [GPL version 3](https://www.gnu.org/licenses/gpl-3.0), and section 16 in particular applies. 

## General Idea

[mustang](https://mustangproject.org/) is a Java library for creating ZUGfERD invoices. It can be used to augment an existing PDF invoice with the proper XML structure to produce a machine-readable as well as human-readable invoice. 

What mustang is lacking though is the ability to create the PDF part of the invoice. The PDF must created by some other means. Of course, this creates the danger that the PDF invoice content does not match the embedded XML content.  

ReGen attempts to overcome this danger by generating both a PDF and the embedded XML from the same command line input:

1. ReGen takes command line input and uses that to fill mustang's API datastructures with the invoice data.
2. ReGen uses mustang to generate a preliminary XML representation of the invoice data.
3. In a first (optional) step, the invoice XML is fed through an XSLT stylesheet to produce an intermediate representation (e.g. `TeX` source).
4. ReGen invokes an external program that takes the intermediate representation (or the XML itself) and generates PDF from it (e.g. `xelatex`).
5. ReGen invokes mustang with the generated PDF and the datastructures from step 1 to create the final output.

## Usage

The pieces of input data that are likely to be used in many invoices (i.e. seller, buyer, product information) must be put into `.yaml` files. See below for the supported fields.

If want you use the included `to-tex-invoice.xslt` stylesheet, note that it contains some hardcoded information that cannot be scraped from the invoice data. You should replace that, obviously.

It is important that you don't use any characters in your invoice data that might interfere with `TeX`'s operation. In particular, don't use '%', '{', '}' or '\'.

### Command line tool
```shell
$ java -jar target/regen-0.1.0-SNAPSHOT-shaded.jar invoice
Missing required options: '--products=<productsFile>', '--delivery-date=<delivered>', '--sender=<sender>', '--recipient=<recipient>', '--generator=<generator>'
Usage: regen invoice [-hV] [-C=<currency>] [-D=<invoiceDate>] -dd=<delivered>
                     -G=<generator> [-N=<number>] -P=<productsFile>
                     -r=<recipient> [-R=<reference>] -s=<sender>
                     [-X=<stylesheet>] (-pu=<dueDate> | -pw=<dueDays>)
                     (-ip=<product> -iq=<quantity> (-in=<net> |
                     -ig=<gross>))... [-a=<filename> [-at=<mimeType>]
                     [-ar=<relation>]]...
Generates a new ZUGfERD invoice '<invoice-number>.pdf'. Refuses to overwrite an existing file of the same name.
  -a, --attach=<filename>   Attachment filename
      -ar, --attachment-relation=<relation>
                            Attachment relation
      -at, --attachment-type=<mimeType>
                            Attachment mime type
  -C, --currency=<currency> Currency for all amounts
  -D, --invoice-date=<invoiceDate>
                            Invoice date, default: today
      -dd, --delivery-date=<delivered>
                            Delivery date, can be 'YYYY-MM-DD', 'YYYY-MM',
                              'YYYY' or a period with start and end dates
                              'YYYY-MM-DD...YYYY-MM-DD'
  -G, --generator=<generator>
                            Path to a program that reads (preprocessed) input
                              on stdin and writes a PDF invoice to stdout
  -h, --help                Show this help message and exit.
      -ig, --gross-price=<gross>
                            Item's gross price
      -in, --net-price=<net>
                            Item's net price
      -ip, --product=<product>
                            Item's product ID in database
      -iq, --quantity=<quantity>
                            Item's quantity
  -N, --invoice-number=<number>
                            Invoice number, defaults to invoice date in
                              YYYY-MM-DD format
  -P, --products=<productsFile>
                            Filename of products database (in yaml format)
      -pu, --payable-until=<dueDate>
                            Due date
      -pw, --payable-within=<dueDays>
                            Due after this many days
  -r, --recipient=<recipient>
                            Filename containing recipient data in YAML format
  -R, --reference-number=<reference>
                            Reference number (aka Leitweg-ID)
  -s, --sender=<sender>     Filename containing sender data in YAML format
  -V, --version             Print version information and exit.
  -X, --xsl=<stylesheet>    Filename containing stylesheet to preprocess the
                              generated XML before calling the external PDF
                              generator program
```

### Buyer and Seller `.yaml` files

```yaml
contact:
  !Contact
  name: Willi Wunder
  street: Dresdner Str. 36
  ZIP: 67663
  location: Kaiserslautern
  country: DE
  eMail: willi@example.com
  fax: +49-6102-555723510
  phone: +49-6102-555723511
ID: ww
globalID:
  !SchemeId
  scheme: 1 # see EN16931 code lists values, tab ?
  id: x
legalOrganisation:
  !SchemeId
  scheme: 2 # see EN16931 code lists values, tab ?
  id: y
#uriUniversalCommunication: # mutually exclusive with email
#  !SchemeId
#  scheme: 3 # see EN16931 code lists values, tab ?
#  id: z
name: Wunder Ltd.
eMail: wunder@example.com
street: Bahnhofstr. 18
additionalAddress: Hinterhaus
additionalAddressExtension: bei Milzer klingeln
country: DE
ZIP: 63263
location: Neu-Isenburg
taxID: 123-123-0
VATID: DE1231230
banks:
  - !BankDetails
    IBAN: DE00200400600815471112
    BIC: COBADEFFXXX
    accountName: Wunder GmbH
debitDetails:
  - !DebitDetail
    IBAN: DE00200400600815471112
    mandate: test
```

Most fields are optional. If you want to write an invoice you should be familiar with what's really required.
Note that the seller will usually have only `BankDetails` and the buyer may have `DebitDetail` when payment is done with a SEPA mandate.

### Products '.yaml' file

This contains three products.

```yaml
0815:
  !Product
  description: Softwareentwicklung entspr. Anlage
  buyerAssignedID: buyer-1
  name: Softwareentwicklung
  unit: HUR # see EN16931 code lists values, tab "Unit"
  globalID:
    !SchemeId
    scheme: 1 # see EN16931 code lists values, tab "Item"?
    id: x
  sellerAssignedID: 0815
  reverseCharge: true
  intraCommunitySupply: false
  netPricePerUnit: 11.25
0816:
  !Product
  description: Delikatess-Fleischwurst
  buyerAssignedID: buyer-2
  name: Fleischwurst
  unit: GIP # see EN16931 code lists values, tab "Unit"
  sellerAssignedID: 0816
  VATPercent: 19
  grossPricePerUnit: 12.25
0817:
  !Product
  description: Orangensaft, frisch gepresst
  name: O-Saft
  unit: LTR # see EN16931 code lists values, tab "Unit"
  VATPercent: 7
```
Again, most fields are optional (see last entry).

## Other solutions

* [GNUaccounting](https://www.gnuaccounting.org/) is (was?) supposed to be a full-fleged accounting application, but it hasn't been touched in years and I've [failed](https://github.com/pmconrad/gnuaccounting) to get it running.
* [Fakturama](https://www.fakturama.info/) is a more modern accounting application that uses LibreOffice for generating invoice PDFs. Also didn't work for me.
