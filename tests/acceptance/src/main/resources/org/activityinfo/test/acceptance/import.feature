@web
Feature: Import from Excel

  Background:
    Given I have created a database "Import"
    And I have added partner "NRC" to "Import"
    And I have created a form named "NFI Distribution"
    And I have created a quantity field "nb. kits" in "NFI Distribution"
    And I have created a single-valued enumerated field "Donor" with items:
      | USAID |
      | ECHO  |
      | NRC   |

  @AI-999
  Scenario: Import single-valued enum from Excel
    When I import into the form "NFI Distribution" spreadsheet:
      | Partner Name | Partner Full Name | Donor | Nb. kits | Start Date | End Date   | Comments |
      | NRC          |                   | USAID | 1,000    | 01/02/2014 | 01/03/2014 | row 1    |
      | NRC          |                   | ECHO  | 500      | 01/03/2014 | 01/04/2014 | row 2    |
      | NRC          |                   | ECHO  | 2,000    | 01/04/2014 | 01/05/2014 | row 3    |
    And open table for the "NFI Distribution" form in the database "Import"
    Then table has rows with hidden built-in columns:
      | Donor | Nb. kits |
      | enum  | quantity |
      | USAID | 1000     |
      | ECHO  | 500      |
      | ECHO  | 2000     |

  @AI-1209
  Scenario: Import with 500 rows
    When I import into the form "NFI Distribution" spreadsheet with 500 rows:
      | Partner Name | Partner Full Name | Donor | Nb. kits | Start Date | End Date   | Comments |
      | NRC          |                   | USAID | 1,000    | 01/02/2014 | 01/03/2014 | row 1    |
    Then "NFI Distribution" table has 500 rows in "Import" database
