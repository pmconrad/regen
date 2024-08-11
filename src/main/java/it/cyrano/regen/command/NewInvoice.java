package it.cyrano.regen.command;

import it.cyrano.regen.MyVersionProvider;
import picocli.CommandLine;

@CommandLine.Command(name = "invoice", description = "Generate a new invoice",
                     mixinStandardHelpOptions = true, versionProvider = MyVersionProvider.class)
public class NewInvoice {
}
