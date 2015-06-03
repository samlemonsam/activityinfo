@web
Feature: Link indicators

  Background:
    Given I have created a database "RRMP"
    And I have added partner "CRS" to "RRMP"
    And I have added partner "NRC" to "RRMP"
    And I have created a form named "NFI Distribution" in "RRMP"
    And I have created a quantity field "nb. kits" in "NFI Distribution"
    And I have created a quantity field "Satisfaction score" in "NFI Distribution"
    And I have created a database "PEAR"
    And I have added partner "ABC" to "PEAR"
    And I have created a form named "NFI" in "PEAR"
    And I have created a quantity field "kits" in "NFI"
    And I have created a quantity field "beneficiaries" in "NFI"

  @AI-857
  Scenario: Link indicators
    When I link indicators:
      | sourceDb  | sourceIndicator    | destDb | destIndicator |
      | RRMP      | nb. kits           | PEAR   | kits          |
      | RRMP      | Satisfaction score | PEAR   | beneficiaries |
    Then Linked indicators marked by icon:
      | sourceDb  | sourceIndicator    | destDb | destIndicator |
      | RRMP      | nb. kits           | PEAR   | kits          |
      | RRMP      | Satisfaction score | PEAR   | beneficiaries |
