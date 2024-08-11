package it.cyrano.regen;

import it.cyrano.regen.command.NewInvoice;
import picocli.CommandLine;

@CommandLine.Command(name = "regen",
                     description = "CLI tool for generating machine- and human-readable documents conforming to the ZugFERD standard",
                     mixinStandardHelpOptions = true, versionProvider = MyVersionProvider.class,
                     subcommands = {
                         NewInvoice.class
                     })
public class Main {
    public static void main(String[] commandlineParameters) {
        System.exit(new CommandLine(new Main()).execute(commandlineParameters));
    }
}
