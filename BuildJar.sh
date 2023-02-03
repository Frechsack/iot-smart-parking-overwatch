#!/bin/bash
/usr/share/idea/plugins/maven/lib/maven3/bin/mvn Djavacpp.platform.custom -Djavacpp.platform.linux-arm64 -Djavacpp.platform.linux-x86_64 -f /opt/pom.xml clean package