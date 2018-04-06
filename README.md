# xqa-commons-qpid-jms [![Build Status](https://travis-ci.org/jameshnsears/xqa-commons-qpid-jms.svg?branch=master)](https://travis-ci.org/jameshnsears/xqa-commons-qpid-jms) [![Coverage Status](https://coveralls.io/repos/github/jameshnsears/xqa-commons-qpid-jms/badge.svg?branch=master)](https://coveralls.io/github/jameshnsears/xqa-commons-qpid-jms?branch=master)
* maven central hosted, common qpid-jms functionality used by various xqa projects.

## 1. Build
* rm -rf $HOME/.m2/*
* mvn clean package -DskipTests

## 2. Test
* docker-compose -p "dev" up -d xqa-message-broker
* mvn test
* mvn jacoco:report coveralls:report
* mvn site  # findbugs

## 3. Publishing to [Maven Central](https://search.maven.org/)
* (one off) [https://www.youtube.com/watch?v=0gyF17kWMLg&feature=youtu.be](https://www.youtube.com/watch?v=0gyF17kWMLg&feature=youtu.be)

### 3.1. (one off) Register - for groupId, creating JIRA ticket
* login to [https://issues.sonatype.org](https://issues.sonatype.org/login.jsp?os_destination=%2Fdefault.jsp)
* using groupId: com.github.jameshnsears

#### 3.1.1. Visit project JIRA ticket
* record any deployment issues in: https://issues.sonatype.org/browse/OSSRH-38943

### 3.2. settings.xml
* cp settings.xml ~/.m2
* configure <password/> element with correct password

### 3.3. Ensure pgp key installed
* install into Seahorse

### 3.4. Deploy
#### 3.4.1. To staging snapshot repo
* mvn -DperformRelease=true clean deploy
    * entering private pgp key password
* visit:
    * [https://oss.sonatype.org/#nexus-search;quick~xqa-commons-qpid-jms](https://oss.sonatype.org/#nexus-search;quick~xqa-commons-qpid-jms)

#### 3.4.2 To staging release repo
* [https://www.youtube.com/watch?v=dXR4pJ_zS-0&feature=youtu.be](https://www.youtube.com/watch?v=dXR4pJ_zS-0&feature=youtu.be)

##### 3.4.2.1. publish
* remove "-SNAPSHOT" in pom.xml + increment version #.
* mvn -DperformRelease=true clean deploy
    * entering private pgp key password

### 3.5. Search Maven Cental
* after a couple of hours [https://search.maven.org/](https://search.maven.org/) for com.github.jameshnsears, pom.xml imports should happen in 10 minutes.

### 3.6.. (one off) Update JIRA ticket
* update https://issues.sonatype.org/browse/OSSRH-38943 - saying it's published successfully.
