package com.shadeep.util;

import com.sun.management.GarbageCollectionNotificationInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.List;

public class GCWatcher implements NotificationListener {
    private static final Logger log = LogManager.getLogger(GCWatcher.class);

    private final ThreadMXBean threadMXBean;
    private final List<GarbageCollectorMXBean> garbageCollectorMXBeans;
    private final long threshold;

    public GCWatcher(long threshold) {
        this.threshold = threshold;
        threadMXBean = ManagementFactory.getThreadMXBean();
        garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
    }

    public void start() {
        for (GarbageCollectorMXBean gcBean : garbageCollectorMXBeans) {
            NotificationEmitter emitter = (NotificationEmitter) gcBean;
            emitter.addNotificationListener(this, null, null);
        }
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
        if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
            GarbageCollectionNotificationInfo gcInfo = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
            if (gcInfo.getGcInfo().getDuration() > threshold) {
                log.warn(" Long GC pause above {}", threshold);
                log.warn(Arrays.toString(threadMXBean.dumpAllThreads(true, true)));
            }
        }
    }
}
