#!/bin/sh
cpePID=0

getCpePID(){
    javaps=`ps -ef | grep cpe-*.jar | grep -v "$0" | grep -v "grep"`
    if [ -n "$javaps" ]; then
        cpePID=`echo $javaps | awk '{print $2}'`
    else
        cpePID=0
    fi
}

shutdown(){
    getCpePID
    echo "================================================================================================================"
    if [ $cpePID -ne 0 ]; then
        echo "Stopping CPE(PID=$cpePID)..."
        kill -9 $cpePID
        if [ $? -eq 0 ]; then
            echo "CPE stopped successful!"
            echo "================================================================================================================"
        else
            echo "CPE stopped failed!"
            echo "================================================================================================================"
        fi
        getCpePID
        if [ $cpePID -ne 0 ]; then
            shutdown
        fi
    else
        echo "CPE is not running"
        echo "================================================================================================================"
    fi
}

shutdown
