Analyze GPS data
========

The R package **analyzeGPS** offers functions for basic preparation and analysis of the GPS data: 

* `readGPS`: imports the GPS data in csv format into R data frame,
* `distanceGPS`: calculation of distance between two data points or vectors of data points,
* `speedGPS`: calculation of velocity between GPS data points,
* `accGPS`: calculation of acceleration between GPS data points,
* `gradeGPS`: calculation of grade or inclination between GPS data points.

Additionally, an example GPS dataset *myGPSData.csv*, acquired during cycling of a person. It is a data frame with 7771 rows and 5 variables:

* `lon`: longitude data,
* `lat`: latitude data,
* `ele`: elevation data,
* `time`: GPS time stamp - GMT time zone,
* `tz_CEST`: time stamp converted to CEST time zone.

