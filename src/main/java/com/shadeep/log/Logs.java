package com.shadeep.log;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.stream.Collectors;

public class Logs {

    private static final Logger log = LogManager.getLogger(Logs.class);

    private static final String MASK_PASSWORD_PATTERN = "((?i).*(pass|auth|secret|key|token).*)=.*";

    public static void logStartup() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        log.info("======================");
        log.info("Runtime:    {}",    runtime.getName());
        log.info("VM:         {} {}", runtime.getVmName(), runtime.getVmVersion());
        log.info("OS:         {}",    System.getProperty("os.name"));
        log.info("Arguments:  {}",    maskPasswords(runtime.getInputArguments()));
        log.info("Libraries:  {}",    runtime.getLibraryPath());
        log.info("Classpath:  {}",    runtime.getClassPath());
        log.info("Directory:  {}",    System.getProperty("user.dir"));
        log.info("Properties: {}",    runtime.getSystemProperties());
        log.info("======================");
    }

    private static String maskPasswords(List<String> args) {
        return args.stream()
                .map(arg -> arg.matches(MASK_PASSWORD_PATTERN) ? arg.substring(0, arg.indexOf("=")) + "=***" : arg)
                .collect(Collectors.joining(" "));
    }

}
