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

@Getter
@Setter
public class Contact {
    private String name;
    private String street;
    private String country;
    private String ZIP;
    private String location;
    private String eMail;
    private String phone;
    private String fax;

    public org.mustangproject.Contact get() {
        var contact = new org.mustangproject.Contact();
        contact.setName(name);
        contact.setStreet(street);
        contact.setCountry(country);
        contact.setZIP(ZIP);
        contact.setLocation(location);
        contact.setEMail(eMail);
        contact.setPhone(phone);
        contact.setFax(fax);
        return contact;
    }
}
