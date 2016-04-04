

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

#' Queries databases that have targets defined
queryDatabases <- function(whereClause = "1") {
  
  mydb = dbConnect(MySQL(), user='root', password='root', dbname='activityinfo')
  dbs <- dbGetQuery(mydb, paste("SELECT d.name, d.databaseId id, u.email owner, d.owneruserid ownerId FROM userdatabase d",
                                    "LEFT JOIN userlogin u on (u.userid=d.owneruserid) ",
                                    "WHERE dateDeleted is null AND ", whereClause))
  
  dbDisconnect(mydb)
  
  dbs
}

withTargets <- "databaseId IN (SELECT databaseId FROM target)"


hasLinks <- paste(
    "databaseId IN ",
    "(SELECT DA.databaseId FROM indicatorlink K ",
      "LEFT JOIN indicator DI ON (DI.indicatorId = K.DestinationIndicatorId)",
      "LEFT JOIN activity DA ON (DA.activityId = DI.activityId))")

#' Creates a new request for a saved report
savedReport <- function(report.id, user.id) {
  if(missing(user.id)) {
    reports <- queryReports()
    user.id <- reports$ownerId[which(reports$id == report.id)]
  }
  list(path = "resources/testPivot/report", 
       name = sprintf("report%d", report.id),
       query.params = list(
         reportId = report.id,
         userId = user.id))
}

#' Samples a number of saved reports
sampleSavedReports <- function(n = 10) {
  reports <- queryReports()
  rows <- sample(x = nrow(reports), size = n)
  
  lapply(rows, function(row) savedReport(reports$id[row], reports$ownerId[row]))
}



#' Creates a new request for pivoting on a whole database
databasePivot <- function(database.id, user.id, ...) {
  
  if(missing(user.id)) {
    dbs <- queryDatabases()
    user.id <- dbs$ownerId[which(dbs$id == database.id)]
  }
  
  list(path = "resources/testPivot/database",
       name = sprintf("db%d-%s", database.id, paste(names(list(...)), collapse="-")),
       query.params = list(
         databaseId = database.id,
         userId = user.id,
         ...))
}

allDatabases <- function(n, where, ...) {
  dbs <- queryDatabases(whereClause = where)
  if(missing(n)) {
    rows <- 1:nrow(dbs)
  } else {
    rows <- sample(x = nrow(dbs), size = n)
  }
  lapply(rows, function(i) databasePivot(dbs$id[i], dbs$ownerId[i], ...))  
}


#' Execute pivot query against test endpoint
execute <- function(req, engine, ...) {
  
  url <- "http://localhost:8080/"
  
  timing <- system.time({ 
    response <- GET(url,
                    path = req$path,
                    query = c(req$query.params, 
                              list(`new` = identical(engine, "NEW")),
                              list(...)))
    
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
      error = content(response, "text", encoding = "UTF-8"),
      succeeded = FALSE))
  }
}


#' Profiles the new query engine, sampling from the list of all saved reports.
#' @return a data frame with performance and comparison data for each sampled report
checkCompare <- function(reqs) {
  
  if(!is.null(reqs$name)) {
    reqs <- list(reqs)
  }
  
  reports <- data.frame(row.names = 1:length(reqs))
  
  for(i in 1:length(reqs)) {
    req <- reqs[[i]]
    cat(sprintf("%s %s (%d)\n", req$name, "?", req$query.params$userId))
    old <- execute(req, engine = "OLD")
    new.cold <- execute(req, "NEW")
    new.warm <- execute(req, "NEW")
    reports[i, "name"] <- req$name
    reports[i, "old"] <- old$time
    reports[i, "new.cold"] <- new.cold$time
    reports[i, "new.warm"] <- new.warm$time
    
    if(!is.null(old$result) && !is.null(new.cold$result)) {
      matched <- compareResults(req$name, old$result, new.cold$result)
      if(identical(matched, FALSE)) {
        logDetails(req)
      }
      reports[i, "match"] <- matched
    }
  }
  reports
}

logDetails <- function(req, engine) {
  
  old <- execute(req, engine = "OLD", details=TRUE)
  nqe <- execute(req, engine = "NEW", details=TRUE)
  
  compareResults(paste(req$name, "details", sep="-"), old$result, nqe$result)
}


#' Compares the results of a saved report
#' 
#' Each saved report may contain zero or more pivot table / pivot chart queries,
#' so compare them each in turn
compareResults <- function(req.name, old, nqe) {
  if(!is.list(old) || !is.list(nqe)) {
    cat("OLD:\n")
    str(old)
    cat("NEW:\n")
    str(nqe)
    return(NA)
  }
  if(length(old) == 0) {
    return(NA)
  }
  
 tryCatch({
    matching <- sapply(1:length(old), function(i) {
      compareBuckets(req.name, i, old[[i]]$buckets, nqe[[i]]$buckets)
    })
    return(all(matching, na.rm=TRUE))
  }, error = function(e) {
    cat("  -> ERROR!\n")
    capture.output({ 
      print(e)
      cat("old:\n")
      str(old)
      cat("new:\n")
      str(nqe)
    }, file = sprintf("logs/%s.error.log", req.name))
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
compareBuckets <- function(req.name, report.index, old, nqe) {
  od <- as.data.frame.buckets(old, value.column = "old.value")
  nd <- as.data.frame.buckets(nqe, value.column = "new.value")
  
  # Workaround for peculiarty of old engine
  # Target dimension is ALWAYS included
  if(is.null(nd$Target) && all(od$Target == "REALIZED")) {
    od$Target <- NULL
  }
  
  # Logs the results for subsequent debugging if anything is wrong
  write.csv(od, file = sprintf("logs/%s-%d.old.csv", req.name, report.index))
  write.csv(nd, file = sprintf("logs/%s-%d.new.csv", req.name, report.index))
  
  old.dims <- names(od)[-1] 
  new.dims <- names(nd)[-1]    

  # If the dimensions are the same, we can compare values for values
  if(setequal(old.dims, new.dims)) {
    md <- merge(od, nd, by = unique(old.dims, new.dims), all = TRUE)
    md$diff <- md$old.value - md$new.value
    write.csv(md, file = sprintf("logs/%s-%d.merged.csv", req.name, report.index))
    
    matches <- !any(is.na(md$diff)) && all(abs(md$diff) < 0.001)
    if(!matches) {
      cat(sprintf("MISMATCH: %s-%d.csv\n", req.name, report.index))
    }
    return(matches)
    
  } else {
    return(FALSE)
  }
}

