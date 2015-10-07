## ---- eval=FALSE---------------------------------------------------------
#  separate_bh3 <- function(NAME="filename") {
#  
#    ecg_file <- NAME
#    # Extract directory name
#    ecg_directory_name <- dirname(ecg_file)
#  
#    ecg <- read.csv(ecg_file, stringsAsFactors=FALSE)
#  
#    # Determine number of different sessions in data file.
#    time_idx <- format(as.POSIXct(ecg$datetimems/1000, origin = "1970-01-01"),
#                       "%Y_%m_%d-%H_%M_%S")
#    session_index <- c(1, which(diff(ecg$datetimems/1000) > 5) + 1)
#    ecg_file_name <- rep("", length(session_index))
#  
#    # Export data of each session to separate csv files.
#    for (i in 1:length(session_index)) {
#      ecg_file_name[i] <- time_idx[session_index[i]]
#  
#      if (i == length(session_index)) {
#        write.table(
#          ecg[session_index[i]:dim(ecg)[1],],
#          file = paste(ecg_directory_name, "/", ecg_file_name[i], ".csv", sep=""),
#          sep = ",",
#          row.names = FALSE,
#          quote = FALSE
#        )
#      } else {
#        write.table(
#          ecg[session_index[i]:(session_index[i+1]-1),],
#          file = paste(ecg_directory_name, "/", ecg_file_name[i], ".csv", sep=""),
#          sep = ",",
#          row.names = FALSE,
#          quote = FALSE
#        )
#      }
#  
#    }
#  }

## ------------------------------------------------------------------------
library(zephyrECG)
NAME <- system.file("extdata", "myZephyrBH3Data.csv", package="zephyrECG")
str(NAME)

## ------------------------------------------------------------------------
ecg_directory_name <- dirname(NAME)
str(ecg_directory_name)

## ---- echo=FALSE, results='hide'-----------------------------------------
file.remove(dir(ecg_directory_name, full.names = TRUE, pattern = "[0-9].csv$"))

## ------------------------------------------------------------------------
list.files(ecg_directory_name)
ecg <- read.csv(NAME, stringsAsFactors=FALSE)
str(ecg)

## ------------------------------------------------------------------------
time_idx <- format(as.POSIXct(ecg$datetimems/1000, origin = "1970-01-01"), 
                   "%Y_%m_%d-%H_%M_%S")
str(time_idx)
session_index <- c(1, which(diff(ecg$datetimems/1000) > 60) + 1)
str(session_index)
str(time_idx[session_index])

## ---- width=90-----------------------------------------------------------
ecg_file_name <- rep("", length(session_index))

# Export data of each session to separate csv files.
for (i in 1:length(session_index)) {
  ecg_file_name[i] <- time_idx[session_index[i]]

  if (i == length(session_index)) {
    write.table(
      ecg[session_index[i]:dim(ecg)[1],], 
      file = paste(ecg_directory_name, "/", ecg_file_name[i], ".csv", sep=""), 
      sep = ",", 
      row.names = FALSE, 
      quote = FALSE
    )
  } else {
    write.table(
      ecg[session_index[i]:(session_index[i+1]-1),],
      file = paste(ecg_directory_name, "/", ecg_file_name[i], ".csv", sep=""),
      sep = ",", 
      row.names = FALSE, 
      quote = FALSE
    )
  }

}

list.files(ecg_directory_name)

## ---- eval=FALSE---------------------------------------------------------
#  read_ecg <- function(NAME="filename") {
#    ecg_file <- NAME
#    ecg <- read.csv(ecg_file, stringsAsFactors=FALSE)
#    if (length(ecg) == 2) {
#      names(ecg) <- c("datetimems", "measurement")
#    } else {
#      ecg <- ecg[, c(length(ecg) - 1, length(ecg))]
#      names(ecg) <- c("datetimems", "measurement")
#    }
#    return(ecg)
#  }

## ------------------------------------------------------------------------
library(zephyrECG)
NAME <- system.file("extdata", "myECGData.csv", package="zephyrECG")
str(NAME)

## ------------------------------------------------------------------------
ecg <- read.csv(NAME, stringsAsFactors=FALSE)

## ------------------------------------------------------------------------
if (length(ecg) == 2) {
  names(ecg) <- c("datetimems", "measurement")
} else {
  ecg <- ecg[, c(length(ecg) - 1, length(ecg))]
  names(ecg) <- c("datetimems", "measurement")
}

