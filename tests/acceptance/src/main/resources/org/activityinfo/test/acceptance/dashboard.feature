@web
Feature: Dashboard
  As a project stakeholder,
  I want to see selected reports on my dashboard
  So that I can see key results about my projects at a glance
  
  Background:
    Given I have created a database "RRMP"
    And I have added partner "NRC" to "RRMP"
    And I have created a form named "NFI Distribution" with the submissions:
      | Partner | Donor | Nb. kits  |
      | enum    | enum  | quantity  |
      | NRC     | USAID |  1000     |  
    And I have granted Alice permission to View on behalf of "NRC"
  
  Scenario: Adding my own report to the dashboard
    When I create a pivot table report aggregating the indicators Nb. kits by Partner
    And I save the report as "Key Performance Indicators"
    And I pin the report to my dashboard
    Then the report should be shown on my dashboard with:
      |                     | Value |
      | NRC                 | 1,000 |
    And the report should not be shown on the dashboard of "Alice"
    
  Scenario: Saving a report
    When I create a pivot table report aggregating the indicators Nb. kits by Partner
     And I save the report as "Key Performance Indicators"
    Then "Key Performance Indicators" should appear in my list of saved reports