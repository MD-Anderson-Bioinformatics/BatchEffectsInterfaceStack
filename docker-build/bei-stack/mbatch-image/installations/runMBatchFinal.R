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
  
message("runMBatchFinal.R 2017-11-20-1200")

#jobID and beiURL come from commandArgs()
jobID <- NULL
message(paste("commandArgs():", commandArgs(), sep="", collapse="\n"))

for( myStr in commandArgs() )
{
  message("processing command arg:", myStr)
  if (length(grep("^-jobID=", myStr))>0)
  {
    jobID <- substring(myStr, nchar("-jobID=")+1) 
    message("found argument ", jobID)
  }else if (length(grep("^-beiURL=", myStr))>0)
  {
    beiURL <- substring(myStr, nchar("-beiURL=")+1) 
    message("found argument ", beiURL)
  }
}

runStatus <- "success"
tryCatch({
	if (file.exists(file.path("/BEI/OUTPUT", jobID, "MBatch", jobID, "MBATCH_SUCCESS.txt")))
	{
	  jobResponse <- GET(url=paste(beiURL, "/BEI/BEI/JOBupdate?jobId=", jobID, "&status=MBATCHRUN_END_SUCCESS", sep=""))
	  message("beiURL JOBupdate response '", content(jobResponse, "text"), "'")
	} else {
	  file.create(file.path("/BEI/OUTPUT", jobID, "MBatch", jobID, "MBATCH_FAILED.txt"))
	  jobResponse <- GET(url=paste(beiURL, "/BEI/BEI/JOBupdate?jobId=", jobID, "&status=MBATCHRUN_END_FAILURE", sep=""))
	  message("beiURL JOBupdate response '", content(jobResponse, "text"), "'")
	}
}, warning = function(war){
  message(paste("runMBatchFinal.R hit the Warning: ", war))
  runStatus <- "warning"
  traceback()
}, error = function(err){
  message(paste("runMBatchFinal.R hit the error: ", err))
  runStatus <- "error"
  traceback()
}, finally = {
  cat(runStatus)
})


# if(file.exists(file.path(getwd(), "runMBatch.rLog")))
# {
#   file.copy(file.path(getwd(),"runMBatch.rLog"), 
#             file.path("/BEI/OUTPUT", jobID))
# }else
# {
#   message("Did not copy runMBatch.rLog to /BEI/OUTPUT/jobID because file not found")
# }
#
