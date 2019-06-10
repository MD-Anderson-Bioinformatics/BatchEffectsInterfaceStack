library(MBatch)
library(MBatchUtils)
library(httr)

message("runMBatch2.R 2017-11-29-1430")

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
  dir.create(file.path("/BEI/OUTPUT", jobID, "MBatch", jobID), showWarnings=FALSE, recursive=TRUE)
  setwd(file.path("/BEI/OUTPUT", jobID, "MBatch", jobID))
  mbatchDF <- readAsGenericDataframe(file.path("/BEI/OUTPUT/mbatch.tsv"))
  mbatchRunFromConfig(theConfigFile=file.path("/BEI/OUTPUT", jobID, "MBatchConfig.tsv"),
                      theOutputDir=file.path("/BEI/OUTPUT", jobID, "MBatch", jobID),
                      theNaStrings=c("null", "NA"),
					  theShaidyMapGen="/home/docker_tcga/mbatch/ShaidyMapGen.jar",
					  theShaidyMapGenJava="/usr/bin/java",
					  theNGCHMShaidyMem=mbatchDF$NGCHMShaidyMem, 
					  thePCAMem=mbatchDF$PCAMem, 
					  theBoxplotMem=mbatchDF$BoxplotMem)
}, warning = function(war){
  message(paste("runMBatch2.R hit the Warning: ", war))
  runStatus <- "warning"
  traceback()
}, error = function(err){
  message(paste("runMBatch2.R hit the error: ", err))
  runStatus <- "error"
  traceback()
}, finally = {
  cat(runStatus)
})
