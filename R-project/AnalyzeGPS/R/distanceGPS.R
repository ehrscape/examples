#' Calculate distance between GPS points
#'
#' This function calculates the distance in meters between pair or vector of GPS points described with latitude and longitude. The calculation considers the Earth radius by applying the Haversine formula. The number of elements of the output is one less than the number of input (lon, lat).
#' @keywords GPS data, distance calculation
#' @param lat1 latitude data of the first point (or vector of points).
#' @param lon1 longitude data of the first point (or vector of points).
#' @param lat2 latitude data of the second point (or vector of points).
#' @param lon2 longitude data of the second point (or vector of points).
#' @return Function returns the value or vector of calculated distances in meters.
#' @export
#' @examples
#' data_file <- system.file("extdata", "myGPSData.csv", package="analyzeGPS")
#' gps <- readGPS(data_file)
#' d <- distanceGPS(lat1 = gps$lat[1:(length(gps$lat)-1)], lon1 = gps$lon[1:(length(gps$lon)-1)],
#' lat2 = gps$lat[2:length(gps$lat)], lon2 = gps$lon[2:length(gps$lon)])


distanceGPS <- function(lat1, lon1, lat2, lon2) {

  # Convert degrees to radians
  lat1 <- lat1 * pi/180
  lat2 <- lat2 * pi/180
  lon2 <- lon2 * pi/180
  lon1 <- lon1 * pi/180

  # Haversine formula;
  R = 6371000
  a <- sin(0.5 * (lat2 - lat1))
  b <- sin(0.5 * (lon2 - lon1))
  d <- 2 * R * asin(sqrt(a * a + cos(lat1) * cos(lat2) * b * b))

  return(d)

}
