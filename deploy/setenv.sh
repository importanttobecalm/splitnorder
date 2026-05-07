#!/usr/bin/env bash
# Tomcat 10 — splitnorder JVM args
# Konum: /opt/tomcat10/bin/setenv.sh (chmod 755)
# Tomcat startup.sh otomatik source eder.
#
# NOT: catalina.sh `eval` kullanıyor → JAVA_OPTS içindeki '&' karakterleri
# tek tırnak içinde olmalı, yoksa shell backgrounding zanneder.

# Heap
JAVA_OPTS="-Xms512m -Xmx2048m"

# Spring property override (hibernate.properties'i ezer)
JAVA_OPTS="$JAVA_OPTS '-Dmysql.url=jdbc:mysql://10.0.1.212:3306/stemsep_db?characterEncoding=utf-8&useSSL=true&serverTimezone=UTC'"
JAVA_OPTS="$JAVA_OPTS -Dmysql.user=admin"
JAVA_OPTS="$JAVA_OPTS -Dmysql.password=${MYSQL_PASSWORD}"
JAVA_OPTS="$JAVA_OPTS -Dcolab.api.url=https://seizing-hatless-reflector.ngrok-free.dev"

# Upload/stem dizinleri — production'da sabit absolute path (cwd=/ sorununu önler)
JAVA_OPTS="$JAVA_OPTS -Dupload.directory=/var/lib/stemsep/uploads"
JAVA_OPTS="$JAVA_OPTS -Dstems.directory=/var/lib/stemsep/stems"

# Encoding (Türkçe karakter)
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"

export JAVA_OPTS
