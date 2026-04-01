MVN ?= mvnw.cmd
LOCAL_JAVA_HOME ?= C:\Program Files\Java\jdk-17.0.1

JAVA_ENV = set "JAVA_HOME=$(LOCAL_JAVA_HOME)" &&
MVNJ = $(JAVA_ENV) $(MVN)

run:
	$(MVNJ) spring-boot:run

dev:
	$(MVNJ) spring-boot:run -Dspring-boot.run.profiles=dev