library(MBatch)
library(MBatchUtils)
library(httr)

message("runMBatch1.R 2017-11-20-1200")

### received in commandArgs()
message(commandArgs())
beiURL <- NULL

message(paste("commandArgs():", commandArgs(), sep="", collapse="\n"))

for( myStr in commandArgs() )
{
	message("processing command arg:", myStr)
	if (length(grep("^-beiURL=", myStr))>0)
	{
		beiURL <- substring(myStr, nchar("-beiURL=")+1) 
		message("found argument ", beiURL)
	}
}

message("beiURL= '", beiURL, "'")
jobResponse <- GET(url=paste(beiURL, "/BatchEffectsInterface/JOBnext?jobType=MBATCH", sep=""))
message(jobResponse)
jobID <- content(jobResponse, "text")
message("beiURL response '", jobID, "'")

if ("none"==jobID)
{
  cat("none")
} else {
	message("jobID is '", jobID, "'")
	message("beiURL= '", beiURL, "'")
	jobResponse <- GET(url=paste(beiURL, "/BatchEffectsInterface/JOBupdate?jobId=", jobID, "&status=MBATCHRUN_RUNNING_WAIT", sep=""))
	message("beiURL JOBupdate response '", content(jobResponse, "text"), "'")
	cat(toString(jobID))
}