package com.shadeep.log;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Logs {

    private static final Logger log = LogManager.getLogger(Logs.class);

    private static final String[] SYSTEM_ATTRIBUTES = new String[]{
            "Name",
            "Version",
            "Arch",
            "AvailableProcessors",
            "TotalPhysicalMemorySize",
            "FreePhysicalMemorySize",
            "OpenFileDescriptorCount",
            "MaxFileDescriptorCount"
    };

    private static final long MB = 1024 * 1024;

    private static final String MASK_PASSWORD_PATTERN = "((?i).*(pass|auth|secret|key|token).*)=.*";

    public static void logStartup() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        log.info("===== Runtime details =====");
        log.info("Runtime   : {}", runtime.getName());
        log.info("VM        : {} {}", runtime.getVmName(), runtime.getVmVersion());
        log.info("OS        : {}", System.getProperty("os.name"));
        log.info("Arguments : {}", maskPasswords(runtime.getInputArguments()));
        log.info("Libraries : {}", runtime.getLibraryPath());
        log.info("Classpath : {}", runtime.getClassPath());
        log.info("Directory : {}", System.getProperty("user.dir"));
        log.info("Properties: {}", runtime.getSystemProperties());
        log.info("======================");
    }

    public static void logMachineDetails(boolean all) {
        try {
            MBeanServer platformBean = ManagementFactory.getPlatformMBeanServer();
            ObjectName os = ObjectName.getInstance("java.lang:type=OperatingSystem");

            String[] attrNames;
            if(all) {
                MBeanInfo mBeanInfo = platformBean.getMBeanInfo(os);
                MBeanAttributeInfo[] attributesInfo = mBeanInfo.getAttributes();
                attrNames = Arrays.stream(attributesInfo).map(MBeanAttributeInfo::getName).toArray(String[]::new);
            } else {
                attrNames = SYSTEM_ATTRIBUTES;
            }

            AttributeList attributes = platformBean.getAttributes(os, attrNames);
            log.info("===== Machine details =====");
            for (Object o : attributes) {
                Attribute attr = (Attribute) o;
                if(attr.getName().toLowerCase().contains("size") && attr.getValue() instanceof Long) {
                    log.info(String.format("%-30s: %,d MB", attr.getName(), (Long) attr.getValue() / MB));
                } else {
                    log.info(String.format("%-30s: %s", attr.getName(), attr.getValue()));
                }
            }
            log.info("======================");

        } catch (Exception ex) {
            log.error("Could not get {} ", Arrays.asList(SYSTEM_ATTRIBUTES), ex);
        }
    }

    public static void logFilesystemSpace() {
        Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();
        log.info("===== Filesystem details =====");
        for (Path root : rootDirectories) {
            try {
                FileStore store = Files.getFileStore(root);
                log.info(String.format("%-25s: Available=%,d MB, Total=%,d MB",
                        root,
                        store.getUsableSpace() / MB,
                        store.getTotalSpace() / MB));
            } catch (IOException e) {
                log.error("Error getting file system details for {}", root, e);
            }
        }
        log.info("======================");
    }

    private static String maskPasswords(List<String> args) {
        return args.stream()
                .map(arg -> arg.matches(MASK_PASSWORD_PATTERN) ? arg.substring(0, arg.indexOf("=")) + "=***" : arg)
                .collect(Collectors.joining(" "));
    }

    public static void main(String[] args) {
        Logs.logStartup();
        Logs.logMachineDetails(false);
        Logs.logFilesystemSpace();
    }

}
