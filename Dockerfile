FROM tomcat:9.0
# 빌드된 WAR 파일을 ROOT.war 로 복사
COPY build/libs/MoneyBunny-backend-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
