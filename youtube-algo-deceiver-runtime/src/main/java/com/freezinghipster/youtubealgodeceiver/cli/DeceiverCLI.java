package com.freezinghipster.youtubealgodeceiver.cli;

import com.freezinghipster.youtubealgodeceiver.Deceiver;
import com.freezinghipster.youtubealgodeceiver.DeceiverOptions;
import com.freezinghipster.youtubealgodeceiver.DeceiverRunnerOptions;
import com.freezinghipster.youtubealgodeceiver.cli.DeceiverOptionsImpl.CLIDeceiverOptionsKeys;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeceiverCLI {

    private CommandLine commandLine;

    private CommandLine.IParseResultHandler2<Map<String,Object>> resultHandler;
    private CommandLine.IExceptionHandler2<Map<String,Object>> exceptionHandler;

    public DeceiverCLI() {
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.create();

        spec.mixinStandardHelpOptions(true);

        spec.addOption(
                CommandLine.Model.OptionSpec.builder("--geckoDriverPath")
                        .paramLabel(CLIDeceiverOptionsKeys.GECKO_DRIVER_PATH.toString())
                        .type(String.class)
                        .description("full path to gecko driver binary")
                        .required(true)
                        .build()
        );

        spec.addOption(
                CommandLine.Model.OptionSpec.builder("--firefoxPath")
                        .paramLabel(CLIDeceiverOptionsKeys.FIREFOX_PATH.toString())
                        .type(String.class)
                        .description("full path to firefox binary")
                        .required(true)
                        .build()
        );

        spec.addOption(
                CommandLine.Model.OptionSpec.builder("--profileFolderPath")
                        .paramLabel(CLIDeceiverOptionsKeys.PROFILE_FOLDER_PATH.toString())
                        .type(String.class)
                        .description("full path to firefox profile path. A copy will be created in /tmp for each instance.")
                        .required(true)
                        .build()
        );


        spec.addOption(
                CommandLine.Model.OptionSpec.builder("-d", "--duration")
                        .paramLabel(CLIDeceiverOptionsKeys.DURATION.toString())
                        .type(Long.class)
                        .description("maximum number of millis that each runner will spend on each video")
                        .defaultValue("60000")
                        .build()
        );

        spec.addOption(
                CommandLine.Model.OptionSpec.builder("-t", "--queueCycleTime")
                        .paramLabel(CLIDeceiverOptionsKeys.QUEUE_CYCLE_TIME.toString())
                        .type(Long.class)
                        .description("number of millis between queue polling")
                        .defaultValue("10000")
                        .build()
        );

        spec.addOption(
                CommandLine.Model.OptionSpec.builder("--runHeadless")
                        .paramLabel(CLIDeceiverOptionsKeys.RUNNING_HEADLESS.toString())
                        .type(Boolean.class)
                        .description("whether instances should be headless")
                        .defaultValue(Boolean.TRUE.toString())
                        .build()
        );

        spec.addOption(
                CommandLine.Model.OptionSpec.builder("-c", "--count")
                        .paramLabel("COUNT")
                        .type(Integer.class)
                        .description("number of simultaneous runners")
                        .defaultValue("1")
                        .build()
        );



        commandLine = new CommandLine(spec);

        resultHandler = new Handler().useOut(System.out);

        exceptionHandler = new CommandLine.DefaultExceptionHandler<>();
    }

    public static void main(String[] args) throws InterruptedException {
        DeceiverCLI handler = new DeceiverCLI();

        Map<String,Object> options = handler.parseArgs(args);
        System.out.println(options);

        DeceiverOptions deceiverOptions = new DeceiverOptionsImpl(options);
        DeceiverRunnerOptions deceiverRunnerOptions = new DeceiverRunnerOptions();

        System.out.println(deceiverOptions.getQueueCycleTime());

        deceiverRunnerOptions.setMaxPlayTime(deceiverOptions.getMaxPlayingTime());
        deceiverRunnerOptions.setShuttingDownIfQueueIsEmpty(false);
        deceiverRunnerOptions.setQueueCycleTime(deceiverOptions.getQueueCycleTime());

        Deceiver deceiver = new Deceiver(deceiverOptions);

        for (int i = 0; i < Integer.parseInt(options.get("COUNT").toString()); i++) {
            String runnerId = deceiver.initRunner();
            deceiver.setRunnerOptions(runnerId, deceiverRunnerOptions);
        }

        deceiver.startAllRunners();

        Thread.sleep(30000);

        deceiver.stopAllRunners();

    }

    public Map<String, Object> parseArgs(String... args) {
        return commandLine.parseWithHandlers(resultHandler, exceptionHandler, args);
    }

    private static class Handler extends CommandLine.AbstractParseResultHandler<Map<String,Object>> {
        protected Handler self() {
            return this;
        }

        public Map<String,Object> handle(CommandLine.ParseResult pr) {
            Map<String, Object> result = new HashMap<>();
            List<CommandLine.Model.OptionSpec> optionSpecs = pr.commandSpec().options();
            List<CommandLine.Model.OptionSpec> matchedSpecs = pr.matchedOptions();

            for (CommandLine.Model.OptionSpec spec : optionSpecs) {
                CommandLine.Model.OptionSpec matchedSpec = matchedSpecs.stream()
                        .filter(e -> e.paramLabel().equals(spec.paramLabel()))
                        .findFirst()
                        .orElse(null);

                if (matchedSpec == null) {
                    result.put(spec.paramLabel(), spec.defaultValue());
                } else {
                    result.put(spec.paramLabel(), matchedSpec.getValue());
                }

            }

            return result;
        }
    }


}
