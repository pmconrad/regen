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
package it.cyrano.regen.data;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Product {
    private String name;
    private String description;
    private String unit;
    private String sellerAssignedID;
    private String buyerAssignedID;
    private SchemeId globalID;
    private BigDecimal VATPercent;
    private boolean reverseCharge;
    private boolean intraCommunitySupply;

    public org.mustangproject.Product get() {
        var prod = new org.mustangproject.Product();
        prod.setName(name);
        prod.setDescription(description);
        prod.setUnit(unit);
        prod.setSellerAssignedID(sellerAssignedID);
        prod.setBuyerAssignedID(buyerAssignedID);
        prod.setVATPercent(VATPercent);
        if (reverseCharge) prod.setReverseCharge();
        if (intraCommunitySupply) prod.setIntraCommunitySupply();
        if (globalID != null) {
            prod.addGlobalID(globalID.get());
        }
        return prod;
    }
}
