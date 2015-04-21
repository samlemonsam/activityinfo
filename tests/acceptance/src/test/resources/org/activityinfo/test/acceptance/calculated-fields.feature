@web
Feature: Calculated fields

  @AI-991
  Scenario: Calculating Percentages.
    Given I have created a database "RRMP"
    And I have added partner "NRC" to "RRMP"
    And I have created a form named "NFI Distribution"
    And I have created a quantity field "a" in "NFI Distribution" with code "a"
    And I have created a quantity field "b" in "NFI Distribution" with code "b"
    And I have created a calculated field "c" in "NFI Distribution" with expression "{a}/{b}"
    When I submit a "NFI Distribution" form with:
      | field              | value           |
      | a                  | 1               |
      | b                  | 2               |
    Then the submission's detail shows:
      | field              | value           |
      | a                  | 1               |
      | b                  | 2               |
      | c                  | 0.5             |
    When I update the submission with:
      | field              | value           |
      | a                  | 1               |
      | b                  | 0               |
    Then the submission's detail shows:
      | field              | value           |
      | a                  | 1               |
      | c                  | ∞              |
    When I update the submission with:
      | field              | value           |
      | a                  | 0               |
      | b                  | 0               |
    Then the submission's detail shows:
      | field              | value           |
      | c                  | NaN             |