#' Determine heart rate distribution
#'
#' This function determines instant heart rates, their distribution and a basic histogram based on the heart-beat detection output of the function \code{\link{heart_beat}}.
#' @keywords instant heart rate, heart rate histogram
#' @param Rall data frame with information about (detected) heart beats with columns \itemize{\item \code{Rtrue_idx}: the indexes of R peaks in \code{signal}, \item \code{Rtrue_sec}: the time moments of \code{Rtrue_idx} in seconds, \item \code{RtoRext}: R-R intervals in number of samples (with the starting zero), \item \code{RtoR_secext}: R-R intervals in seconds (with the starting zero).},
#' @param signal vector of ECG signal data points,
#' @param HRrest numeric value of the monitored person's resting heart rate,
#' @param age numeric value of age of the monitored person's age
#' @param training a flag indicating, whether the monitored person was on trainning/exercise or not
#' @return \code{beat_matrix}: data frame with columns coresponding to heart rate intervals.
#'  In each column the indexes of all heart beats that belong to the certain interval are writen.
#'  The number of columns is determined by the function and the number of rows is determined by the interval with the most heart beats.
#'  All other columns are appropriately filled with \code{NA} values.
#' @export
#' @examples
#' load(system.file("extdata", "Rall.Rda", package="heartBeat"))
#' beat_matrix <- HRdistribution(Rall, data$ecg, 75, 30, training = FALSE)
#'
#' load(system.file("extdata", "ecg.Rda", package="heartBeat"))
#' result <- heart_beat(ecg)
#' beat_matrix <- HRdistribution(result$Rall, data$ecg, 75, 30, training = FALSE)

HRdistribution <- function(Rall, signal, HRrest, age, training = FALSE) {

  # Calculate sampling rate
  SampleFreq <- round(Rall$Rtrue_idx[2] / Rall$Rtrue_sec[2], -1)

  # Calculate instant heart rate IHR. The padded zero at the begining is left out.
  IHR <- 60 / Rall$RtoR_secext[-1]

  if (training == FALSE) {

    # Determine indexes of heart beats which are in certain limits - divide
    # beats into interval groups
    ind_sub_60 <- which(IHR < 60, arr.ind = TRUE)
    ind_60_70 <- which(IHR >= 60 & IHR < 70, arr.ind = TRUE)
    ind_70_80 <- which(IHR >= 70 & IHR < 80, arr.ind = TRUE)
    ind_80_90 <- which(IHR >= 80 & IHR < 90, arr.ind = TRUE)
    ind_90_100 <- which(IHR >= 90 & IHR < 100, arr.ind = TRUE)
    ind_100_110 <- which(IHR >= 100 & IHR < 110, arr.ind = TRUE)
    ind_110_120 <- which(IHR >= 110 & IHR < 120, arr.ind = TRUE)
    ind_120_130 <- which(IHR >= 120 & IHR < 130, arr.ind = TRUE)
    ind_130_140 <- which(IHR >= 130 & IHR < 140, arr.ind = TRUE)
    ind_140_150 <- which(IHR >= 140 & IHR < 150, arr.ind = TRUE)
    ind_150_160 <- which(IHR >= 150 & IHR < 160, arr.ind = TRUE)
    ind_160_170 <- which(IHR >= 160 & IHR < 170, arr.ind = TRUE)
    ind_170_180 <- which(IHR >= 170 & IHR < 180, arr.ind = TRUE)
    ind_180_190 <- which(IHR >= 180 & IHR < 190, arr.ind = TRUE)
    ind_190_200 <- which(IHR >= 190 & IHR < 200, arr.ind = TRUE)
    ind_above_200 <- which(IHR >= 200, arr.ind = TRUE)

    beats_count <- c(length(ind_sub_60), length(ind_60_70), length(ind_70_80),
                     length(ind_80_90), length(ind_90_100), length(ind_100_110),
                     length(ind_110_120), length(ind_120_130),
                     length(ind_130_140), length(ind_140_150),
                     length(ind_150_160), length(ind_160_170),
                     length(ind_170_180), length(ind_180_190),
                     length(ind_190_200), length(ind_above_200))

    graphics::barplot(beats_count, main = "HR Histogram",
                      xlab = "HR range", ylab = "Total",
                      names.arg = c("< 60","60-70","70-80","80-90","90-100",
                                  "100-110", "110-120", "120-130", "130-140",
                                  "140-150", "150-160", "160-170", "170-180",
                                  "180-190", "190-200", "> 200"),
                      border = "blue")

    beat_list <- list(ind_sub_60 = ind_sub_60, ind_60_70 = ind_60_70,
                      ind_70_80 = ind_70_80, ind_80_90 = ind_80_90,
                      ind_90_100 = ind_90_100, ind_100_110 = ind_100_110,
                      ind_110_120 = ind_110_120, ind_120_130 = ind_120_130,
                      ind_130_140 = ind_130_140, ind_140_150 = ind_140_150,
                      ind_150_160 = ind_150_160, ind_160_170 = ind_160_170,
                      ind_170_180 = ind_170_180, ind_180_190 = ind_180_190,
                      ind_190_200 = ind_190_200, ind_above_200 = ind_above_200)

    beat_data <- lapply(beat_list, '[', seq(max(beats_count)))
    beat_matrix <- data.frame(beat_data)

  }


  if (training == TRUE) {

    ## Determine maximum heart rate for the given age
    HRmax <- 205.8 - (0.685 * age)

    # Determine heart rate limits according to resting heart rate and
    # maximum heart rate - the so called training limits
    recovery_lim <- c(HRrest + ((HRmax - HRrest) * 0.6),
                      HRrest + ((HRmax - HRrest) * 0.7))
    aerobic_lim <- c(HRrest + ((HRmax - HRrest) * 0.7),
                     HRrest + ((HRmax - HRrest) * 0.8))
    anaerobic_lim <- c(HRrest + ((HRmax - HRrest) * 0.8),
                       HRrest + ((HRmax - HRrest) * 0.9))
    red_lim <- c(HRrest + ((HRmax - HRrest) * 0.9), HRrest + ((HRmax - HRrest) * 1))

    # Determine indexes or parts of signal where heart rate was in specific limits
    ind_recovery <- which(IHR >= recovery_lim[1] & IHR < recovery_lim[2],
                          arr.ind = TRUE)
    ind_aerobic <- which(IHR >= aerobic_lim[1] & IHR < aerobic_lim[2],
                         arr.ind = TRUE)
    ind_anaerobic <- which(IHR >= anaerobic_lim[1] & IHR < anaerobic_lim[2],
                           arr.ind = TRUE)
    ind_red <- which(IHR >= red_lim[1] & IHR < red_lim[2], arr.ind = TRUE)

    beats_count <- c(length(ind_recovery), length(ind_aerobic),
                        length(ind_anaerobic), length(ind_red))

    graphics::barplot(beats_count, main = "HR Histogram - Training",
                      xlab = "HR range", ylab = "Total",
                      names.arg = c("Recovery","Aerobic","Anaerobic","Red Line"),
                      border = "blue")

    beat_list <- list(ind_recovery = ind_recovery, ind_aerobic = ind_aerobic,
                      ind_anaerobic = ind_anaerobic, ind_red = ind_red)

    beat_data <- lapply(beat_list, '[', seq(max(beats_count)))
    beat_matrix <- data.frame(beat_data)
  }

  return(beat_matrix)

}
