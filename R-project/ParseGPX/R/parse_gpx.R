#' Parse GPX data file to a data frame
#'
#' This script reads a gpx file, converts date-time string data. The final gps dataframe is loaded to environment and can be saved as a csv file.
#'
#' @keywords GPX data, parse, csv format
#' @param NAME The input argument NAME is the string path to the selected GPX file (gpx format).
#' @param writeData Write to disk flag. Default setting is FALSE. If set to TRUE, the parsed GPX data frame is saved as csv file to the same directory as the source GPX file.
#' @param timeZone Time zone definition of the parsed data. Default value is "GMT". For other possible values see \code{\link[base]{timezones}}.
#' @return
#'  The function returns a data frame with GPS data. If the writeData flag is set to TRUE, the data frame is also saved in csv format to the same location as the source gpx file.
#' @export
#' @examples
#' data_file <- system.file("extdata", "myGPXData.gpx", package="parseGPX")
#' gpx <- parse_gpx(data_file)
#' gpx <- parse_gpx(data_file, writeData = TRUE)
#' gpx <- parse_gpx(data_file, timeZone = "Europe/Ljubljana")
#' gpx <- parse_gpx(data_file, writeData = TRUE, timeZone = "Europe/Ljubljana")

parse_gpx <- function(NAME="filename", writeData = FALSE, timeZone = "GMT") {

  gpx_file <- NAME
  # Extract file and directory names
  file_name <- substr(basename(gpx_file), 1, nchar(basename(gpx_file))-4)
  directory_name <- dirname(gpx_file)

  # Read data
  gpx_data <- plotKML::readGPX(gpx.file = gpx_file,
                               metadata = FALSE,
                               bounds = FALSE,
                               waypoints = FALSE,
                               routes = FALSE)
  gpx_data <- gpx_data[[4]][[1]][[1]]

  # Convert date-time string to time data.
  gpxdatum <- as.POSIXct(gpx_data$time, format = "%Y-%m-%dT%H:%M:%SZ", tz="GMT")

  if (timeZone != "GMT") {
    attributes(gpxdatum)$tzone <- timeZone
  }

  # Add time data column to gpx_data data frame
  gpx_data$tz_CEST <- gpxdatum

  # Convert numbers to type numeric
  if (sum("ele" == names(gpx_data)) > 0) {
    gpx_data$ele <- as.numeric(gpx_data$ele)
  }
  if (sum("speed" == names(gpx_data)) > 0) {
    gpx_data$speed <- as.numeric(gpx_data$speed)
  }

  if (writeData == TRUE) {
    # Export data to csv file
    write.table(gpx_data,
                file = paste(directory_name, "/", file_name, ".csv", sep = ""),
                sep = ",", row.names = FALSE, quote = FALSE)
  }

  return(gpx_data)
}
