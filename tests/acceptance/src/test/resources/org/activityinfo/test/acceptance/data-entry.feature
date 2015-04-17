@web
Feature: Data Entry
  As a database owner,
  I want to be able enter new submissions, change them, delete and track changes.

  @AI-991
  Scenario: Create/Update/Delete of form submission must work with calculated indicators
            (including the case of division on zero).
    Given I have created a database "RRMP"
    And I have added partner "NRC" to "RRMP"
    And I have created a form named "NFI Distribution"
    And I have created a quantity field "_a" in "NFI Distribution"
    And I have created a quantity field "_b" in "NFI Distribution"
    And I have created a calculated field "_c" in "NFI Distribution" with expression "{_a}/{_b}"
    When I submit a "NFI Distribution" form with:
      | field               | value           |
      | _a                  | 1               |
      | _b                  | 2               |
    Then the submission's detail shows:
      | field               | value           |
      | _a                  | 1               |
      | _b                  | 2               |
      | _c                  | 0.5             |
    When I update the submission with:
      | field               | value           |
      | _a                  | 1               |
      | _b                  | 0               |
    Then the submission's detail shows:
      | field               | value           |
      | _a                  | 1               |
      | _c                  | ∞              |
    When I update the submission with:
      | field               | value           |
      | _a                  | 0               |
      | _b                  | 0               |
    Then the submission's detail shows:
      | field               | value           |
      | _c                  | NaN             |