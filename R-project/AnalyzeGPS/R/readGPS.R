#' Read GPS data file
#'
#' This function reads the acquired GPS data in csv format and sorts it into a data frame.
#' @keywords GPS data, read
#' @param NAME The input parameter is the string path to the selected GPS file (csv format).
#' @return Function returns the gps data frame.
#' @export
#' @examples
#' data_file <- system.file("extdata", "myGPSData.csv", package="analyzeGPS")
#' gps <- readGPS(data_file)

readGPS <- function(NAME="filename") {

  gps <- read.csv(NAME, stringsAsFactors=FALSE)
  return(gps)
}
