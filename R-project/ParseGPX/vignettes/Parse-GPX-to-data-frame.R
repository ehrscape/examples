## ---- eval=FALSE---------------------------------------------------------
#  parse_gpx <- function(NAME="filename", writeData = FALSE, timeZone = "GMT") {
#  
#    gpx_file <- NAME
#    # Extract file and directory names
#    file_name <- substr(basename(gpx_file), 1, nchar(basename(gpx_file))-4)
#    directory_name <- dirname(gpx_file)
#  
#    # Read data
#    gpx_data <- plotKML::readGPX(gpx.file = gpx_file,
#                                 metadata = FALSE,
#                                 bounds = FALSE,
#                                 waypoints = FALSE,
#                                 routes = FALSE)
#    gpx_data <- gpx_data[[4]][[1]][[1]]
#  
#    # Convert date-time string to time data.
#    gpxdatum <- as.POSIXct(gpx_data$time, format = "%Y-%m-%dT%H:%M:%SZ", tz="GMT")
#  
#    if (timeZone != "GMT") {
#      attributes(gpxdatum)$tzone <- timeZone
#    }
#  
#    # Add time data column to gpx_data data frame
#    gpx_data$tz_CEST <- gpxdatum
#    # gpx_data <- gpx_data[,c("lon", "lat", "ele", "time", "tz_CEST", "speed")]
#  
#    # Convert numbers to type numeric
#    if (sum("ele" == names(gpx_data)) > 0) {
#      gpx_data$ele <- as.numeric(gpx_data$ele)
#    }
#    if (sum("speed" == names(gpx_data)) > 0) {
#      gpx_data$speed <- as.numeric(gpx_data$speed)
#    }
#  
#    if (writeData == TRUE) {
#      # Export data to csv file
#      write.table(gpx_data,
#                  file = paste(directory_name, "/", file_name, ".csv", sep = ""),
#                  sep = ",", row.names = FALSE, quote = FALSE)
#    }
#  
#    return(gpx_data)
#  }

## ------------------------------------------------------------------------
NAME <- system.file("extdata", "myGPXData.gpx", package="parseGPX")
writeData <- FALSE
timeZone <- "Europe/Ljubljana"

## ---- tidy=TRUE, message=FALSE-------------------------------------------
library(plotKML)
gpx_data <- readGPX(gpx.file = NAME,
                               metadata = FALSE,
                               bounds = FALSE,
                               waypoints = FALSE,
                               routes = FALSE)

## ------------------------------------------------------------------------
str(gpx_data, strict.width = "wrap")

## ------------------------------------------------------------------------
gpx_data <- gpx_data[[4]][[1]][[1]]
str(gpx_data, strict.width = "wrap")

## ------------------------------------------------------------------------
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

str(gpx_data, strict.width = "wrap")

## ---- eval=FALSE---------------------------------------------------------
#  file_name <- substr(basename(NAME), 1, nchar(basename(NAME))-4)
#  directory_name <- dirname(NAME)
#  
#  if (writeData == TRUE) {
#    # Export data to csv file
#    write.table(gpx_data,
#                file = paste(directory_name, "/", file_name, ".csv", sep = ""),
#                sep = ",", row.names = FALSE, quote = FALSE)
#  }
#  
#  return(gpx_data)

