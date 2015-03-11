@web
Feature: Offline Mode

Scenario: Enabling Offline mode
  Given my browser supports offline mode
  Given that I am logged in as "auto.qa@bedatadriven.com"
  Given offline mode is not enabled
  When I enable offline mode
  Then offline mode should be enabled
  
Scenario: Working offline with large databases
  Given that I am logged in as "auto.qa@bedatadriven.com"
  Given I have created 20 databases each containing 40 forms with 30 fields
  When I enable offline mode
  Then synchronization should complete successfully
  
  
  
  
  

