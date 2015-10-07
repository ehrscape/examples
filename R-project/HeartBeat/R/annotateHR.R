#' Annotate data with heart rate
#'
#' This function adds factorized code to data points according to heart rate.
#' @keywords heart rate, annotate
#' @param Rall data frame with information about (detected) heart beats with columns \itemize{\item \code{Rtrue_idx}: the indexes of R peaks in \code{signal}, \item \code{Rtrue_sec}: the time moments of \code{Rtrue_idx} in seconds, \item \code{RtoRext}: R-R intervals in number of samples (with the starting zero), \item \code{RtoR_secext}: R-R intervals in seconds (with the starting zero).},
#' @param beat_matrix output of the \code{\link{HRdistribution}} function containing distribution of heart rate values,
#' @param data data frame containing ECG, EEG and GPS measurements
#' @return \code{data}: original data frame complemented with heart rate color code (for plotting).
#' @export
#' @examples
#' load(system.file("extdata", "ecg.Rda", package="heartBeat"))
#' load(system.file("extdata", "Rall.Rda", package="heartBeat"))
#' load(system.file("extdata", "beat_matrix.Rda", package="heartBeat"))
#' ecg <- annotateHR(Rall, beat_matrix, ecg)
#'
#' load(system.file("extdata", "ecg.Rda", package="heartBeat"))
#' result <- heart_beat(ecg)
#' beat_matrix <- HRdistribution(result$Rall, data$ecg, 75, 30, training = FALSE)
#' ecg <- annotateHR(Rall, beat_matrix, ecg)


annotateHR <- function(Rall, beat_matrix, data) {

  if (names(beat_matrix)[1] != "ind_recovery") {
    # Create new column in data for color code factor values.
    data$heartRate <- NA
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_sub_60]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_sub_60])],
                     Rall$Rtrue_idx[beat_matrix$ind_sub_60 + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_sub_60])] - 1,
                     SIMPLIFY = FALSE)
              )] <- "sub 60" #"aquamarine"
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_60_70]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_60_70])],
                     Rall$Rtrue_idx[beat_matrix$ind_60_70 + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_60_70])] - 1,
                     SIMPLIFY = FALSE)
              )] <- "60-70" #"green"
    data$heartRate[
      do.call("c",
              mapply( seq,
                      Rall$Rtrue_idx[beat_matrix$ind_70_80]
                      [!is.na(Rall$Rtrue_idx[beat_matrix$ind_70_80])],
                      Rall$Rtrue_idx[beat_matrix$ind_70_80 + 1]
                      [!is.na(Rall$Rtrue_idx[beat_matrix$ind_70_80])] - 1,
                      SIMPLIFY = FALSE)
              )] <- "70-80" #"green"
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_80_90]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_80_90])],
                     Rall$Rtrue_idx[beat_matrix$ind_80_90 + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_80_90])] - 1,
                     SIMPLIFY = FALSE)
              )] <- "80-90" #"yellowgreen"
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_90_100]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_90_100])],
                     Rall$Rtrue_idx[beat_matrix$ind_90_100 + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_90_100])] - 1,
                     SIMPLIFY = FALSE)
              )] <- "90-100" #"yellowgreen"
    data$heartRate[
      do.call("c",
              mapply( seq,
                      Rall$Rtrue_idx[beat_matrix$ind_100_110]
                      [!is.na(Rall$Rtrue_idx[beat_matrix$ind_100_110])],
                      Rall$Rtrue_idx[beat_matrix$ind_100_110 + 1]
                      [!is.na(Rall$Rtrue_idx[beat_matrix$ind_100_110])] - 1,
                      SIMPLIFY = FALSE)
              )] <- "100-110" #"yellow"
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_110_120]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_110_120])],
                     Rall$Rtrue_idx[beat_matrix$ind_110_120 + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_110_120])] - 1,
                     SIMPLIFY = FALSE)
              )] <- "110-120" #"yellow"
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_120_130]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_120_130])],
                     Rall$Rtrue_idx[beat_matrix$ind_120_130 + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_120_130])] - 1,
                     SIMPLIFY = FALSE)
              )] <- "120-130" #"gold"
    data$heartRate[
      do.call("c",
              mapply( seq,
                      Rall$Rtrue_idx[beat_matrix$ind_130_140]
                      [!is.na(Rall$Rtrue_idx[beat_matrix$ind_130_140])],
                      Rall$Rtrue_idx[beat_matrix$ind_130_140 + 1]
                      [!is.na(Rall$Rtrue_idx[beat_matrix$ind_130_140])] - 1,
                      SIMPLIFY = FALSE)
              )] <- "130-140" #"orange"
    data$heartRate[
      do.call("c",
              mapply( seq,
                      Rall$Rtrue_idx[beat_matrix$ind_140_150]
                      [!is.na(Rall$Rtrue_idx[beat_matrix$ind_140_150])],
                      Rall$Rtrue_idx[beat_matrix$ind_140_150 + 1]
                      [!is.na(Rall$Rtrue_idx[beat_matrix$ind_140_150])] - 1,
                      SIMPLIFY = FALSE))] <- "140-150" #"orange"
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_150_160]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_150_160])],
                     Rall$Rtrue_idx[beat_matrix$ind_150_160 + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_150_160])] - 1,
                     SIMPLIFY = FALSE)
              )] <- "150-160" #"darkorange"
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_160_170]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_160_170])],
                     Rall$Rtrue_idx[beat_matrix$ind_160_170 + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_160_170])] - 1,
                     SIMPLIFY = FALSE)
              )] <- "160-170" #"darkorange2"
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_170_180]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_170_180])],
                     Rall$Rtrue_idx[beat_matrix$ind_170_180 + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_170_180])] - 1,
                     SIMPLIFY = FALSE))] <- "170-180" #"orangered2"
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_180_190]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_180_190])],
                     Rall$Rtrue_idx[beat_matrix$ind_180_190 + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_180_190])] - 1,
                     SIMPLIFY = FALSE)
              )] <- "180-190" #"orangered3"
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_190_200]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_190_200])],
                     Rall$Rtrue_idx[beat_matrix$ind_190_200 + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_190_200])] - 1,
                     SIMPLIFY = FALSE)
              )] <- "190-200" #"red"
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_above_200]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_above_200])],
                     Rall$Rtrue_idx[beat_matrix$ind_above_200 + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_above_200])] - 1,
                     SIMPLIFY = FALSE)
              )] <- "above 200" #"red"

    # Create factors
    data$heartRate <- factor(data$heartRate, exclude = NA)
    # Reorder factor levels
    data$heartRate <- factor(data$heartRate,
                             levels = c("sub 60","60-70","70-80","80-90",
                                        "90-100","100-110","110-120","120-130",
                                        "130-140","140-150","150-160","160-170",
                                        "170-180","180-190","above 200"))

    data$ihr <- NA
    IHR <- round(60/Rall$RtoR_secext[-1])

    for (idx in 1:length(IHR)) {
      if (idx < length(IHR)){
        data$ihr[Rall$Rtrue_idx[idx]:(Rall$Rtrue_idx[idx+1]-1)] <- IHR[idx]
      } else {
        data$ihr[Rall$Rtrue_idx[idx]:length(data$ecg)] <- IHR[idx]
      }
    }

  } else {

    data$heartRate <- NA
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_recovery]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_recovery])],
                     Rall$Rtrue_idx[beat_matrix$ind_recovery + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_recovery])] - 1,
                     SIMPLIFY = FALSE)
              )] <- "recovery" #"green"
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_aerobic]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_aerobic])],
                     Rall$Rtrue_idx[beat_matrix$ind_aerobic + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_aerobic])] - 1,
                     SIMPLIFY = FALSE)
              )] <- "aerobic" #"yellow"
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_anaerobic]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_anaerobic])],
                     Rall$Rtrue_idx[beat_matrix$ind_anaerobic + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_anaerobic])] - 1,
                     SIMPLIFY = FALSE)
              )] <- "anaerobic" #"orange"
    data$heartRate[
      do.call("c",
              mapply(seq,
                     Rall$Rtrue_idx[beat_matrix$ind_red]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_red])],
                     Rall$Rtrue_idx[beat_matrix$ind_red + 1]
                     [!is.na(Rall$Rtrue_idx[beat_matrix$ind_red])] - 1,
                     SIMPLIFY = FALSE)
              )] <- "red" #"red"

    # Create factors
    data$heartRate <- factor(data$heartRate, exclude = NA)
    # Reorder factor levels
    data$heartRate <- factor(data$heartRate,
                             levels = c("recovery","aerobic","anaerobic","red"))

    data$ihr <- NA
    IHR <- round(60/Rall$RtoR_secext[-1])

    for (idx in 1:length(IHR)) {
      if (idx < length(IHR)){
        data$ihr[Rall$Rtrue_idx[idx]:(Rall$Rtrue_idx[idx+1]-1)] <- IHR[idx]
      } else {
        data$ihr[Rall$Rtrue_idx[idx]:length(data$ecg)] <- IHR[idx]
      }
    }

  }


  return(data)

}
