Parse and read EEG data from InterAxon Muse device
========

The R package **museEEG** was designed to parse and read the EEG data collected with the [InterAxon Muse device](http://www.choosemuse.com/). The device stores the acquired data directly in .muse format and the manufacturer offers a tool [MusePlayer](http://developer.choosemuse.com/research-tools/museplayer) that converts the .muse data to .csv format. 
The package is comprised of `read_eeg` function, which reads a file in csv format acquired by MUSE hardware and sorts the EEG data in it into a data frame.

For more details on the usage of the package please refer to the included vignette. 
