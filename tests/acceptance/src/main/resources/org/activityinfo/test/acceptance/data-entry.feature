@web
Feature: Data entry

  Background:
    Given I have created a database "Database"
    And I have added partner "NRC" to "Database"
    And I have added partner "UPS" to "Database"
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