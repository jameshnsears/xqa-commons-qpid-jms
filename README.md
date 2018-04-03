# xqa-commons-qpid-jms [![Build Status](https://travis-ci.org/jameshnsears/xqa-commons-qpid-jms.svg?branch=master)](https://travis-ci.org/jameshnsears/xqa-commons-qpid-jms) [![Coverage Status](https://coveralls.io/repos/github/jameshnsears/xqa-commons-qpid-jms/badge.svg?branch=master)](https://coveralls.io/github/jameshnsears/xqa-commons-qpid-jms?branch=master) [![sonarcloud.io](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_xqa-commons-qpid-jms&metric=alert_status)](https://sonarcloud.io/dashboard?id=jameshnsears_xqa-commons-qpid-jms) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/f6ad070e1779484887c9c8e5a9bd45d4)](https://www.codacy.com/app/jameshnsears/xqa-commons-qpid-jms?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/xqa-commons-qpid-jms&amp;utm_campaign=Badge_Grade)
* a Maven Central shared library.

## 1. Build
Assuming the [build prerequisites](https://github.com/jameshnsears/xqa-documentation/blob/master/BUILD-PREREQUISITES.md) have been met:
* ./build.sh

## 2. Test
* see .travis.yml

## 3. Publishing to [Maven Central](https://search.maven.org/)

### 3.1. (one off) Register - for groupId, creating JIRA ticket
* login to [https://issues.sonatype.org](https://issues.sonatype.org/login.jsp?os_destination=%2Fdefault.jsp)
* using groupId: com.github.jameshnsears

#### 3.1.1. Visit project JIRA ticket
* record any deployment issues in: https://issues.sonatype.org/browse/OSSRH-nnnnn

### 3.2. settings.xml
* decrypt settings.xml.pgp
* cp settings.xml ~/.m2

### 3.3. Ensure pgp key installed
* install into Seahorse

### 3.4. Deploy
export JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64"

#### 3.4.1. To staging repo
* mvn -DperformRelease=true clean deploy
    * enter private pgp key password
* visit:
    * [https://oss.sonatype.org/#nexus-search;quick~xqa-commons-qpid-jms](https://oss.sonatype.org/#nexus-search;quick~xqa-commons-qpid-jms)

#### 3.4.2 To release repo
* (optionally) [https://www.youtube.com/watch?v=dXR4pJ_zS-0&feature=youtu.be](https://www.youtube.com/watch?v=dXR4pJ_zS-0&feature=youtu.be)
* remove "-SNAPSHOT" in pom.xml + increment version #.
* mvn -DperformRelease=true clean deploy
    * enter private pgp key password

### 3.5. Search Maven Central
* after a couple of hours [https://search.maven.org/](https://search.maven.org/#search%7Cga%7C1%7Cjameshnsears) for com.github.jameshnsears, pom.xml imports should appear in ~10 minutes.

### 3.6. (one off / first time publishing) Update JIRA ticket
* update https://issues.sonatype.org/browse/OSSRH-nnnnn - saying it's published successfully.
