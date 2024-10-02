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
package it.cyrano.regen.util;

import it.cyrano.regen.data.Product;
import it.cyrano.regen.data.TradeParty;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class YamlHelperTest {
    @Test
    public void testReadSeller() throws IOException {
        var seller = YamlHelper.read("src/test/resources/seller.yaml", TradeParty.class).get();
        assertEquals("ww", seller.getID());
        assertEquals("1", seller.getGlobalIDScheme());
        assertEquals("x", seller.getGlobalID());
        assertNotNull(seller.getLegalOrganisation());
        assertEquals("2", seller.getLegalOrganisation().getSchemeID());
        assertEquals("y", seller.getLegalOrganisation().getID());
        assertEquals("EM", seller.getUriUniversalCommunicationIDScheme());
        assertEquals("wunder@example.com", seller.getUriUniversalCommunicationID());
        assertEquals("Wunder Ltd.", seller.getName());
        assertEquals("wunder@example.com", seller.getEmail());
        assertEquals("Bahnhofstr. 18", seller.getStreet());
        assertEquals("Hinterhaus", seller.getAdditionalAddress());
        assertEquals("bei Milzer klingeln", seller.getAdditionalAddressExtension());
        assertEquals("DE", seller.getCountry());
        assertEquals("63263", seller.getZIP());
        assertEquals("Neu-Isenburg", seller.getLocation());
        assertEquals("123-123-0", seller.getTaxID());
        assertEquals("DE1231230", seller.getVATID());
        assertEquals("DE1231230", seller.getVatID());
        var contact = seller.getContact();
        assertNotNull(contact);
        assertEquals("Willi Wunder", contact.getName());
        assertEquals("Dresdner Str. 36", contact.getStreet());
        assertEquals("67663", contact.getZIP());
        assertEquals("Kaiserslautern", contact.getLocation());
        assertEquals("DE", contact.getCountry());
        assertEquals("willi@example.com", contact.getEMail());
        assertEquals("+49-6102-555723510", contact.getFax());
        assertEquals("+49-6102-555723511", contact.getPhone());
        var banks = seller.getBankDetails();
        assertNotNull(banks);
        assertEquals(1, banks.size());
        var bank = banks.get(0);
        assertNotNull(bank);
        assertEquals("Wunder GmbH", bank.getAccountName());
        assertEquals("DE00200400600815471112", bank.getIBAN());
        assertEquals("COBADEFFXXX", bank.getBIC());
    }

    @Test
    public void testReadBuyer() throws IOException {
        var buyer = YamlHelper.read("src/test/resources/buyer.yaml", TradeParty.class).get();
        assertEquals("ww", buyer.getID());
        assertEquals("3", buyer.getUriUniversalCommunicationIDScheme());
        assertEquals("z", buyer.getUriUniversalCommunicationID());
        assertEquals("Wunder Ltd.", buyer.getName());
        assertNull(buyer.getEmail());
        assertEquals("Bahnhofstr. 18", buyer.getStreet());
        assertEquals("Hinterhaus", buyer.getAdditionalAddress());
        assertEquals("bei Milzer klingeln", buyer.getAdditionalAddressExtension());
        assertEquals("DE", buyer.getCountry());
        assertEquals("63263", buyer.getZIP());
        assertEquals("Neu-Isenburg", buyer.getLocation());
        assertEquals("123-123-0", buyer.getTaxID());
        assertEquals("DE1231230", buyer.getVATID());
        assertEquals("DE1231230", buyer.getVatID());
        var contact = buyer.getContact();
        assertNotNull(contact);
        assertEquals("Willi Wunder", contact.getName());
        assertEquals("Dresdner Str. 36", contact.getStreet());
        assertEquals("67663", contact.getZIP());
        assertEquals("Kaiserslautern", contact.getLocation());
        assertEquals("DE", contact.getCountry());
        assertEquals("willi@example.com", contact.getEMail());
        assertEquals("+49-6102-555723510", contact.getFax());
        assertEquals("+49-6102-555723511", contact.getPhone());
        var debits = buyer.getAsTradeSettlement();
        assertNotNull(debits);
        assertEquals(1, debits.length);
        // todo: check XML
    }

    @Test
    public void testReadProducts() throws IOException {
        var map = new HashMap<String, Product>();
        var _map = YamlHelper.read("src/test/resources/products.yaml", HashMap.class);
        for (var _e : _map.entrySet()) {
            var e = (Map.Entry) _e;
            assertInstanceOf(String.class, e.getKey());
            assertInstanceOf(Product.class, e.getValue());
            map.put((String) e.getKey(), ((Product) e.getValue()));
        }
        assertEquals(3, map.size());

        var _prod = map.get("0815");
        assertNotNull(_prod);
        var prod = _prod.get();
        assertEquals("buyer-1", prod.getBuyerAssignedID());
        assertEquals("Softwareentwicklung", prod.getName());
        assertEquals("HUR", prod.getUnit());
        assertEquals("x", prod.getGlobalID());
        assertEquals("1", prod.getGlobalIDScheme());
        assertEquals("0815", prod.getSellerAssignedID());
        assertEquals(BigDecimal.valueOf(0), prod.getVATPercent());
        assertEquals("AE", prod.getTaxCategoryCode());
        assertEquals("Reverse Charge", prod.getTaxExemptionReason());
        assertTrue(prod.isReverseCharge());
        assertFalse(prod.isIntraCommunitySupply());
        assertEquals(new BigDecimal("11.25"), _prod.getNetPricePerUnit());
        assertNull(_prod.getGrossPricePerUnit());

        _prod = map.get("0816");
        assertNotNull(_prod);
        prod = _prod.get();
        assertEquals("Delikatess-Fleischwurst", prod.getDescription());
        assertEquals("buyer-2", prod.getBuyerAssignedID());
        assertEquals("Fleischwurst", prod.getName());
        assertEquals("GIP", prod.getUnit());
        assertNull(prod.getGlobalID());
        assertNull(prod.getGlobalIDScheme());
        assertEquals("0816", prod.getSellerAssignedID());
        assertEquals(BigDecimal.valueOf(19), prod.getVATPercent());
        assertEquals("S", prod.getTaxCategoryCode());
        assertFalse(prod.isReverseCharge());
        assertFalse(prod.isIntraCommunitySupply());
        assertNull(_prod.getNetPricePerUnit());
        assertEquals(new BigDecimal("12.25"), _prod.getGrossPricePerUnit());

        _prod = map.get("0817");
        assertNotNull(_prod);
        prod = _prod.get();
        assertEquals("Orangensaft, frisch gepresst", prod.getDescription());
        assertNull(prod.getBuyerAssignedID());
        assertEquals("O-Saft", prod.getName());
        assertEquals("LTR", prod.getUnit());
        assertNull(prod.getGlobalID());
        assertNull(prod.getGlobalIDScheme());
        assertNull(prod.getSellerAssignedID());
        assertEquals(BigDecimal.valueOf(7), prod.getVATPercent());
        assertEquals("S", prod.getTaxCategoryCode());
        assertFalse(prod.isReverseCharge());
        assertFalse(prod.isIntraCommunitySupply());
        assertNull(_prod.getNetPricePerUnit());
        assertNull(_prod.getGrossPricePerUnit());
    }
}
