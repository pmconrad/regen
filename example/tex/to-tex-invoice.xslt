<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:rsm="urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100"
                xmlns:ram="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100"
                xmlns:qdt="urn:un:unece:uncefact:data:standard:QualifiedDataType:100"
                xmlns:udt="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100"
                version='1.1'>
    <xsl:output method="text" indent="no" encoding="UTF8"/>

    <xsl:template match="udt:DateTimeString[@format='102']">
        <xsl:value-of select="substring(., 7, 2)"/>.<xsl:value-of select="substring(., 5, 2)"/>.<xsl:value-of select="substring(., 1, 4)"/>
    </xsl:template>

    <xsl:template match="ram:ApplicableTradeTax">
        \multicolumn{3}{r}{zzgl. \numprint{<xsl:value-of select="ram:RateApplicablePercent"/>}\% MWSt
            auf \EUR{\numprint{<xsl:value-of select="ram:BasisAmount"/>}}}
        &amp; \EUR{\numprint{<xsl:value-of select="ram:CalculatedAmount"/>}} \\
    </xsl:template>

    <xsl:template match="ram:IncludedSupplyChainTradeLineItem">
        \numprint{<xsl:value-of select="ram:SpecifiedLineTradeDelivery/ram:BilledQuantity"/>} <xsl:text> </xsl:text>
        <xsl:choose>
            <xsl:when test="ram:SpecifiedLineTradeDelivery/ram:BilledQuantity/@unitCode='H87'">Stk</xsl:when>
            <xsl:when test="ram:SpecifiedLineTradeDelivery/ram:BilledQuantity/@unitCode='HUR'">Std</xsl:when>
        </xsl:choose> &amp;
            <xsl:value-of select="ram:SpecifiedTradeProduct/ram:Name"/> &amp;
            \EUR{\numprint{<xsl:value-of select="ram:SpecifiedLineTradeAgreement/ram:NetPriceProductTradePrice/ram:ChargeAmount"/>}} &amp;
            \EUR{\numprint{<xsl:value-of select="ram:SpecifiedLineTradeSettlement/ram:SpecifiedTradeSettlementLineMonetarySummation/ram:LineTotalAmount"/>}} \\
    </xsl:template>

    <xsl:template match="/rsm:CrossIndustryInvoice">
%\let\pdfminorversion=7
\let\pdfcreationdate=\creationdate
\begin{filecontents*}[overwrite]{\jobname.xmpdata}
\Title{Rechnung <xsl:apply-templates select="rsm:ExchangedDocument/ram:ID"/>}
\Author{<xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:DefinedTradeContact/ram:DepartmentName"/>}
\Publisher{<xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:Name"/>}
\Language{de-DE}
\end{filecontents*}
\documentclass[parskip=half]{scrlttr2}
%Spracheinstellungen
\usepackage{polyglossia}
\setdefaultlanguage[spelling = new]{german}
\usepackage{xltxtra}   
% The xltxtra-package automatically loads the following packages: 
% fixltx2e, metalogo, xunicode, fontspec

% zum Rechnen in Tabellen
%\usepackage{fp, xstring, spreadtab, numprint}
\usepackage{fp, xstring, numprint}

%Um das Euro-Symbol typografisch korrekt auszugeben und es rechts vom Betrag
% zu stellen, wird das Paket eurosym mit der Option [right] geladen

\usepackage[right]{eurosym}

\usepackage[bottom]{footmisc}

\usepackage[a-3u]{pdfx}

% Jetzt werden die Variablen definiert, die durch den Befehl \usekomavar expandiert
% werden können. Aber innerhalb der Umgebung spreadtab funktioniert das in der 
% Tabelle für Berechnungen nicht. Dank der dante-Mailingliste muss einfach nur 
% eine weitere % Variable angelegt werden, in der das Token bereits expandiert 
% gespeichert werden kann.
\setkomavar{date}{<xsl:apply-templates select="rsm:ExchangedDocument/ram:IssueDateTime/udt:DateTimeString"/>}

\setkomavar{fromname}{<xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:Name"/>}
\setkomavar{fromaddress}{<xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:PostalTradeAddress/ram:LineOne"/>\\
        <xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:PostalTradeAddress/ram:PostcodeCode"/><xsl:text> </xsl:text><xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:PostalTradeAddress/ram:CityName"/>}
\setkomavar{fromphone}[\Phone~]{+49\,(0)\,6102\,821\,206}
%\setkomavar{fromfax}[\FAX~]{+49\,(0)\,123\,456\,789\,1}
\setkomavar{fromemail}[\Letter~]{cyrano@quisquis.de}
\setkomavar{backaddressseparator}{ - }

\setkomavar{invoice}{<xsl:apply-templates select="rsm:ExchangedDocument/ram:ID"/>}

% Brieffuss festlegen mit \firtstfoot, Standardwert ist kein Fuß
\setkomavar{firstfoot}{ 
    \parbox[b][5mm]{\linewidth}{
        
    \footnotesize
    \begin{tabbing}
aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \= 
aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \= 
aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \kill
<xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:Name"/> \> Adresse: \> Kontakt: \\
Geschäftsführer: Willi Wunder \>
        <xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:PostalTradeAddress/ram:LineOne"/> \>
        <xsl:choose>
            <xsl:when test="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:URIUniversalCommunication/ram:URIID[@schemeID='EM']">
                <xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:URIUniversalCommunication/ram:URIID[@schemeID='EM']"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:DefinedTradeContact/ram:EmailURIUniversalCommunication/ram:URIID"/>
            </xsl:otherwise>
        </xsl:choose> \\
AG Hintertupfingen, HRX 57535 \>
        <xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:PostalTradeAddress/ram:PostcodeCode"/>
            <xsl:text> </xsl:text> <xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:PostalTradeAddress/ram:CityName"/> \>
        <xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:DefinedTradeContact/ram:TelephoneUniversalCommunication/ram:CompleteNumber"/>\\
USt-ID: <xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:SpecifiedTaxRegistration/ram:ID[@schemeID='VA']"/> \>
        Germany \>
        <xsl:choose>
            <xsl:when test="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans/ram:TypeCode = '58'">
                IBAN <xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans[ram:TypeCode = '58']/ram:PayeePartyCreditorFinancialAccount/ram:IBANID"/>
            </xsl:when>
        </xsl:choose> \\
    \end{tabbing}
    }
}

%Nun der eigentliche Brief - die Rechnung:

\begin{document}
\begin{letter}{<xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:Name"/>\\
<xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:PostalTradeAddress/ram:LineOne"/>\\[1\baselineskip]
<xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:PostalTradeAddress/ram:PostcodeCode"/><xsl:text> </xsl:text>
<xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:PostalTradeAddress/ram:CityName"/>}
\opening{Sehr geehrte Damen und Herren,}
unsere Leistungen <xsl:choose>
    <xsl:when test="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:BillingSpecifiedPeriod/ram:StartDateTime">
        im Zeitraum <xsl:apply-templates select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:BillingSpecifiedPeriod/ram:StartDateTime/udt:DateTimeString"/>
        -- <xsl:apply-templates select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:BillingSpecifiedPeriod/ram:EndDateTime/udt:DateTimeString"/>
    </xsl:when>
    <xsl:when test="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeDelivery/ram:ActualDeliverySupplyChainEvent/ram:OccurrenceDateTime">
        am <xsl:apply-templates select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeDelivery/ram:ActualDeliverySupplyChainEvent/ram:OccurrenceDateTime/udt:DateTimeString"/>
    </xsl:when>
</xsl:choose> stellen wir Ihnen wie folgt in Rechnung:

\npdecimalsign{,} \npthousandsep{.} \nprounddigits{2}

% Hier nun die rechnende Tabelle (Die Option [\STsavecell\gesamtsumme{c3}]) 
% speichert das Endergebnis (Zelle c3 der Tabelle), um das Ergebnis auch außerhalb 
% der Tabelle verwenden zu können):

% Die Zahlwerte werden in :={Variable oder Zahlwert} gesetzt; so kann "spreadtab"
% zwischen Text und Zahlwerten unterscheiden. Bleibt :={} leer, wird das als 
% leerer Zahlwert (0) verstanden, der jedoch nicht ausgegeben wird.
\begin{tabular}{p{20mm}p{70mm}p{25mm}p{30mm}}
Menge &amp; Bezeichnung &amp; Einzelpreis &amp; Gesamt \\
\hline
<xsl:apply-templates select="rsm:SupplyChainTradeTransaction/ram:IncludedSupplyChainTradeLineItem"/>
\hline
\multicolumn{3}{r}{Summe} &amp; \EUR{\numprint{<xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementHeaderMonetarySummation/ram:LineTotalAmount"/>}} \\
<xsl:apply-templates select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:ApplicableTradeTax"/>
\hline
\multicolumn{3}{r}{Gesamt} &amp; \EUR{\numprint{<xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementHeaderMonetarySummation/ram:GrandTotalAmount"/>}} \\
\end{tabular}

<!--<xsl:choose>
    <xsl:when test="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradePaymentTerms/ram:Description">
        <xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradePaymentTerms/ram:Description"/>
    </xsl:when>
    <xsl:otherwise>-->
        <xsl:choose>
            <xsl:when test="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans/ram:TypeCode = '59'">
Der Gesamtbetrag von \EUR{\numprint{<xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementHeaderMonetarySummation/ram:DuePayableAmount"/>}}
wird mit der Mandatsreferenz <xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradePaymentTerms/ram:DirectDebitMandateID"/>
von Ihrem Konto <xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans[ram:TypeCode = '59']/ram:PayerPartyDebtorFinancialAccount/ram:IBANID"/> abgebucht.
            </xsl:when>
            <xsl:otherwise>
Bitte überweisen Sie den Gesamtbetrag von \EUR{\numprint{<xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementHeaderMonetarySummation/ram:DuePayableAmount"/>}} %
unter Angabe der Rechnungsnummer \usekomavar{invoice} auf unser Bankkonto:

IBAN <xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans[ram:TypeCode = '58']/ram:PayeePartyCreditorFinancialAccount/ram:IBANID"/> \\
BIC <xsl:value-of select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans[ram:TypeCode = '58']/ram:PayeeSpecifiedCreditorFinancialInstitution/ram:BICID"/>

Zahlbar bis zum <xsl:apply-templates select="rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradePaymentTerms/ram:DueDateDateTime/udt:DateTimeString"/> ohne Abzug.
            </xsl:otherwise>
        </xsl:choose>
    <!--</xsl:otherwise>
</xsl:choose>-->

\closing{Mit freundlichen Grüßen,}
\end{letter}
\end{document}
    </xsl:template>
</xsl:stylesheet>
