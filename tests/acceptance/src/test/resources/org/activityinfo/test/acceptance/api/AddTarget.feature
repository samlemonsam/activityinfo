Feature: AddTarget Command

  Background:
    Given I have created a database "EMIS"
    And I have added partner "UNICEF" to "EMIS"
    And I have created a form named "School Attendance" in "EMIS"
    And I have created a quantity field "% enrolled" in "School Attendance"
    And I have submitted a "School Attendance" form with:
      | field       | value  |
      | partner     | UNICEF |
      | % enrolled  | 50%    |
    
    
  Scenario: Create a global target
    When I post the command
    """
    {
      "type": "AddTarget"
      "target": {
    
      
      }
    }
    
    """