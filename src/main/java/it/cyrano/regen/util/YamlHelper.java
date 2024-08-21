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

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.scalar.ScalarSerializer;
import it.cyrano.regen.data.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;

public class YamlHelper {
    private static class BigNumSerializer implements ScalarSerializer<BigDecimal> {
        @Override
        public String write(BigDecimal bigDecimal) {
            return bigDecimal.toPlainString();
        }

        @Override
        public BigDecimal read(String s) {
            return new BigDecimal(s);
        }
    }

    public static <T> T read(String filename, Class<T> what) throws IOException {
        try (var rdr = Files.newBufferedReader(Paths.get(filename))) {
            var reader = new YamlReader(rdr);
            reader.getConfig().setClassTag("BankDetails", BankDetails.class);
            reader.getConfig().setClassTag("Contact", Contact.class);
            reader.getConfig().setClassTag("DebitDetail", DebitDetail.class);
            reader.getConfig().setClassTag("Product", Product.class);
            reader.getConfig().setClassTag("SchemeId", SchemeId.class);
            reader.getConfig().setScalarSerializer(BigDecimal.class, new BigNumSerializer());
            return reader.read(what);
        }
    }
}
