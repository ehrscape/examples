#' Perform HRV analysis
#'
#' This function uses an ASCII file Rsec_data.txt (output of heart beat detector) as input for HRV analysis based on RHRV package.
#' @export
#' @keywords HRV analysis
#' @param NAME File name of input data as string (ASCII file containing information about consecutive heart beats in seconds).
#' @return
#' hrv_data: HRVData structure (RHRV package) containing data of HRV analysis.
#'
#' @examples
#' hrv_data <- hrv_analyze(system.file("extdata", "Rsec_data.txt", package="stressHR"))

hrv_analyze <- function(NAME = "Rsec_data.txt") {

  hrv.data <- RHRV::CreateHRVData()
  hrv.data <- RHRV::SetVerbose(hrv.data, TRUE)
  hrv.data <- RHRV::LoadBeatAscii(hrv.data, NAME, RecordPath = ".")
  hrv.data <- RHRV::BuildNIHR(hrv.data)

  # Filter the NIHR data to remove outliers
  hrv.data <- RHRV::FilterNIHR(hrv.data, maxbpm = 300)

  # Plot filtered NIHR
  RHRV::PlotNIHR(hrv.data)

  # Manually remove outlier beats if any
  hrv.data <- RHRV::EditNIHR(hrv.data)
  RHRV::PlotNIHR(hrv.data)

  # Create equally spaced ECG time series for spectral analysis
  hrv.data = RHRV::InterpolateNIHR (hrv.data, freqhr = 4)

  # Estimation of HR series duration in seconds
  interval_estimate <- ceiling(hrv.data$Beat$Time[length(hrv.data$Beat$Time)])

  # Frequency domain analysis
  hrv.data = RHRV::CreateFreqAnalysis(hrv.data)

  if (interval_estimate >= 1000) {
    size <- 300
  }
  if ((interval_estimate < 1000) && (interval_estimate >= 100)) {
    size <- 30
  }
  if ((interval_estimate < 100) && (interval_estimate >= 10)) {
    size <- 5
  }
  if (interval_estimate < 10) stop("The selected ECG interval is too short.")

  shift <- 1

  hrv.data = RHRV::CalculatePowerBand(hrv.data,
                                      indexFreqAnalysis = 1,
                                      size, shift, type = "fourier" )

  return(hrv.data)
}

