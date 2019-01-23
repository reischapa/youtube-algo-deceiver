package com.freezinghipster.youtubealgodeceiver;

import picocli.CommandLine;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgsParser {

    public static Map<String, String> parseArgs(String... args) {
        // TODO complete this
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.create();

        spec.mixinStandardHelpOptions(true); // usageHelp and versionHelp options

        spec.addOption(
            CommandLine.Model.OptionSpec.builder("-c", "--count")
                .paramLabel("COUNT")
                .type(int.class)
                .description("number of times to execute").build()
        );

        spec.addPositional(
            CommandLine.Model.PositionalParamSpec.builder()
                .paramLabel("FILES")
                .type(List.class)
                .auxiliaryTypes(File.class) // List<File>
                .description("The files to process").build()
        );

        CommandLine commandLine = new CommandLine(spec);

        CommandLine.IParseResultHandler2<Map<String, String>> handler =
                new Handler().useOut(System.out).andExit(123);

        CommandLine.IExceptionHandler2<Map<String, String>> exceptionHandler =
                new CommandLine.DefaultExceptionHandler<Map<String,String>>().andExit(567);

        return commandLine.parseWithHandlers(handler,exceptionHandler, args);
    }

    // processing parse results can be done manually
    // or delegated to a handler
    private static class Handler extends CommandLine.AbstractParseResultHandler<Map<String, String>> {
        protected Handler self() {
            return this;
        }

        public Map<String, String> handle(CommandLine.ParseResult pr) {
            int count = pr.matchedOptionValue('c', 1);
            List<File> files = pr.matchedPositionalValue(0, Collections.emptyList());
            for (File f : files) {
                for (int i = 0; i < count; i++) {
                    System.out.println(i + " " + f.getName());
                }
            }

            return new HashMap<>();
        }
    }

}
