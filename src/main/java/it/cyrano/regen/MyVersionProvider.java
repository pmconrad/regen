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
package it.cyrano.regen;

import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MyVersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() {
        try (InputStream is = MyVersionProvider.class.getClassLoader().getResourceAsStream("version.properties")) {
            Properties version = new Properties();
            version.load(is);
            return new String[] { "ReGen version " + version.getProperty("version") };
        } catch (IOException | RuntimeException e) {
            return new String[] { "ReGen - failed to load version information" };
        }
    }
}
