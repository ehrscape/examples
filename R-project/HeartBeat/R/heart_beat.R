#' Heart beat detector
#'
#' This function detects the R peaks from ECG data using wavelet decomposition.
#' The ECG data is provided in a data frame with two columns named \code{time} and \code{ecg.}
#'
#' @keywords ECG, heart beat, detection
#' @param data Data frame with ECG data.
#' @param SampleFreq The sampling frequency in Hz of ECG data. The default setting is 250 Hz.
#' @param thr Fixed threshold value used to detect heart beats from wavelet coefficients. The default value is 9.
#' @return
#'  The function returns a data object called "result" with 4 elements:
#'  \itemize{
#'   \item \code{signal}: the ECG data frame that was analyzed,
#'   \item \code{coeff}: the coefficients of the 2nd level wavelet decomposition of the ECG signal (\code{signal$ecg}),
#'   \item \code{R}: the indexes of R peaks in \code{coeff},
#'   \item \code{Rall}: the data frame with the heart-beat detection results with columns:
#'    \itemize{
#'    \item \code{Rtrue_idx}: the indexes of R peaks in \code{signal},
#'    \item \code{Rtrue_sec}: the time moments of \code{Rtrue_idx} in seconds,
#'    \item \code{RtoRext}: R-R intervals in number of samples (with the starting zero),
#'    \item \code{RtoR_secext}: R-R intervals in seconds (with the starting zero).
#'    }
#'  }
#'  The function also writes an ascii text file "Rsec_data.txt" to the current work directory, which contains the \code{Rtrue_sec} variable and is required as input for the HRV analysis.
#' @export
#' @examples
#' load(system.file("extdata", "ecg.Rda", package="heartBeat"))
#' result <- heart_beat(ecg)
#' result <- heart_beat(ecg, SampleFreq = 250)
#' result <- heart_beat(ecg, SampleFreq = 360,  thr = 15)

heart_beat <- function(data, SampleFreq = 250, thr = 9){

  # 4-level decomposition is used with the Daubechie d4 wavelet.
  wavelet <- "d4"
  level <- 4

  # If active detach packages RHRV and waveslim.

  if (!is.na(match('TRUE',search() == "package:RHRV"))) {
    detach("package:RHRV", unload=TRUE)
  }
  if (!is.na(match('TRUE',search() == "package:waveslim"))) {
    detach("package:waveslim", unload=TRUE)
  }

  df <- data.frame(matrix(NA, nrow = sum(!is.na(data$ecg)), ncol = 2))
  names(df) <- c("idx", "ecg")
  df$idx <- which(!is.na(data$ecg))
  df$ecg <- data$ecg[df$idx]

  X <- as.numeric(df$ecg)

  ecg_wav <- wavelets::dwt(X, filter=wavelet, n.levels=level,
                           boundary="periodic", fast=TRUE)

  # Coefficients of the second level of decomposition are used for R peak detection.
  x <- ecg_wav@W$W2

  # Empty vector for detected R peaks
  R <- matrix(0,1,length(x))

  # While loop for sweeping the L2 coeffs for local maxima.
  i <- 2
  while (i < length(x)-1) {
    if ((x[i]-x[i-1]>=0) && (x[i+1]-x[i]<0) && x[i]>thr) {
      R[i] <- i
    }
    i <- i+1
  }

  # Clear all zero values from R vector.
  R <- R[R!=0]

  # Scaling of results to the original signal.
  # Since L2 coeffs are used, the results have to me multiplied by 4.
  Rtrue <- R*4

  # Checking results on the original signal
  for (k in 1:length(Rtrue)){
    if (Rtrue[k] > 10){
      Rtrue[k] <- Rtrue[k]-10+(which.max(X[(Rtrue[k]-10):(Rtrue[k]+10)]))-1
    } else {
      Rtrue[k] <- which.max(X[1:(Rtrue[k]+10)])
    }
  }

  Rtrue <- unique(Rtrue)
  Rtrue_idx <- df$idx[Rtrue]

  # Determine R-R intervals in samples and seconds and average heart rate.
  RtoR <- Rtrue_idx[-1]-Rtrue_idx[1:length(Rtrue_idx)-1]
  RtoR_sec <- (data$time[Rtrue_idx[-1]] -
                 data$time[Rtrue_idx[1:length(Rtrue_idx)-1]])/1000

  avgHR = 60/mean(RtoR_sec)
  avgHR = as.integer(avgHR)

  # Write the information about detected R peaks to Rsec_data ascii file
  Rtrue_sec = (data$time[Rtrue_idx] - data$time[1])/1000;
  Rtrue_sec <- round(Rtrue_sec, 3)
  write(Rtrue_sec,"Rsec_data.txt", 1)

  RtoRext <- c(0,RtoR)
  RtoR_secext <- c(0,RtoR_sec)
  Rall <- data.frame(Rtrue_idx,Rtrue_sec,RtoRext,RtoR_secext)

  return(list(signal = df, coeff = x, R = R, Rall = Rall))

}
