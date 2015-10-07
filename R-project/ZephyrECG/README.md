Parse and read Zephyr BH3 ECG data
========

The R package **zephyrECG** was designed to parse ECG data acquired with the Zephyr BioHarness 3 (BH3) monitor and how to import it into R. 
The package includes functions

* `separate_bh3`:  parses and separates multiple sessions of ECG data recorded with the Zephyr BH3 monitor into separate csv files,
* `read_ecg`: reads the signal and the output of `heart_beat` function and determines instant heart rates, their distribution and a basic histogram.

For more details on the usage of the package please refer to the included vignette. 
