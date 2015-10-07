#' Calculate stress
#'
#' Function merges data (containing ECG, EEG and GPS values) and hrv_data (containing results of the HRV analysis), which are the input parameters.
#' Adds columns LF/HF and stress to data.
#' @keywords stress calculation, ECG, HRV
#' @param data Data frame containing ECG, EEG, GPS and heart rate data.
#' @param hrv_data HRVData structure (RHRV package) containing data of HRV analysis.
#' @return
#' Output of the function is the modified data frame "data" with new columns HRV, LF/HF and stress added.
#' @export
#' @examples
#' load(system.file("extdata", "ecg.Rda", package="stressHR"))
#' hrv_data <- hrv_analyze(system.file("extdata", "Rsec_data.txt", package="stressHR"))
#' ecg <- merge_hrv(ecg, hrv_data)
#'

merge_hrv <- function(data, hrv_data) {

  # Create columns containing NA values
  data$lf_hf <- NA

  # Extract indexes of beats from the original data frame (t_beat).
  # hrv_data is a data structure resulting from HRV analysis.
  # data$time[1] is the first time stamp (start time).

  t_beat <- match((1000*hrv_data$Beat$Time + data$time[1]), data$time)

  # time_hrv is the time vector used in HRV analysis.
  # It is a series of unix time stamps (in milliseconds).
  time_hrv <- seq(data$time[t_beat[1]], data$time[tail(t_beat,1)], by = 1000)

  # Length of the time vector has to match the number of elements returned by
  # HRV analysis. The last "n" seconds are discarded, where "n" is equal to the
  # size of the time window used in HRV analysis.
  length(time_hrv) <- length(hrv_data$FreqAnalysis[[1]]$LFHF)

  # Locate timestamps in "data" which correspond to time_hrv.
  time_data <- rep(0, length(time_hrv))
  for (i in 1:length(time_data)) {
    time_data[i] <- tail(which((abs(data$time - time_hrv[i])) ==
                                 min(abs(data$time - time_hrv[i]))), 1)
  }

  # Appropriately fill in column lf_hf.
  for (i in 1:length(time_data)) {
    if (i == length(time_data)) {
      data[time_data[i] : (time_data[i] + 500), "lf_hf"] <-
        round(sapply(hrv_data$FreqAnalysis[[1]][6],
                     function(m)
                       rep(m[i], length(time_data[i] : (time_data[i] + 500))) ), 3)
    } else {
      data[time_data[i] : (time_data[i + 1] - 1), "lf_hf"] <-
        round(sapply(hrv_data$FreqAnalysis[[1]][6],
                     function(m)
                       rep(m[i], length(time_data[i] : (time_data[i + 1] - 1))) ), 3)
    }
  }

  stress <- classInt::classIntervals(data$lf_hf, n = 9, style = "equal")
  data$stress <- classInt::findCols(stress)
  data$stress <- as.factor(data$stress)

  return(data)

}
