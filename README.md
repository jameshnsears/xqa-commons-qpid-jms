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

## 3. Publishing to [https://search.maven.org/](https://search.maven.org/)
### 3.1. (one off) Register - for groupId
* login to [https://issues.sonatype.org](https://issues.sonatype.org/login.jsp?os_destination=%2Fdefault.jsp)

### 3.2. Visit project XQA JIRA ticket
* https://issues.sonatype.org/browse/OSSRH-38943

### 3.3. settings.xml
* cp settings.xml ~/.m2
* configure <password/> element with correct password

### 3.4. ensure pgp key installed
* installed into Seahorse

### 3.4 deploy
#### 3.4.1. to snapshot
* mvn clean deploy
* [https://oss.sonatype.org/#nexus-search;quick~xqa-commons-qpid-jms](https://oss.sonatype.org/#nexus-search;quick~xqa-commons-qpid-jms)
* if unhappy: mvn nexus-staging:drop

#### 3.4.2 promote to release
* mvn nexus-staging:release

### 3.5. Update XQA JIRA ticket
* 

com.github.jameshnsears has been prepared, now user(s) jsears can:
* Deploy snapshot artifacts into repository https://oss.sonatype.org/content/repositories/snapshots
* Deploy release artifacts into the staging repository https://oss.sonatype.org/service/local/staging/deploy/maven2
* Promote staged artifacts into repository 'Releases'
* Download snapshot and release artifacts from group https://oss.sonatype.org/content/groups/public
* Download snapshot, release and staged artifacts from staging group https://oss.sonatype.org/content/groups/staging
