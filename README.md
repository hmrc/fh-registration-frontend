# fh-registration-frontend

[![Build Status](https://travis-ci.org/hmrc/fh-registration-frontend.svg?branch=master)](https://travis-ci.org/hmrc/fh-registration-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/fh-registration-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/fh-registration-frontend/_latestVersion)

This service provides the frontend endpoint for the [Fulfilment House Registration Scheme](https://www.gov.uk/guidance/fulfilment-house-due-diligence-scheme) project.

## Summary

This service allow a customer to apply for the Fulfilment House Registration Scheme.

##Authentication

This customer logs into this service using [Government Gateway](http://www.gateway.gov.uk/).

## Requirements

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE] to run. It also
requires [MongoDB 3.2](https://www.mongodb.com/).

## Run the application locally

Use service manager to run all services required by FHDDS Frontend:

```
sm --start FHDDS_ALL -f
```

To run the application execute
```
sbt run
```

### Get to the landing page

```
GET   	/fhdds/
```

### How to test project

Unit Tests
```
sbt test
```

Integration Tests
```
sbt it:test
```

Acceptance Tests
```
https://github.com/hmrc/fh-registration-acceptance-tests
```

Performance Tests
```
https://github.com/hmrc/fh-registration-performance-tests
```

Contract Tests
```
https://github.com/hmrc/fh-registration-contract-tests
```
