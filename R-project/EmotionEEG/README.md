Emotional valence and arousal assessment based on EEG recordings
========

The R package **emotionEEG** uses the EEG data collected with the [InterAxon Muse device](http://www.choosemuse.com/) and assesses emotional valence and arousal based on asymmetry analysis. 
The EEG data prepared by the `museEEG` package contains EEG signal values in microvolts and alpha and beta absolute band powers and ratios in decibels. 
Emotional valence is calcluated based on the ratio of alpha band power between right and left EEG chanels FP2 and FP1. 
Emotional arousal is calculated based on the mean value of beta to alpha ratios of left and right EEG channels FP1 and FP2. 

For more details on the usage of the package please refer to the included vignette. 
