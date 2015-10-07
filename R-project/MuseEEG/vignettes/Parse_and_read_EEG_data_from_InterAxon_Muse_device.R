## ---- eval=FALSE---------------------------------------------------------
#  read_eeg <- function(NAME="filename")

## ------------------------------------------------------------------------
NAME <- system.file("extdata", "eeg_sample.csv", package="museEEG")
NAME

## ------------------------------------------------------------------------
muse_file <- NAME
muse <- read.csv(muse_file, header=FALSE, stringsAsFactors=FALSE)
str(muse, strict.width = "wrap", list.len = 20)

## ------------------------------------------------------------------------
muse_sub <- subset(muse, (muse$V2 == " /muse/elements/alpha_absolute") |
                     (muse$V2 == " /muse/elements/beta_absolute") | 
                     (muse$V2 == " /muse/eeg"))
str(muse_sub, strict.width = "wrap", list.len = 20)

## ------------------------------------------------------------------------
muse_sub <- muse_sub[,c(1:6)]
names(muse_sub) <- c("time", "type", "TP9", "FP1", "FP2", "TP10")
muse_sub[,1] <- as.numeric(muse_sub[,1])
muse_sub[,3] <- as.numeric(muse_sub[,3])
muse_sub[,4] <- as.numeric(muse_sub[,4])
muse_sub[,5] <- as.numeric(muse_sub[,5])
muse_sub[,6] <- as.numeric(muse_sub[,6])

str(muse_sub, strict.width = "wrap")

## ------------------------------------------------------------------------
muse_sub[which(muse_sub[,2] == " /muse/elements/alpha_absolute"),2] <- "alpha"
muse_sub[which(muse_sub[,2] == " /muse/elements/beta_absolute"),2] <- "beta"
muse_sub[which(muse_sub[,2] == " /muse/eeg"),2] <- "eeg"
muse_sub[,2] <- as.factor(muse_sub[,2]) 

str(muse_sub, strict.width = "wrap")

## ------------------------------------------------------------------------
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

str(muse_sub, strict.width = "wrap")

## ------------------------------------------------------------------------
f_sampling <- length(which(muse_sub$type == "eeg")) /
  (muse_sub$time[length(muse_sub$time)] - muse_sub$time[1])
f_sampling

test <- round(seq(muse_sub$time[1]*1000,
                  muse_sub$time[length(muse_sub$time)] * 1000,
                  by=1000/f_sampling))/1000 
str(test) 

muse_sub$time[which(muse_sub$type == "eeg")] <- test 
str(muse_sub)

## ---- fig.width=7, fig.height=5, fig.align='center'----------------------
library(ggplot2)
library(scales)
muse_sub$timecest <- as.POSIXct(muse_sub$time, origin = "1970-01-01")

ggplot(data=muse_sub[which(muse_sub$type == "eeg"),]) +
  geom_line(aes(x=timecest,y=FP1)) +
  xlab("Time [min:sec]") +
  ylab("FP1 channel") +
  scale_x_datetime(breaks=date_breaks(width = "10 sec"),
                            minor_breaks=date_breaks(width = "1 sec"),
                            labels=date_format("%M:%S")) +
  annotate("text",  x=max(muse_sub$timecest), y = Inf,
                    label = "FP1 channel", vjust=1, hjust=1)

ggplot(data=muse_sub[which(muse_sub$type == "eeg"),]) +
  geom_line(aes(x=timecest,y=FP2)) +
  xlab("Time [min:sec]") +
  ylab("FP2 channel") +
  scale_x_datetime(breaks=date_breaks(width = "10 sec"),
                            minor_breaks=date_breaks(width = "1 sec"),
                            labels=date_format("%M:%S")) +
  annotate("text",  x=max(muse_sub$timecest), y = Inf,
                    label = "FP2 channel", vjust=1, hjust=1)

## ---- eval=FALSE---------------------------------------------------------
#  return(muse_sub)

