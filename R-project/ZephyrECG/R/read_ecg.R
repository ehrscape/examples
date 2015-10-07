#' Read ECG data file
#'
#' This function reads raw ECG data acquired by Zephyr BH3 and sorts it into a data frame.
#' The stringsAsFactors parameter is set to FALSE by default.
#' @keywords ECG data, read
#' @param NAME The input parameter is the string path to the selected ECG file (csv format).
#' @return Function returns the ecg data frame.
#' @export
#' @examples
#' data_file <- system.file("extdata", "myECGData.csv", package="zephyrECG")
#' ecg <- read_ecg(data_file)

read_ecg <- function(NAME="filename") {
  ecg_file <- NAME
  ecg <- read.csv(ecg_file, stringsAsFactors=FALSE)
  if (length(ecg) == 2) {
    names(ecg) <- c("datetimems", "measurement")
  } else {
    ecg <- ecg[, c(length(ecg) - 1, length(ecg))]
    names(ecg) <- c("datetimems", "measurement")
  }
  return(ecg)
}

