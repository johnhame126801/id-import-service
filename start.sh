#!/bin/bash

echo "启动开始......"
nohup java -jar id-import-service-1.0-SNAPSHOT.jar --spring.profiles.active=prod >/dev/null 2>&1&
echo "启动成功......"

