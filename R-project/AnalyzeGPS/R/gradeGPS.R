#' Calculate grade between GPS points
#'
#' This function calculates the grade between pair or vector of GPS points as the ratio between the rise and distance traveled.
#' @keywords GPS data, grade calculation
#' @param eleVec Vector of elevations of GPS data points.
#' @param distanceVec Vector of distances between GPS data points (output of the \code{distanceGPS}, padded with an extra zero at the beginning (see vignette of this package)).
#' @return Function returns the value or vector of calculated grade as the ratio between the rise and distance traveled.
#' @export
#' @examples
#' data_file <- system.file("extdata", "myGPSData.csv", package="analyzeGPS")
#' gps <- readGPS(data_file)
#' d <- distanceGPS(lat1 = gps$lat[1:(length(gps$lat)-1)], lon1 = gps$lon[1:(length(gps$lon)-1)],
#' lat2 = gps$lat[2:length(gps$lat)], lon2 = gps$lon[2:length(gps$lon)])
#' grade <- gradeGPS(gps$ele, d)

gradeGPS <- function(eleVec, distanceVec) {

  delta_ele <- eleVec[2:length(eleVec)] - eleVec[1:(length(eleVec)-1)]

  grade <- delta_ele / distanceVec[-1]

  return(grade)

}
