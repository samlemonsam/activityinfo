@web
Feature: Reference fields

  Background:
    Given I have created a database "ReferenceDb"
    And I have added partner "NRC" to "ReferenceDb"
    And I have created a form "Reference fields" using the new layout
    And I have created a form named "Medical Activities" with the submissions:
      | Number of patients | Donor | Diseases treated this month    | Label            | Comments         | Partner | start date | end date   |
      | quantity           | enum  | enum                           | text             | text             | enum    | date       | date       |
      | 230                | USAID | cholera, malaria, tuberculosis | No Label         | no comment       | NRC     | 2014-01-02 | 2014-01-04 |
      | 51                 | ECHO  | cholera                        | cholera epidemic | cholera epidemic | NRC     | 2014-05-11 | 2014-05-15 |
      | 1                  | ECHO  | cholera                        | cholera          | cholera          | NRC     | 2014-04-14 | 2014-04-24 |
      | 13                 | USAID | cholera, malaria               | Some Label       | some comment     | NRC     | 2015-02-07 | 2015-03-07 |
      | 11                 | ECHO  | malaria                        | malaria          | cholera epidemic | NRC     | 2015-07-07 | 2015-08-17 |

  Scenario: Reference fields
    Given I open the form designer for "Reference fields" in database "ReferenceDb"
    And drop field:
      | label                  | type      |
      | ReferPartners          | Reference |
      | ReferMedicalActivities | Reference |
    And choose reference for field "ReferPartners"
      | My databases | ReferenceDb | Partners |
    And choose reference for field "ReferMedicalActivities"
      | My databases | ReferenceDb | Medical Activities |
    And I begin a new submission for "Reference fields"
    Then "ReferPartners" field has instances:
      | Default |
      | NRC     |
    Then "ReferMedicalActivities" field has instances:
      | No Label         |
      | cholera epidemic |
      | cholera          |
      | Some Label       |
      | malaria          |