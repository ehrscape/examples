## ---- out.width=60-------------------------------------------------------
library(analyzeGPS)
data_file <- system.file("extdata", "myGPSData.csv", package="analyzeGPS")
gps <- readGPS(data_file)

str(gps, strict.width = "wrap")

## ---- fig.width=6, fig.height=6, fig.align='center', tidy=TRUE, message=FALSE----
library(ggmap)
ggmap(get_googlemap(center = c(lon = mean(gps$lon), lat = mean(gps$lat)), zoom = 11, size = c(720, 720))) + 
  geom_point(data = gps, aes(x = lon, y = lat))

## ---- fig.width=6, fig.height=6, fig.align='center', tidy=TRUE, message=FALSE----
library(plot3D)
lines3D(gps$lon, gps$lat, gps$ele, phi = 10, theta = 20)

## ---- fig.width=6, fig.height=6, fig.align='center', tidy=TRUE, message=FALSE, eval=FALSE----
#  library(rgl)
#  plot3d(gps$lon, gps$lat, gps$ele)

## ---- fig.width=7, fig.height=5, fig.align='center', tidy=TRUE, message=FALSE----
d <- distanceGPS(lat1 = gps$lat[1:(length(gps$lat)-1)], lon1 = gps$lon[1:(length(gps$lon)-1)],
                 lat2 = gps$lat[2:length(gps$lat)], lon2 = gps$lon[2:length(gps$lon)])
gps$d <- c(0, d)
str(gps$d)
gps$dist <- cumsum(gps$d)
str(gps$dist)

library(ggplot2)
ggplot(data = gps, aes(x = dist/1000, y = ele)) + 
  geom_line() + 
  xlab("Distance [km]") + 
  ylab("Elevation [m]")

## ---- eval=FALSE---------------------------------------------------------
#  distanceGPS <- function(lat1, lon1, lat2, lon2) {
#  
#    # Convert degrees to radians
#    lat1 <- lat1 * pi/180
#    lat2 <- lat2 * pi/180
#    lon2 <- lon2 * pi/180
#    lon1 <- lon1 * pi/180
#  
#    # Haversine formula;
#    R = 6371000
#    a <- sin(0.5 * (lat2 - lat1))
#    b <- sin(0.5 * (lon2 - lon1))
#    d <- 2 * R * asin(sqrt(a * a + cos(lat1) * cos(lat2) * b * b))
#  
#    return(d)
#  
#  }

## ---- fig.width=7, fig.height=5, fig.align='center', tidy=TRUE, message=FALSE----

speed <- speedGPS(gps$time, gps$d[-1])
str(speed)

mean(speed, na.rm = TRUE)
max(speed, na.rm = TRUE)
min(speed, na.rm = TRUE)

gps$speed <- c(NA, speed)
str(gps, strict.width = "wrap")

## ---- fig.width=7, fig.height=5, fig.align='center', tidy=TRUE, message=FALSE, warning=FALSE----
ggplot(data = gps, aes(x = dist/1000, y = speed*3.6)) + 
  geom_line() + 
  xlab("Distance [km]") + 
  ylab("Speed [km/h]")

## ---- fig.width=7, fig.height=5, fig.align='center', tidy=TRUE, message=FALSE, warning=FALSE----
library(zoo)
speed_smooth <- rollmean(gps$speed[-1],10)
gps$speed_smooth <- c(NA, speed_smooth, tail(gps$speed,9)) 

ggplot(data = gps, aes(x = dist/1000, y = speed_smooth*3.6)) + 
  geom_line() + 
  xlab("Distance [km]") + 
  ylab("Smoothed speed [km/h]")

## ---- eval=FALSE---------------------------------------------------------
#  speedGPS <- function(timeVec, distanceVec) {
#    timeVec <- as.POSIXct(timeVec, format = "%Y-%m-%dT%H:%M:%SZ")
#  
#    delta_time <- as.numeric(timeVec[2:length(timeVec)] -
#                               timeVec[1:(length(timeVec)-1)])
#    speed <- distanceVec / delta_time
#  
#    return(speed)
#  
#  }

## ---- fig.width=7, fig.height=5, fig.align='center', tidy=TRUE, message=FALSE, warning=FALSE----
acc <- accGPS(gps$time, gps$speed_smooth)
str(acc)

mean(acc, na.rm = TRUE)
max(acc, na.rm = TRUE)
min(acc, na.rm = TRUE)

gps$acc <- c(NA, acc)
str(gps, strict.width = "wrap")

## ---- fig.width=7, fig.height=5, fig.align='center', tidy=TRUE, message=FALSE, warning=FALSE----
ggplot(data = gps, aes(x = dist/1000, y = acc)) + 
  geom_line() + 
  xlab("Distance [km]") + 
  ylab("Acceleration [m/s^2]")

## ---- eval=FALSE---------------------------------------------------------
#  accGPS <- function(timeVec, speedVec) {
#    timeVec <- as.POSIXct(timeVec, format = "%Y-%m-%dT%H:%M:%SZ")
#  
#    delta_time <- as.numeric(timeVec[2:length(timeVec)] -
#                               timeVec[1:(length(timeVec)-1)])
#  
#    delta_speed <- speedVec[2:length(speedVec)] - speedVec[1:(length(speedVec)-1)]
#  
#    acc <- delta_speed / delta_time
#  
#    return(acc)
#  
#  }

## ---- fig.width=7, fig.height=5, fig.align='center', tidy=TRUE, message=FALSE, warning=FALSE----
grade <- gradeGPS(gps$ele, gps$d)
str(grade)

mean(grade, na.rm = TRUE)
max(grade, na.rm = TRUE)
min(grade, na.rm = TRUE)

gps$grade <- c(NA, grade)
str(gps, strict.width = "wrap")

## ---- eval=FALSE---------------------------------------------------------
#  gradeGPS <- function(eleVec, distanceVec) {
#  
#    delta_ele <- eleVec[2:length(eleVec)] - eleVec[1:(length(eleVec)-1)]
#  
#    grade <- delta_ele / distanceVec[-1]
#  
#    return(grade)
#  
#  }

