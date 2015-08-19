@web
Feature: Sharing Reports
  As a project stakeholder,
  I want to see selected reports on my dashboard
  So that I can see key results about my projects at a glance

  Background:
    Given I have created a database "NFI Cluster"
    And I have added partner "NRC" to "NFI Cluster"
    And I have created a form named "NFI Distribution" with the submissions:
      | Partner | Donor | Nb. kits  | Nb. households |
      | enum    | enum  | quantity  | quantity       |
      | NRC     | USAID |  1000     | 300            |
    And I have granted Alice permission to View on behalf of "NRC"
    And I have created a database "Education Cluster"
    And I have added partner "NRC" to "Education Cluster"
    And I have created a form named "School Rehabilitation" with the submissions:
      | Partner | Classrooms |
      | enum    | quantity   |
      | NRC     |          6 |
    And I have granted Bob permission to View on behalf of "NRC"

  Scenario: Saving a report
    When I create a pivot table report aggregating the indicators Nb. kits by Partner
    And I save the report as "Key Performance Indicators"
    Then "Key Performance Indicators" should appear in my list of saved reports
    And the report should not appear in Alice's list of saved reports

  Scenario: Adding my own report to the dashboard
    When I create a pivot table report aggregating the indicators Nb. kits by Partner
    And I save the report as "Key Performance Indicators"
    And I pin the report to my dashboard
    Then the report should be shown on my dashboard with:
      |                     | Value |
      | NRC                 | 1,000 |
    And the report should not be shown on the dashboard of "Alice"
    
  Scenario: Sharing a report 
    When I create a pivot table report aggregating the indicators Nb. kits and Classrooms by Partner
     And I save the report as "Intercluster Report"
     And I share the report with users of the "NFI Cluster" database
    Then the report should not appear in Bob's list of saved reports
    Then the report should appear in Alice's list of saved reports
    
  Scenario: Placing a report on users' dashboard by default
    When I create a pivot table report aggregating the indicators Classrooms by Partner
    And I save the report as "School Progress"
    And I share the report with users of the "Education Cluster" database as a default dashboard report
    Then the report should be shown on Bob's dashboard
