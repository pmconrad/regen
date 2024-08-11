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
