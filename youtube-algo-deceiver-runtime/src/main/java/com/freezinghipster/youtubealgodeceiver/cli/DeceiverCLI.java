package com.freezinghipster.youtubealgodeceiver.cli;

import com.freezinghipster.youtubealgodeceiver.Deceiver;
import com.freezinghipster.youtubealgodeceiver.DeceiverOptions;
import com.freezinghipster.youtubealgodeceiver.cli.DeceiverOptionsImpl.YoutubeAlgoDeceiverOptionKeys;
import picocli.CommandLine;

import java.util.Arrays;
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
                        .paramLabel(YoutubeAlgoDeceiverOptionKeys.GECKO_DRIVER_PATH.toString())
                        .type(String.class)
                        .description("full path to gecko driver binary")
                        .required(true)
                        .build()
        );

        spec.addOption(
                CommandLine.Model.OptionSpec.builder("--firefoxPath")
                        .paramLabel(YoutubeAlgoDeceiverOptionKeys.FIREFOX_PATH.toString())
                        .type(String.class)
                        .description("full path to firefox binary")
                        .required(true)
                        .build()
        );

        spec.addOption(
                CommandLine.Model.OptionSpec.builder("--profileFolderPath")
                        .paramLabel(YoutubeAlgoDeceiverOptionKeys.PROFILE_FOLDER_PATH.toString())
                        .type(String.class)
                        .description("full path to firefox profile path. A copy will be created in /tmp for each instance.")
                        .required(true)
                        .build()
        );


        spec.addOption(
                CommandLine.Model.OptionSpec.builder("-d", "--duration")
                        .paramLabel(YoutubeAlgoDeceiverOptionKeys.DURATION.toString())
                        .type(Integer.class)
                        .description("maximum number of seconds that each runner will spend on each video")
                        .defaultValue("60")
                        .build()
        );

        spec.addOption(
                CommandLine.Model.OptionSpec.builder("-T", "--queueTimeout")
                        .paramLabel(YoutubeAlgoDeceiverOptionKeys.QUEUE_TIMEOUT.toString())
                        .type(Integer.class)
                        .description("number of videoId queue polling cycles that a runner will wait for before exiting")
                        .defaultValue("3")
                        .build()
        );

        spec.addOption(
                CommandLine.Model.OptionSpec.builder("-t", "--queueCycleTime")
                        .paramLabel(YoutubeAlgoDeceiverOptionKeys.QUEUE_CYCLE_TIME.toString())
                        .type(Integer.class)
                        .description("number of seconds between queue polling")
                        .defaultValue("10")
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

        DeceiverOptions defaultDeceiverOptions = new DeceiverOptionsImpl(options);

        Deceiver deceiver = new Deceiver(defaultDeceiverOptions);

        for (int i = 0; i < Integer.parseInt(options.get("COUNT").toString()); i++) {
            String runnerId = deceiver.initRunner();
            deceiver.setMaxPlayTime(runnerId, defaultDeceiverOptions.getInstancePlayDuration());
            deceiver.setShutdownOnEmptyQueue(runnerId, false);
        }

        deceiver.startAllRunners();

        deceiver.pushVideoIds(Arrays.asList("xsxy5LZ1T_A", "WJt_4vgWLlA"));

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
