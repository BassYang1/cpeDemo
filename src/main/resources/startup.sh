#!/bin/sh

JAVA_OPTS="-Xmx6G -Xms6G -XX:NewSize=256m -XX:MaxNewSize=256m -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=2 -XX:TargetSurvivorRatio=50 -XX:+CMSClassUnloadingEnabled -XX:+HeapDumpOnOutOfMemoryError -XX:+UseG1GC -XX:+UseStringDeduplication"

cpePID=0

getCpePID(){
    javaps=`ps -ef | grep cpe-simulator.jar | grep -v "$0" | grep -v "grep"`
    if [ -n "$javaps" ]; then
        cpePID=`echo $javaps | awk '{print $2}'`
    else
        cpePID=0
    fi
}

startup(){
    getCpePID
    echo "================================================================================================================"
    if [ $cpePID -ne 0 ]; then
        echo "CPE already started(PID=$cpePID)!"
        echo "================================================================================================================"
    else
        echo "Starting CPE..."
        set CLASSPATH=.
        nohup java $JAVA_OPTS -Xloggc:gc.log  -jar cpe-*.jar >server.out 2>&1 &
        getCpePID
        if [ $cpePID -ne 0 ]; then
            echo "CPE started successful(PID=$cpePID)!"
            echo "================================================================================================================"
        else
            echo "CPE started  failed!"
            echo "================================================================================================================"
        fi
    fi
}

startup

