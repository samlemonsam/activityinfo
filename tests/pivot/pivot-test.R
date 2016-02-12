

# PIVOT TABLE TEST SCRIPT
#
# This test script iterates through ALL saved reports in a local database and then
# executes pivot tables against both the new and the old pivot table engines
# to verify that results have not changed.
#
# It also records response time in order to quantify the performance improvement,
# at least under controlled, single-user mode.


library(RMySQL)
library(httr)
library(RJSONIO)

#' Query the list of saved reports from a local copy of the activityinfo database
queryReports <- function() {
  
  mydb = dbConnect(MySQL(), user='root', password='root', dbname='activityinfo')
  reports <- dbGetQuery(mydb, paste("SELECT r.ReportTemplateId id, u.email owner, u.userid ownerId, r.title FROM reporttemplate r",
                                    "LEFT JOIN userlogin u ON (u.userid=r.ownerUserId)",
                                    "WHERE title is not null "))
  
  dbDisconnect(mydb)
  
  reports
}

#' Execute a pivot table query
execute <- function(reportId, userId, engine) {

  url <- "http://localhost:8080/resources/testPivot"
  
  timing <- system.time({ 
    response <- GET(url,
      query = list(reportId = reportId,
                   userId=userId,
                   `new` = identical(engine, "NEW")))
    
  })
  
  if(response$status_code == 200) {
    
    json <- content(response, "text", encoding = "UTF-8")
    
    return(list(
        time = as.double(timing["elapsed"]), 
        succeeded = TRUE,
        result = fromJSON(json)))
  } else {
    return(list(
      time = NA, 
      succeeded = FALSE))
  }
}

#' Profiles the new query engine, sampling from the list of all saved reports.
#' @return a data frame with performance and comparison data for each sampled report
profile.new.old <- function(n, report.id) {
  reports <- queryReports()
  if(!missing(report.id)) {
    rows <- which(reports$id %in% report.id)
  } else if(!missing(n)) {
    rows <- sample(x = nrow(reports), size = n)
  } else {
    rows <- 1:nrow(reports)
  }
  for(i in rows) {
    report <- reports[i, ]
    cat(sprintf("%d %s (%d) %s\n", report$id, report$owner, report$ownerId, report$title))
    old <- execute(report$id, report$ownerId, "OLD")
    new.cold <- execute(report$id, report$ownerId, "NEW")
    new.warm <- execute(report$id, report$ownerId, "NEW")
    reports[i, "old"] <- old$time
    reports[i, "new.cold"] <- new.cold$time
    reports[i, "new.warm"] <- new.warm$time
    
    if(!is.null(old$result) && !is.null(new.cold$result)) {
      reports[i, "match"] <- compareResults(report$id, old$result, new.cold$result)
    }
  }
  reports[rows,]
}

#' Compares the results of a saved report
#' 
#' Each saved report may contain zero or more pivot table / pivot chart queries,
#' so compare them each in turn
compareResults <- function(report.id, old, nqe) {
  stopifnot(is.list(old), is.list(nqe))
  if(length(old) == 0) {
    return(NA)
  }
  tryCatch({
    matching <- sapply(1:length(old), function(i) {
      compareBuckets( report.id, i, old[[i]]$buckets, nqe[[i]]$buckets)
    })
    return(all(matching))
  }, error = function(e) {
    cat("  -> ERROR!\n")
    capture.output({ 
      print(e)
      cat("old:\n")
      str(old)
      cat("new:\n")
      str(nqe)
    }, file = sprintf("logs/%d.error.log", report.id))
    return(NA)
  })
}

#' Convert a PivotResult object into an R dataframe
as.data.frame.buckets <- function(buckets, value.column = "value") {

  df <- list()
  
  # Extract the value from each bucket
  df[[value.column]] <- sapply(buckets, bucketValue)
  
  # Extract list of all unique dimensions present 
  # AdminLevel, Activity, Indicator, etc
  dims <- sort(unique(unlist(lapply(buckets, function(bucket) names(bucket$key)))))
 
  
  for(dim in dims) {
    dim.id <- sapply(buckets, dimId, dim)
    dim.label <- sapply(buckets, dimLabel, dim)
    if(!all(is.na(dim.id))) {
      df[[dim]] <- dim.id
      df[[paste(dim, "label", sep=".")]] <- dim.label
    } else if(!all(is.na(dim.label))) {
      df[[dim]] <- dim.label
    }
  }
  
  as.data.frame(df, stringsAsFactors = FALSE)
}

#' Extract the dimension value's id from a dimension key
#' For example, this might be an activity id, or an indicator id,
#' or a calendar month
dimId <- function(bucket, dim) {
  key <- bucket$key[[dim]]
  if(!is.list(key)) {
    NA
  } else if(identical(dim, "DateDimension.YEAR")) {
    key$year
    
  } else if(identical(dim, "DateDimension.MONTH")) {
    sprintf("%d-%02d", key$year, key$month)
  
  } else if(identical(dim, "DateDimension.QUARTER")) {
    sprintf("%dQ%d", key$year, key$quarter)
    
  } else if(identical(dim, "DateDimension.WEEK_MON")) {
    sprintf("%dW%d", key$year, key$week)

  } else if(is.null(key$id)) {
    NA
  } else {
    key$id
  }
}

dimLabel <- function(bucket, dim) {
  key <- bucket$key[[dim]]
  if(is.character(key)) {
    as.character(key[1])
  } else if(is.null(key$label)) {
    NA
  } else {
    key$label
  } 
}


bucketValue <- function(bucket) {
  if(bucket$aggregationMethod == 0) {
    if(is.numeric(bucket$sum)) {
      bucket$sum
    } else {
      NA
    }
  } else if(bucket$aggregationMethod == 1) {
    if(is.numeric(bucket$sum) && is.numeric(bucket$count)) {
      bucket$sum / bucket$count
    } else {
      NA
    }
  } else {
    if(is.numeric(bucket$count)) {
      bucket$count
    } else {
      NA
    }
  }
}

#' Compares the results of the old and new query engine 
#' 
#' Writes the results out to a 'logs' folder for later examination
#' 
#' @return TRUE if the results match exactly
compareBuckets <- function(report.id, report.index, old, nqe) {
  od <- as.data.frame.buckets(old, value.column = "old.value")
  nd <- as.data.frame.buckets(nqe, value.column = "new.value")
  
  # Workaround for peculiarty of old engine
  # Target dimension is ALWAYS included
  if(is.null(nd$Target) && all(od$Target == "REALIZED")) {
    od$Target <- NULL
  }
  
  # Logs the results for subsequent debugging if anything is wrong
  write.csv(od, file = sprintf("logs/%d-%d.old.csv", report.id, report.index))
  write.csv(nd, file = sprintf("logs/%d-%d.new.csv", report.id, report.index))
  
  old.dims <- names(od)[-1] 
  new.dims <- names(nd)[-1]    

  # If the dimensions are the same, we can compare values for values
  if(setequal(old.dims, new.dims)) {
    md <- merge(od, nd, by = unique(old.dims, new.dims), all = TRUE)
    md$diff <- md$old.value - md$new.value
    write.csv(md, file = sprintf("logs/%d-%d.merged.csv", report.id, report.index))
    
    matches <- identical(md$old.value, md$new.value)
    if(!matches) {
      cat(sprintf("MISMATCH: %d-%d.csv\n", report.id, report.index))
    }
    return(matches)
    
  } else {
    return(FALSE)
  }
}

