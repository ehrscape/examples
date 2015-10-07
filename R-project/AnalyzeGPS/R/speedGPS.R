#' Calculate velocity/speed between GPS points
#'
#' This function calculates the speed in meters per second between pair or vector of GPS points based on the time and distance between them.
#' @keywords GPS data, velocity calculation
#' @param timeVec Time vector of the GPS data points in \dQuote{Y-m-dTH:M:SZ} format.
#' @param distanceVec Vector of distances between GPS data points (output of the \code{\link{distanceGPS}}).
#' @return Function returns the value or vector of calculated speed in m/s.
#' @export
#' @examples
#' data_file <- system.file("extdata", "myGPSData.csv", package="analyzeGPS")
#' gps <- readGPS(data_file)
#' d <- distanceGPS(lat1 = gps$lat[1:(length(gps$lat)-1)], lon1 = gps$lon[1:(length(gps$lon)-1)],
#' lat2 = gps$lat[2:length(gps$lat)], lon2 = gps$lon[2:length(gps$lon)])
#' speed <- speedGPS(gps$time, d)

speedGPS <- function(timeVec, distanceVec) {
  timeVec <- as.POSIXct(timeVec, format = "%Y-%m-%dT%H:%M:%SZ")

  delta_time <- as.numeric(timeVec[2:length(timeVec)] -
                             timeVec[1:(length(timeVec)-1)])
  speed <- distanceVec / delta_time

  return(speed)

}
