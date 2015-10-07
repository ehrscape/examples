Heart beat detection from single-lead ECG signal
========

The R package **heartBeat** was designed for heart beat detection from single-lead ECG signals. 
The ECG data is expected to be already loaded into a data frame and ready to use 
(for importing data recorded with Zephyr BioHarness 3 monitor, please see the package **zephyrECG**). 
The package includes functions

* `heart_beat`: detection of heart beats,
* `HRdistribution`: reads the signal and the output of `heart_beat` function and determines instant heart rates, their distribution and a basic histogram,
* `annotateHR`: adds factorized code to ECG data points according to heart rate determined previously with functions `heart_beat` and `HRdistribution`.

For more details on the usage of the package please refer to the included vignette. 
