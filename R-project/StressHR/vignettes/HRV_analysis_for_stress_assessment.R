## ---- eval=FALSE---------------------------------------------------------
#  hrv_analyze <- function(NAME = "Rsec_data.txt")

## ------------------------------------------------------------------------
library(RHRV)

hrv_data <- CreateHRVData()
hrv_data <- SetVerbose(hrv_data, TRUE)
str(hrv_data)

## ------------------------------------------------------------------------
NAME <- system.file("extdata", "Rsec_data.txt", package="stressHR")
hrv_data <- LoadBeatAscii(hrv_data, NAME, RecordPath = ".")
hrv_data <- BuildNIHR(hrv_data)

str(hrv_data)

## ---- fig.width=7, fig.height=5, fig.align='center'----------------------
# Filter the NIHR data to remove outliers
hrv_data <- FilterNIHR(hrv_data, maxbpm = 300)

# Manually remove outlier beats if any
hrv_data <- EditNIHR(hrv_data)

# Plot filtered NIHR
PlotNIHR(hrv_data)

## ------------------------------------------------------------------------
# Create equally spaced ECG time series for spectral analysis
hrv_data = RHRV::InterpolateNIHR (hrv_data, freqhr = 4)

## ---- fig.width=7, fig.height=5, fig.align='center'----------------------
PlotNIHR(hrv_data)
PlotHR(hrv_data)

## ------------------------------------------------------------------------
# Frequency domain analysis
hrv_data = RHRV::CreateFreqAnalysis(hrv_data)

## ------------------------------------------------------------------------
# Estimation of HR series duration in seconds
interval_estimate <- ceiling(hrv_data$Beat$Time[length(hrv_data$Beat$Time)])
interval_estimate

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

size

shift <- 1

hrv_data = RHRV::CalculatePowerBand(hrv_data,
                                    indexFreqAnalysis = 1,
                                    size, shift, type = "fourier" )

str(hrv_data)

## ---- eval=FALSE---------------------------------------------------------
#  return(hrv_data)

## ---- eval=FALSE---------------------------------------------------------
#  merge_hrv <- function(data, hrv_data)

## ------------------------------------------------------------------------
load(system.file("extdata", "ecg.Rda", package="stressHR"))
options(digits = 14)

head(ecg$time)
data <- ecg
data$lf_hf <- NA
str(data)
summary(data)

## ------------------------------------------------------------------------
t_beat <- match((1000*hrv_data$Beat$Time + data$time[1]), data$time)
str(t_beat)

## ------------------------------------------------------------------------
time_hrv <- seq(data$time[t_beat[1]], data$time[tail(t_beat,1)], by = 1000)
str(time_hrv)

## ------------------------------------------------------------------------
length(time_hrv) <- length(hrv_data$FreqAnalysis[[1]]$HRV)
str(time_hrv)

## ------------------------------------------------------------------------
time_data <- rep(0,length(time_hrv))
for (i in 1:length(time_data)) {
  time_data[i] <- tail(which((abs(data$time - time_hrv[i])) ==
                               min(abs(data$time - time_hrv[i]))),1)
}

## ------------------------------------------------------------------------
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

## ------------------------------------------------------------------------
str(hrv_data$FreqAnalysis[[1]][6])

## ---- eval=FALSE---------------------------------------------------------
#  function(m) rep(m[i], length(time_data[i] : (time_data[i + 1] - 1)))

## ------------------------------------------------------------------------
i <- 1 
test_one <- sapply(hrv_data$FreqAnalysis[[1]][6], 
       function(m) rep(m[i], length(time_data[i] : (time_data[i + 1] - 1))) )
str(test_one)

# Length of the output list
time_data[i + 1] - time_data[i]

## ------------------------------------------------------------------------
summary(data)

## ------------------------------------------------------------------------
stress <- classInt::classIntervals(data$lf_hf, n = 9, style = "equal")
data$stress <- classInt::findCols(stress)
data$stress <- as.factor(data$stress)

summary(data)

## ---- fig.width=7, fig.height=5, fig.align='center'----------------------
library(ggplot2)
library(RColorBrewer)

ggplot() +
  geom_line(data = data, aes(as.POSIXct(data$time/1000, origin = "1970-01-01"), ecg)) + 
  geom_point(data = data, aes(as.POSIXct(data$time/1000, origin = "1970-01-01"), ecg, color = stress), size = 1.5) + 
  scale_colour_manual(values = rev(brewer.pal(9,"Spectral"))) + 
  xlab("Time (CEST)") + 
  ylab("ECG [mV]")

