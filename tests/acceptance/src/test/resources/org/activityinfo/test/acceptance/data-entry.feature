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
    Given I add "bob@bedatadriven.com" to database "Database" with partner "NRC" and permissions
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

  Scenario: Monthly sites are shown independent from locks
    Given I have created a monthly form named "Monthly"
    And I have created a quantity field "# with improved water supply"
    And I have submitted a "Monthly" form with partner NRC with monthly reports:
      | Month   | # with improved water supply |
      | 2015-01 | 1000                         |
      | 2015-02 | 500                          |
      | 2015-03 | 250                          |
    Then old table for "Monthly" form shows:
      |  |  | Partner | Country |
      |  |  | NRC     | RDC     |
    When I add a lock "monthlyFormLock" on the form "Monthly" from "2015-01-01" to "2015-12-31" in database "Database"
    Then old table for "Monthly" form shows:
      |  |  | Partner | Country |
      |  |  | NRC     | RDC     |

  @AI-1122
  Scenario:Link column in data grid for activities with monthly reporting
    Given I have created a monthly form named "Monthly"
    And I have created a quantity field "# with improved water supply"
    And I have submitted a "Monthly" form with partner NRC with monthly reports:
      | Month   | # with improved water supply |
      | 2015-01 | 1000                         |
    And I have created a monthly form named "AnotherMonthly"
    And I have created a quantity field "indicator"
    And I link indicators:
      | sourceDb | sourceIndicator | destDb   | destIndicator  |
      | Database | # with improved water supply         | Database | indicator      |
    Then old table for "AnotherMonthly" form shows:
      |  |  | Partner | Country |
      |  |  | NRC     | RDC     |
