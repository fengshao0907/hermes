#!/bin/sh

# set jvm startup argument
JAVA_OPTS="-Djava.awt.headless=true \
            -Dcom.sun.management.jmxremote.port=8301 \
            -Dcom.sun.management.jmxremote.authenticate=false \
            -Dcom.sun.management.jmxremote.ssl=false \
            -Dfile.encoding=utf-8 \
            -Dio.netty.allocator.type=pooled \
            -Xms2g \
            -Xmx2g \
            -Xmn1g \
            -XX:PermSize=128m \
            -XX:MaxPermSize=256m \
            -XX:-DisableExplicitGC \
            -XX:+PrintGC \
            -XX:+PrintGCDetails \
            -XX:+PrintGCDateStamps \
            -Xloggc:../logs/gc.log \
            -XX:-OmitStackTraceInFastThrow \
            -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/logs/hermes/
            "
export JAVA_OPTS=${JAVA_OPTS}