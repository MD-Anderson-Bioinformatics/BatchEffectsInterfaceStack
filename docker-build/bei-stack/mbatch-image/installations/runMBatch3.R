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

runStatus <- "success"
tryCatch({
  sourceDir <- file.path("/BEI/OUTPUT", jobID, "MBatch")
  archiveDir <- file.path("/BEI/OUTPUT", jobID, "MBatch", jobID)
  dataRunDir <- file.path("/BEI/OUTPUT", jobID, "MBatch")
  externalIndexPath <- file.path("/BEI/WEBSITE", paste(jobID, "json", sep="."))
  dataSetName <- jobID
  dataSetLabel <- "Job Id"
  buildSingleArchive(sourceDir, archiveDir, dataRunDir, externalIndexPath, dataSetName, dataSetLabel)
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
