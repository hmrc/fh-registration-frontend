# Fulfilment House Registration Scheme Application

[![Build Status](https://travis-ci.org/hmrc/fhdds-frontend.svg)](https://travis-ci.org/hmrc/fhdds-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/fhdds-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/fhdds-frontend/_latestVersion)

This service provides the frontend endpoint for the [Fulfilment House Registration Scheme](https://www.gov.uk/guidance/fulfilment-house-due-diligence-scheme) project.

## Summary

This service allow a customer to apply for apply for the Fulfilment House Registration Scheme.

## Requirements

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE] to run.
*   MongoDB 3.2 (not later as of this time the platform is not compatible with higher versions)
*   KEYSTORE and ASSETS_FRONTEND get the latest version via the service-manager

## Run the application locally

User service manager to run all services required by FHDDS Frontend:

```
sm --start FHDDS_ALL -f
```

To run the application execute
```
sbt run
```

### Get to the landing page

```
GET   	/fhdds-frontend/
```

## Authentication

This customer logs into this service using [GOV.UK Verify](https://www.gov.uk/government/publications/introducing-govuk-verify/introducing-govuk-verify).


## Acronyms

In the context of this application we use the following acronyms and define their
meanings. Provided you will also find a web link to discover more about the systems
and technology.

* [API]: Application Programming Interface

* [HoD]: Head of Duty

* [JRE]: Java Runtime Environment

* [JSON]: JavaScript Object Notation

* [URL]: Uniform Resource Locator

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

[HoD]: http://webarchive.nationalarchives.gov.uk/+/http://www.hmrc.gov.uk/manuals/sam/samglossary/samgloss249.htm
[JRE]: http://www.oracle.com/technetwork/java/javase/overview/index.html
[API]: https://en.wikipedia.org/wiki/Application_programming_interface
[URL]: https://en.wikipedia.org/wiki/Uniform_Resource_Locator
[JSON]: http://json.org/