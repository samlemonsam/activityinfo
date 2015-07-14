@web @api
Feature: Export schema to CSV

  Background:
    Given I have created a database "Export-schema"
    And I have added partner "NRC" to "Export-schema"

  Scenario: Export schema to CSV
    Given I have created a form named "NFI Distribution" with the submissions:
      | Partner | Donor | Nb. kits  |
      | enum    | enum  | quantity  |
      | NRC     | USAID |  1000     |
      | NRC     | ECHO  |   500     |
      | NRC     | ECHO  |  2000     |
    When I export the schema of "Export-schema" database
    Then the exported csv contains:
      | DatabaseName  | ActivityName     | FormFieldType  | Name     | AttributeValue |
      | Export-schema | NFI Distribution | Indicator      | Nb. kits |                |
      | Export-schema | NFI Distribution | AttributeGroup | Donor    | ECHO           |
      | Export-schema | NFI Distribution | AttributeGroup | Donor    | USAID          |
