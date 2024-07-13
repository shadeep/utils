package com.shadeep.log;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.test.appender.ListAppender;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class LogsTest {

    private ListAppender appender;

    @BeforeEach
    void setup() {
        LoggerContext loggerContext = LoggerContext.getContext(false);
        ExtendedLogger logger = loggerContext.getLogger(Logs.class);
        appender = new ListAppender("List");
        appender.start();
        loggerContext.getConfiguration().addLoggerAppender((Logger)logger, appender);
    }

    @Test
    void testClasspath() {
        Logs.logStartup();
        List<String> classPathLogs = appender.getEvents().stream()
                .map(e -> e.getMessage().getFormattedMessage())
                .filter(s -> s.contains("Classpath :"))
                .collect(Collectors.toList());
        assertThat(classPathLogs.size()).isEqualTo(1);
        assertThat(classPathLogs.get(0)).contains("junit-jupiter-engine");
    }

}