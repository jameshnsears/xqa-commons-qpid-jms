# xqa-commons-qpid-jms [![Build Status](https://travis-ci.org/jameshnsears/xqa-commons-qpid-jms.svg?branch=master)](https://travis-ci.org/jameshnsears/xqa-commons-qpid-jms) [![Coverage Status](https://coveralls.io/repos/github/jameshnsears/xqa-commons-qpid-jms/badge.svg?branch=master)](https://coveralls.io/github/jameshnsears/xqa-commons-qpid-jms?branch=master)
* common functionality used by various projects.

## 1. Build
* rm -rf $HOME/.m2/*
* mvn clean package -DskipTests

## 2. Test
* docker-compose -p "dev" up -d xqa-message-broker
* mvn test
* mvn jacoco:report coveralls:report
* mvn site  # findbugs
