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

import java.util.List;

@Getter
@Setter
public class TradeParty {
    private String ID;
    private SchemeId globalID;
    private SchemeId legalOrganisation;
    private String name;
    private Contact contact;
    private String eMail;
    private String street;
    private String additionalAddress;
    private String additionalAddressExtension;
    private String country;
    private String ZIP;
    private String location;
    private String taxID;
    private String VATID;
    private List<BankDetails> banks;
    private List<DebitDetail> debitDetails;
    private SchemeId uriUniversalCommunication;

    public org.mustangproject.TradeParty get() {
        var party = new org.mustangproject.TradeParty();
        party.setID(ID);
        if (globalID != null) { party.addGlobalID(globalID.get()); }
        if (legalOrganisation != null) { party.setLegalOrganisation(legalOrganisation.getOrg()); }
        party.setName(name);
        if (contact != null) { party.setContact(contact.get()); }
        party.setEmail(eMail);
        party.setStreet(street);
        party.setAdditionalAddress(additionalAddress);
        party.setAdditionalAddressExtension(additionalAddressExtension);
        party.setCountry(country);
        party.setZIP(ZIP);
        party.setLocation(location);
        if (taxID != null) { party.addTaxID(taxID); }
        if (VATID != null) { party.addVATID(VATID); }
        if (uriUniversalCommunication != null) { party.addUriUniversalCommunicationID(uriUniversalCommunication.get()); }
        if (banks != null) { banks.stream().map(BankDetails::get).forEach(party::addBankDetails); }
        if (debitDetails != null) { debitDetails.forEach(party::addDebitDetails); }
        return party;
    }
}
