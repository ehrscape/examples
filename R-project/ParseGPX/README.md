Parsing GPX data to R data frame
========

The R package **parseGPX** was designed for reading and parsing of GPX files containing GPS data. 
GPS data has become broadly available by integrating low-cost GPS chips into portable consumer devices. 
Consequently, there is an abundance of online and offline tools for GPS data visualization and analysis with R project being in the focus in this example. 
The data itself can be generated in several different file formats, such as txt, csv, xml, kml, gpx. 
Among these the [GPX data format](http://www.topografix.com/gpx.asp) is ment to be the most universal intended for exchanging GPS data between programs, and for sharing GPS data with other users. 
Unlike many other data files, which can only be understood by the programs that created them, GPX files actually contain a description of what's inside them, allowing anyone to create a program that can read the data within. 
Several R packages already exist with functions for reading and parsing of GPX data files, e.g. `plotKML`, `maptools`, `rgdal` with corresponding functions `readGPX`, `readGPS` and `readOGR`.

The presented package **parseGPX** contains the function `parse_gpx` to read, parse and optionally save GPS data.

For more details on the usage of the package please refer to the included vignette. 
