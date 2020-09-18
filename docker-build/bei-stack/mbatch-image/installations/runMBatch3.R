# MBatch Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 University of Texas MD Anderson Cancer Center
#
# This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
# MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
# MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

library(MBatch)
library(MBatchUtils)
library(httr)

message("runMBatch3.R 2019-01-08-1316")

#jobID comes from commandArgs()
jobID <- NULL
message(paste("commandArgs():", commandArgs(), sep="", collapse="\n"))

for( myStr in commandArgs() )
{
  message("processing command arg:", myStr)
  if (length(grep("^-jobID=", myStr))>0)
  {
    jobID <- substring(myStr, nchar("-jobID=")+1) 
    message("found argument ", jobID)
  }
}

# buildSingleArchive(mbatchID, originalDataJsonFile, mbatchResultsDir, zipDir)
# String mbatchID = "MBATCH_ID_0000";
# String originalDataJsonFile = "/code/development/2020_03_12_1022/original_data.json";
# String mbatchResultsDir = "/code/development/2020_03_12_1022/MBatch/";
# String zipDir = "/code/development/2020_03_12_1022/";

runStatus <- "success"
tryCatch({
  mbatchID <- jobID
  originalDataJsonFile <- file.path("/BEI/OUTPUT", jobID, "original_data.json")
  mbatchResultsDir <- file.path("/BEI/OUTPUT", jobID, "MBatch", jobID)
  zipDir <- file.path("/BEI/OUTPUT", jobID)
  buildSingleArchive(mbatchID, originalDataJsonFile, mbatchResultsDir, zipDir)
}, warning = function(war){
  message(paste("runMBatch3.R hit the Warning: ", war))
  runStatus <- "warning"
  traceback()
}, error = function(err){
  message(paste("runMBatch3.R hit the error: ", err))
  runStatus <- "error"
  traceback()
}, finally = {
  cat(runStatus)
})
