@web
Feature: New table (based on new form)

  Background:
    Given I have created a database "Patient Registration"
    And I have added partner "NRC" to "Patient Registration"
    And I have created a form "Patient Visits" using the new layout
    And I have created a form named "Medical Activities" with the submissions:
      | Number of patients | Donor   | Diseases treated this month    | Comments         | Partner |
      | quantity           | enum    | enum                           | text             | enum    |
      | 230                | USAID   | cholera, malaria, tuberculosis | no comment       | NRC     |
      | 51                 | ECHO    | cholera                        | cholera epidemic | NRC     |
      | 1                  | ECHO    | cholera                        | cholera          | NRC     |
      | 13                 | USAID   | cholera, malaria               | some comment     | NRC     |
      | 11                 | ECHO    | cholera                        | cholera epidemic | NRC     |

  @AI-1132
  Scenario: Updating enum values
    When edit entry in new table with field name "Number of patients" and value "11" in the database "Patient Registration" in the form "Medical Activities" with:
      | field       | value  | controlType |
      | Donor       | USAID  | radio       |
    Then table has rows with hidden built-in columns:
      | Number of patients | Donor   | Diseases treated this month  |
      | quantity           | enum    | enum                         |
      | 11                 | USAID   | cholera                      |

  @AI-835
  Scenario: Column filtering
    When open table for the "Medical Activities" form in the database "Patient Registration"
    And filter column "Number of patients" with:
      | 230 |
      | 1   |
    Then table has rows with hidden built-in columns:
      | 230 | USAID   | cholera, malaria, tuberculosis |
      | 1   | ECHO    | cholera                        |