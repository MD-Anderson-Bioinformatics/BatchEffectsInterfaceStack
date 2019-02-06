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


tryCatch({
	if (file.exists(file.path("/BEI/OUTPUT", jobID, "MBatch", jobID, "MBATCH_SUCCESS.txt")))
	{
	  jobResponse <- GET(url=paste(beiURL, "/BatchEffectsInterface/JOBupdate?jobId=", jobID, "&status=MBATCHRUN_END_SUCCESS", sep=""))
	  message("beiURL JOBupdate response '", content(jobResponse, "text"), "'")
	} else {
	  file.create(file.path("/BEI/OUTPUT", jobID, "MBatch", jobID, "MBATCH_FAILED.txt"))
	  jobResponse <- GET(url=paste(beiURL, "/BatchEffectsInterface/JOBupdate?jobId=", jobID, "&status=MBATCHRUN_END_FAILURE", sep=""))
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