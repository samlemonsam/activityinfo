@web
Feature: Subforms

  Background:
    Given I have created a database "Subforms"
    And I have added partner "NRC" to "Subforms"
    And I have created a form "NFI Distribution" using the new layout
    Given I open the form designer for "NFI Distribution" in database "Subforms"

  Scenario: Section container
    Given I open the form designer for "NFI Distribution" in database "Subforms"
    And drop field in:
      | label         | type    | container |
      | Section     | Section | root      |
      | Root Field    | Text    | root      |
      | Section Field | Text    | Section |
    And I submit a "NFI Distribution" form with:
      | field         | value        |
      | partner       | NRC          |
      | Root Field    | root test    |
      | Section Field | section test |
    Then table has rows with hidden built-in columns:
      | Root Field | Section Field |
      | text       | text          |
      | root test  | section test  |