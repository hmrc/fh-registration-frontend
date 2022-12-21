# fh-registration-frontend

[ ![Download](https://api.bintray.com/packages/hmrc/releases/fh-registration-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/fh-registration-frontend/_latestVersion)

This service provides the frontend endpoint for the [Fulfilment House Due Diligence Registration Scheme](https://www.gov.uk/guidance/fulfilment-house-due-diligence-scheme) project.

## Run the application locally

Use service manager to run all the required services:

```
sm --start FHDDS_ALL -f
```

To run the application execute
```
sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes
```

License
---
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").