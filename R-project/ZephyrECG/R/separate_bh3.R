#' Zephyr BioHarness 3 data file separation
#'
#' This function separates ECG sessions from the Zephyr BioHarness 3 file to single csv files.
#' Zephyr BH3 unix timestamps are converted to date-time format Y-m-d HH:MM:SS.ms(3 digits).
#' The function writes the separate ECG sessions as csv files to the same directory.
#' @keywords Zephyr BH3 data file, separate
#' @param NAME Input for the function is a string with the path to the BH3 file.
#' @return Function doesn't return any objects to the environment. Instead it writes the separated sessions as csv files to the same directory as the source data file.
#' @export
#' @examples
#' data_file <- system.file("extdata", "myZephyrBH3Data.csv", package="zephyrECG")
#' separate_bh3(data_file)


separate_bh3 <- function(NAME="filename") {

  ecg_file <- NAME
  # Extract directory name
  ecg_directory_name <- dirname(ecg_file)

  ecg <- read.csv(ecg_file, stringsAsFactors=FALSE)

  # Determine number of different sessions in data file.
  time_idx <- format(as.POSIXct(ecg$datetimems/1000,
                                  origin = "1970-01-01"), "%Y_%m_%d-%H_%M_%S")
  session_index <- c(1, which(diff(ecg$datetimems/1000) > 60) + 1)
  ecg_file_name <- rep("", length(session_index))

  # Export data of each session to separate csv files.
  for (i in 1:length(session_index)) {
    ecg_file_name[i] <- time_idx[session_index[i]]

    if (i == length(session_index)) {
      write.table(ecg[session_index[i]:dim(ecg)[1],],
                  file = paste(ecg_directory_name, "/", ecg_file_name[i], ".csv", sep=""),
                  sep = ",", row.names = FALSE, quote = FALSE)
    } else {
      write.table(ecg[session_index[i]:(session_index[i+1]-1),],
                  file = paste(ecg_directory_name, "/", ecg_file_name[i], ".csv", sep=""),
                  sep = ",", row.names = FALSE, quote = FALSE)
    }

  }
}



