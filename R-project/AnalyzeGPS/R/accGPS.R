#' Calculate acceleration between GPS points
#'
#' This function calculates the acceleration in m/s^2 between pair or vector of GPS points based on the time and velocity between them.
#' @keywords GPS data, acceleration calculation
#' @param timeVec Time vector of the GPS data points in \dQuote{Y-m-dTH:M:SZ} format.
#' @param speedVec Vector of velocities (in m/s) between GPS data points (output of the \code{\link{speedGPS}}).
#' @return Function returns the value or vector of calculated acceleration in m/s^2.
#' @export
#' @examples
#' data_file <- system.file("extdata", "myGPSData.csv", package="analyzeGPS")
#' gps <- readGPS(data_file)
#' d <- distanceGPS(lat1 = gps$lat[1:(length(gps$lat)-1)], lon1 = gps$lon[1:(length(gps$lon)-1)],
#' lat2 = gps$lat[2:length(gps$lat)], lon2 = gps$lon[2:length(gps$lon)])
#' speed <- speedGPS(gps$time, d)
#' acc <- accGPS(gps$time, speed)

accGPS <- function(timeVec, speedVec) {
  timeVec <- as.POSIXct(timeVec, format = "%Y-%m-%dT%H:%M:%SZ")

  delta_time <- as.numeric(timeVec[2:length(timeVec)] -
                             timeVec[1:(length(timeVec)-1)])

  delta_speed <- speedVec[2:length(speedVec)] - speedVec[1:(length(speedVec)-1)]

  acc <- delta_speed / delta_time

  return(acc)

}
