@web
Feature: Data entry

  Background:
    Given I have created a database "Database"
    And I have added partner "NRC" to "Database"
    And I have added partner "UPS" to "Database"
    And I have created a form "Patient Visits" using the new layout
    And I have created a form named "NFI Distribution"


  @AI-1161
  Scenario: Filter by partner update on site crud
    Given I submit a "NFI Distribution" form with:
      | field   | value |
      | partner | NRC   |
    Then "Filter by partner" filter for "NFI Distribution" form has values:
      | NRC |
    When I submit a "NFI Distribution" form with:
      | field   | value |
      | partner | UPS   |
    Then "Filter by partner" filter for "NFI Distribution" form has values:
      | NRC |
      | UPS |


  @AI-1221
  Scenario: Partners are filtered according to permissions
    Given that the user "bob@bedatadriven.com" is signed up
    And I add "bob@bedatadriven.com" to database "Database" with partner "NRC" and permissions
      | field             | value |
      | View              | true  |
      | Edit              | true  |
      | View All          | true  |
      | Edit All          | false |
      | Manager users     | false |
      | Manager all users | false |
      | Design            | false |
    When I login as "bob@bedatadriven.com" with my correct password
    Then new form dialog for "NFI Distribution" form has following items for partner field
      | Default |
      | NRC     |
    Then new form dialog for "Patient Visits" form has following items for partner field
      | Default |
      | NRC     |

