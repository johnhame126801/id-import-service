#!/bin/bash
echo "停止开始......"
ID=`ps -ef | grep "id-import-service-1.0-SNAPSHOT.jar" | grep -v "grep" | awk '{print $2}'`
echo $ID
for id in $ID
do
kill -9 $id
echo "killed $id"
done
sleep 5s
echo "停止成功......"
