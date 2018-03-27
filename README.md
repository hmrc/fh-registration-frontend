# fh-registration-frontend

[![Build Status](https://travis-ci.org/hmrc/fh-registration-frontend.svg?branch=master)](https://travis-ci.org/hmrc/fh-registration-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/fh-registration-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/fh-registration-frontend/_latestVersion)

This service provides the frontend endpoint for the [Fulfilment House Registration Scheme](https://www.gov.uk/guidance/fulfilment-house-due-diligence-scheme) project.

## Summary

This service allow a customer to apply for the Fulfilment House Registration Scheme.

## Authentication

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

## Get to the landing page

```
GET   	/fhdds/
```

## Acronyms

In the context of this application we use the following acronyms and define their
meanings. Provided you will also find a web link to discover more about the systems
and technology.

* [API]: Application Programming Interface

* [HoD]: Head of Duty

* [JRE]: Java Runtime Environment

* [JSON]: JavaScript Object Notation

* [NINO]: National Insurance Number

* [URL]: Uniform Resource Locator


License
---
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

[HoD]: http://webarchive.nationalarchives.gov.uk/+/http://www.hmrc.gov.uk/manuals/sam/samglossary/samgloss249.htm
[NINO]: http://www.hmrc.gov.uk/manuals/nimmanual/nim39110.htm
[JRE]: http://www.oracle.com/technetwork/java/javase/overview/index.html
[API]: https://en.wikipedia.org/wiki/Application_programming_interface
[URL]: https://en.wikipedia.org/wiki/Uniform_Resource_Locator
[JSON]: http://json.org/