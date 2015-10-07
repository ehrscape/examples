#' Read Muse EEG data file
#'
#' This function reads raw EEG data in csv format acquired by MUSE hardware and sorts it into a data frame.
#' The input parameter is the string path to the selected EEG file (csv format).
#' Function returns the data frame with EEG data.
#' The appropriate file must be converted from .muse format to .csv format using muse.player.
#'
#' @keywords EEG data, read, Muse
#' @param NAME The input parameter is the string path to the selected ECG file (csv format).
#' @return Function returns the eeg data frame.
#'  The returned EEG data frame contains the following columns:
#'    - time: vector of unix timestamps
#'    - type: indication of wether the signal is EEG, or only alpha or beta band
#'    - TP9, FP1, FP2, TP10: EEG channels
#'    - fp1_fp2: ratio of alpha and beta band data of channels FP1 and FP2 (in decibels).
#'    - fp1_beta_alpha: ratio between beta and alpha band power of channel FP1 (in decibels).
#'    - fp2_beta_alpha: ratio between beta and alpha band power of channel FP2 (in decibels).
#' @export
#' @examples
#' eeg <- read_eeg(system.file("extdata", "eeg_sample.csv", package="museEEG"))

read_eeg <- function(NAME="filename") {

  muse_file <- NAME

  muse <- read.csv(muse_file, header=FALSE, stringsAsFactors=FALSE)
  muse_sub <- subset(muse, (muse$V2 == " /muse/elements/alpha_absolute") |
                       (muse$V2 == " /muse/elements/beta_absolute") | (muse$V2 == " /muse/eeg"))
  muse_sub <- muse_sub[,c(1:6)]
  names(muse_sub) <- c("time", "type", "TP9", "FP1", "FP2", "TP10")
  muse_sub[,1] <- as.numeric(muse_sub[,1])
  muse_sub[,3] <- as.numeric(muse_sub[,3])
  muse_sub[,4] <- as.numeric(muse_sub[,4])
  muse_sub[,5] <- as.numeric(muse_sub[,5])
  muse_sub[,6] <- as.numeric(muse_sub[,6])

  muse_sub[which(muse_sub[,2] == " /muse/elements/alpha_absolute"),2] <- "alpha"
  muse_sub[which(muse_sub[,2] == " /muse/elements/beta_absolute"),2] <- "beta"
  muse_sub[which(muse_sub[,2] == " /muse/eeg"),2] <- "eeg"
  muse_sub[,2] <- as.factor(muse_sub[,2])

  # Values of FP1 and FP2 alpha and beta bands are given in Bels [B]. Therefore,
  # their ratios in dB units are obtained by and multiplying the difference by 10.

  muse_sub$fp2_fp1 <- NA
  muse_sub$fp2_fp1[which(muse_sub[,2] == "alpha")] <-
    10 * (muse_sub[which(muse_sub[,2] == "alpha"),5] -
            muse_sub[which(muse_sub[,2] == "alpha"),4])
  muse_sub$fp2_fp1[which(muse_sub[,2] == "beta")] <-
    10 * (muse_sub[which(muse_sub[,2] == "beta"),5] -
            muse_sub[which(muse_sub[,2] == "beta"),4])

  muse_sub$fp1_beta_alpha <- NA
  muse_sub$fp1_beta_alpha[which(muse_sub[,2] == "beta")] <-
    10 * (muse_sub[which(muse_sub[,2] == "beta"),4] -
            muse_sub[which(muse_sub[,2] == "alpha"),4])

  muse_sub$fp2_beta_alpha <- NA
  muse_sub$fp2_beta_alpha[which(muse_sub[,2] == "beta")] <-
    10 * (muse_sub[which(muse_sub[,2] == "beta"),5] -
            muse_sub[which(muse_sub[,2] == "alpha"),5])


  # Correction of timing. Corrects the fact that several EEG samples share
  # the same timestamps. After this correction the time vector of the
  # EEG signal is a proper time series.
  f_sampling <- length(which(muse_sub$type == "eeg")) /
    (muse_sub$time[length(muse_sub$time)] - muse_sub$time[1])
  test <- round(seq(muse_sub$time[1]*1000,
                    muse_sub$time[length(muse_sub$time)] * 1000,
                    by=1000/f_sampling))/1000
  muse_sub$time[which(muse_sub$type == "eeg")] <- test[1:length(which(muse_sub$type == "eeg"))]

  return(muse_sub)
}
