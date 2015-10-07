## ---- fig.width=7, fig.height=5, fig.align='center'----------------------
load(system.file("extdata", "ecg.Rda", package="heartBeat"))
str(ecg) 
library(ggplot2)
ggplot(data = ecg, aes(x = time, y = ecg)) + geom_line()

## ---- eval=FALSE---------------------------------------------------------
#  heart_beat <- function(data, SampleFreq = 250, thr = 9){...}

## ---- tidy=TRUE----------------------------------------------------------
SampleFreq <- 250
thr <- 9 

# 4-level decomposition is used with the Daubechie d4 wavelet.
wavelet <- "d4"
level <- 4L

# If active detach packages RHRV and waveslim.

if (!is.na(match('TRUE',search() == "package:RHRV"))) {
  detach("package:RHRV", unload=TRUE)
}
if (!is.na(match('TRUE',search() == "package:waveslim"))) {
  detach("package:waveslim", unload=TRUE)
}

df <- data.frame(matrix(NA, nrow = sum(!is.na(ecg$ecg)), ncol = 2))
names(df) <- c("idx", "ecg")
df$idx <- which(!is.na(ecg$ecg))
df$ecg <- ecg$ecg[df$idx]

str(df)

X <- as.numeric(df$ecg)

library(wavelets)
ecg_wav <- dwt(X, filter=wavelet, n.levels=level, boundary="periodic", fast=TRUE)
str(ecg_wav)

## ---- fig.width=7, fig.height=5, fig.align='center'----------------------
oldpar <- par(mfrow = c(2,2), mar = c(4,4,1.5,1.5) + 0.1)
plot(ecg_wav@W$W1, type = "l")
plot(ecg_wav@W$W2, type = "l")
plot(ecg_wav@W$W3, type = "l")
plot(ecg_wav@W$W4, type = "l")
par(oldpar)

## ---- fig.width=7, fig.height=5, fig.align='center'----------------------
oldpar <- par(mfrow = c(2,2), mar = c(4,4,1.5,1.5) + 0.1)
plot(ecg_wav@V$V1, type = "l")
plot(ecg_wav@V$V2, type = "l")
plot(ecg_wav@V$V3, type = "l")
plot(ecg_wav@V$V4, type = "l")
par(oldpar)

## ------------------------------------------------------------------------
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
str(R)

## ------------------------------------------------------------------------
Rtrue <- R*4
str(Rtrue)

## ------------------------------------------------------------------------
# Checking results on the original signal
for (k in 1:length(Rtrue)){
  if (Rtrue[k] > 10){
    Rtrue[k] <- Rtrue[k]-10+(which.max(X[(Rtrue[k]-10):(Rtrue[k]+10)]))-1
  } else {
    Rtrue[k] <- which.max(X[1:(Rtrue[k]+10)])
  }
}

Rtrue <- unique(Rtrue)
str(Rtrue)
Rtrue_idx <- df$idx[Rtrue]
str(Rtrue_idx)

## ---- tidy=TRUE, fig.width=7, fig.height=5, fig.align='center'-----------
# Determine R-R intervals in samples and seconds and average heart rate.
RtoR <- Rtrue_idx[-1]-Rtrue_idx[1:length(Rtrue_idx)-1]
str(RtoR)
RtoR_sec <- (ecg$time[Rtrue_idx[-1]] -
               ecg$time[Rtrue_idx[1:length(Rtrue_idx)-1]])/1000
str(RtoR_sec)

# Average heart rate of the input ECG signal
avgHR = 60/mean(RtoR_sec)
avgHR = as.integer(avgHR)
avgHR

# Plot the original signal together with results
require(ggplot2)
ggplot(data = ecg, aes(x = time, y = ecg)) + 
  geom_line() + 
  geom_point(data = ecg[Rtrue_idx,], aes(x = time, y = ecg), colour = "red", shape = 1, size = 3)


## ------------------------------------------------------------------------
# Write the information about detected R peaks to Rsec_data ascii file
Rtrue_sec = (ecg$time[Rtrue_idx] - ecg$time[1])/1000;
Rtrue_sec <- round(Rtrue_sec, 3)
Rtrue_sec
write(Rtrue_sec,"Rsec_data.txt", 1)

## ------------------------------------------------------------------------
RtoRext <- c(0,RtoR)
RtoR_secext <- c(0,RtoR_sec)
Rall <- data.frame(Rtrue_idx,Rtrue_sec,RtoRext,RtoR_secext)
str(Rall)

## ---- eval=FALSE---------------------------------------------------------
#  return(list(signal = df, coeff = x, R = R, Rall = Rall))

