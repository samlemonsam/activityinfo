@web
Feature: Export to Excel

  Scenario: Export single form to Excel
    Given I have created a database "RRMP"
    And I have added partner "NRC" to "RRMP"
    And I have created a form named "NFI Distribution" with the submissions:
      | Partner | Donor | Nb. kits  |
      | NRC     | USAID |  1000     |
      | NRC     | ECHO  |   500     |
      | NRC     | ECHO  |  2000     |
    And I export the form "NFI Distribution"
    Then the exported spreadsheet contains:
      | Partner | Donor | Nb. kits  |
      | NRC     | USAID | 1,000     |
      | NRC     | ECHO  |   500     |
      | NRC     | ECHO  | 2,000     |