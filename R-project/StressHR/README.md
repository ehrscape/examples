Heart rate variability analysis for stress assessment
========

The R package **stressHR** assesses mental stress based on heart rate. 
Heart beats and heart rate are previously detected from single-lead ECG signal by using the `heartBeat` package.
The package includes functions

* `hrv_analyze`: executes the [heart rate variability (HRV)](https://en.wikipedia.org/wiki/Heart_rate_variability) on heart beat positions written in an ASCII file (`Rsec_data.txt`),
* `merge_hrv`: merges the HRV data with the initial ECG data frame.

For more details on the usage of the package please refer to the included vignette. 
