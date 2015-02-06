Feature: Indicator Targets
  As an analyst
  I want to be able to define target indicator values
  So I can measure actual results against our plans
  
  Background: 
    Given I have created a database "EMIS"
      And I have added partner "UNICEF" to "EMIS"
      And I have created a form named "School Attendance" in "EMIS"
      And I have created a quantity field "% enrolled" in "School Attendance"
      And I have submitted a "School Attendance" form with:
        | field       | value  |
        | partner     | UNICEF |
        | % enrolled  | 50%    |
    
  Scenario: 
    When I create a target for database "EMIS" with:
        | field       | value |
        | % enrolled  | 50%   |