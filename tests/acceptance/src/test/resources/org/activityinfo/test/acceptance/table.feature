@web
Feature: New table (based on new form)

  Background:
    Given I have created a database "Patient Registration"
    And I have added partner "NRC" to "Patient Registration"
    And I have created a form "Patient Visits" using the new layout
    And I have created a form named "Medical Activities" with the submissions:
      | Number of patients | Donor   | Diseases treated this month    | Comments         | Partner | start date | end date   |
      | quantity           | enum    | enum                           | text             | enum    | date       | date       |
      | 230                | USAID   | cholera, malaria, tuberculosis | no comment       | NRC     | 2014-01-02 | 2014-01-04 |
      | 51                 | ECHO    | cholera                        | cholera epidemic | NRC     | 2014-05-11 | 2014-05-15 |
      | 1                  | ECHO    | cholera                        | cholera          | NRC     | 2014-04-14 | 2014-04-24 |
      | 13                 | USAID   | cholera, malaria               | some comment     | NRC     | 2015-02-07 | 2015-03-07 |
      | 11                 | ECHO    | cholera                        | cholera epidemic | NRC     | 2015-07-07 | 2015-08-17 |

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
  Scenario: Column filtering by items
    When open table for the "Medical Activities" form in the database "Patient Registration"
    And filter column "Number of patients" with:
      | 230 |
      | 1   |
    Then table has rows with hidden built-in columns:
      | Number of patients | Donor   | Diseases treated this month    |
      | quantity           | enum    | enum                           |
      | 230                | USAID   | cholera, malaria, tuberculosis |
      | 1                  | ECHO    | cholera                        |

  @AI-1102
  Scenario: Column filtering by date
    When open table for the "Medical Activities" form in the database "Patient Registration"
    And filter date column "Start Date" with start date "2014-01-01" and end date "2014-12-31":
    Then table has rows:
      | Number of patients | Donor | Diseases treated this month    | Comments         | Partner | start date | end date   |
      | quantity           | enum  | enum                           | text             | enum    | date       | date       |
      | 230                | USAID | cholera, malaria, tuberculosis | no comment       | NRC     | 2014-01-02 | 2014-01-04 |
      | 51                 | ECHO  | cholera                        | cholera epidemic | NRC     | 2014-05-11 | 2014-05-15 |
      | 1                  | ECHO  | cholera                        | cholera          | NRC     | 2014-04-14 | 2014-04-24 |
    And filter date column "Start Date" with start date "2015-01-01" and end date "2015-12-31":
    Then table has rows:
      | Number of patients | Donor   | Diseases treated this month | Comments         | Partner | start date | end date   |
      | quantity           | enum    | enum                        | text             | enum    | date       | date       |
      | 13                 | USAID   | cholera, malaria            | some comment     | NRC     | 2015-02-07 | 2015-03-07 |
      | 11                 | ECHO    | cholera                     | cholera epidemic | NRC     | 2015-07-07 | 2015-08-17 |