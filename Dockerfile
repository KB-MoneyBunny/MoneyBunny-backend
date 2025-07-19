FROM tomcat:9.0

# 기존 default webapps 삭제 (필수 아님, 권장)
RUN rm -rf /usr/local/tomcat/webapps/*

# WAR 복사
COPY build/libs/MoneyBunny-backend-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
