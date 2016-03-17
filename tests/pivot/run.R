
# Runs the pivot tests on 

source("pivot-test.R")

dir.create("logs", showWarnings=F)

results <- profile.new.old()

write.csv(results, file = "results.csv")
