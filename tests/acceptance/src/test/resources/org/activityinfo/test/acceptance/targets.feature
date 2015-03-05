# TODO: should support api as well, but we need to support pivot table requests through the API
@web  
Feature: Indicator Targets
  As an analyst
  I want to be able to define target indicator values
  So I can measure actual results against our plans
  
  Background: 
    Given I have created a database "RRMP"
      And I have added partner "CRS" to "RRMP"
      And I have added partner "NRC" to "RRMP"
      And I have created a form named "NFI Distribution" in "RRMP"
      And I have created a quantity field "nb. kits" in "NFI Distribution"
      And I have submitted a "NFI Distribution" form with:
        | field       | value  |
        | partner     | CRS    |
        | nb. kits    | 1000   |
      And I have submitted a "NFI Distribution" form with:
        | field       | value  |
        | partner     | NRC    |
        | nb. kits    | 800    |
    
  Scenario: Global target
    When I create a target with values:
      | field       | value |
      | nb. kits    | 3000  |
    Then aggregating the indicator "nb. kits" by Realized / Targeted should yield:
      |                     | Value |
      | Realized            | 1,800 |
      | Targeted            | 3,000 |
  
  Scenario: Partner target
    When I create a target for partner NRC with values:
      | field       | value |
      | nb. kits    | 1000  |
    And I create a target for partner CRS with values:
      | field       | value |
      | nb. kits    | 1500  |
    And aggregating the indicator "nb. kits" by Partner and Realized / Targeted should yield:
      |             | Value |
      | CRS         |       |
      |   Realized  | 1,000 |
      |   Targeted  | 1,500 |
      | NRC         |       |
      |   Realized  |   800 |
      |   Targeted  | 1,000 | 
