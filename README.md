# fhdds-frontend

[![Build Status](https://travis-ci.org/hmrc/fhdds-frontend.svg)](https://travis-ci.org/hmrc/fhdds-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/fhdds-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/fhdds-frontend/_latestVersion)

## Software Requirements
*   Contact to team barzan if needed #team-barzan
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

#### Get to the landing page
```
GET   	/fhdds-frontend/
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
