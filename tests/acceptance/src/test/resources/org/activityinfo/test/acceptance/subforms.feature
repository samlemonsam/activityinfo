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
      | MySection     | Section | root      |
      | Root Field    | Text    | root      |
      | Section Field | Text    | MySection |
    And I submit a "NFI Distribution" form with:
      | field         | value        |
      | partner       | NRC          |
      | Root Field    | root test    |
      | Section Field | section test |
    And open table for the "NFI Distribution" form in the database "Subforms"
    Then table has rows with hidden built-in columns:
      | Root Field | Section Field |
      | text       | text          |
      | root test  | section test  |

  Scenario: Repeating subform
    Given I open the form designer for "NFI Distribution" in database "Subforms"
    And drop field in:
      | label               | type     | container           |
      | Root Field          | Text     | root                |
      | Repeating subform 1 | Sub Form | root                |
      | Repeating subform 2 | Sub Form | root                |
      | Subform Field 1     | Text     | Repeating subform 1 |
      | Subform Field 2     | Quantity | Repeating subform 1 |
      | Subform Field 3     | Text     | Repeating subform 2 |
      | Subform Field 4     | Quantity | Repeating subform 2 |
    And set "Repeating subform 1" subform to "Repeating"
    And set "Repeating subform 2" subform to "Repeating"
    And open table for the "NFI Distribution" form in the database "Subforms"
    And I open a new form submission on table page
    And I enter "Repeating subform 1" repeating subform values:
      | Subform Field 1 | Subform Field 2 |
      | text            | quantity        |
      | value 1         | 1               |
      | value 2         | 2               |
    And I enter "Repeating subform 2" repeating subform values:
      | Subform Field 3 | Subform Field 4 |
      | text            | quantity        |
      | value 3         | 3               |
      | value 4         | 4               |
    And I enter values:
      | Partner | Start Date | End Date   | Root Field       |
      | radio   | date       | date       | text             |
      | NRC     | 2016-02-10 | 2016-02-10 | root_field_value |
    And I save submission
    Then table has rows with hidden built-in columns:
      | Root Field       |
      | text             |
      | root_field_value |
    And open edit dialog for entry in new table with field value "root_field_value"
    Then opened form has repeating subform values:
      | Subform Field 1 | Subform Field 2 |
      | text            | quantity        |
      | value 1         | 1               |
      | value 2         | 2               |
    Then opened form has repeating subform values:
      | Subform Field 3 | Subform Field 4 |
      | text            | quantity        |
      | value 3         | 3               |
      | value 4         | 4               |
    And delete item 1 of "Repeating subform 1" repeating subform
    And delete item 2 of "Repeating subform 2" repeating subform
    And I save submission
    And open edit dialog for entry in new table with field value "root_field_value"
    Then opened form has repeating subform values:
      | Subform Field 1 | Subform Field 2 |
      | text            | quantity        |
      | value 2         | 2               |
    Then opened form has repeating subform values:
      | Subform Field 3 | Subform Field 4 |
      | text            | quantity        |
      | value 3         | 3               |