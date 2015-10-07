Analyze GPS data
========

The R package **analyzeGPS** offers functions for basic preparation and analysis of the GPS data: 

* **readGPS**: imports the GPS data in csv format into R data frame
* **distanceGPS**: calculation of distance between two data points or vectors of data points
* **speedGPS**: calculation of velocity between GPS data points
* **accGPS**: calculation of acceleration between GPS data points
* **gradeGPS**: calculation of grade or inclination between GPS data points.

Note: smart apps have been included as they are except for the SMART Pediatric Growth
Chart app where an additional percentile table from the UK-WHO growth charts project has been added.
The UK 1990 data are copyright to the UK Medical Research Council. For more information
please refer to http://www.rcpch.ac.uk/growthcharts.
