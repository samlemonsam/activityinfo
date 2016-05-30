@api
Feature: Calculated fields

  Scenario Outline: Defining Expressions
    Given I have created a database "RRMP"
    And I have added partner "NRC" to "RRMP"
    And I have created a form named "NFI Distribution"
    And I have created a quantity field "Number of households" with code "HH"
    And I have created a quantity field "Number of women" with code "NF"
    And I have created a quantity field "Number of men" with code "NM"
    And I have created a quantity field "Number of children" with code "NC"
    And I have created a calculated field "X" in "NFI Distribution" with expression "<Expression>"
    When I submit a "NFI Distribution" form with:
      | field                | value |
      | partner              | NRC   |
      | Number of households | <HH>  |
      | Number of women      | <NF>  |
      | Number of men        | <NM>  |
      | Number of children   | <NC>  |
    Then the value of "X" in the submission should be <Result>

    Examples:
      | Expression | HH  | NF  | NM  | NC | Result    |
      | HH         | 100 |     |     |    | 100       |
      | [HH]       | 100 |     |     |    | 100       |
      | {HH}       | 100 |     |     |    | 100       |
      | HH         |     |     |     |    | <Missing> |
      | NF+NM      |     | 42  | 11  |    | 53        |
      | NF+NM+NC   |     | 50  | 49  | 35 | 134       |
      | HH-NF      | 100 | 33  |     |    | 67        |
      | (NF+NM)/HH | 50  | 100 | 100 |    | 4         |
      | (NF+NM)/HH |     | 100 | 100 |    | <Missing> |

  Scenario: Calculations with missing values
    Given I have created a database "WASH cost calculation"
    And I have added partner "UNHCR"
    And I have created a form named "Expenditures"
    And I have created a quantity field "Expenditure" with code "EXP"
    And I have created a quantity field "% Capital" with code "PCT_CAPITAL"
    And I have created a quantity field "% Maintenance" with code "PCT_MAINT"
    And I have created a calculated field "$ Capital" with expression "EXP*(PCT_CAPITAL/100)"
    And I have submitted "Expenditures" forms with:
      | Partner | Comments          | Expenditure | % Capital | % Maintenance |
      | UNHCR   | Purchase of Pumps | 23450       | 100       |               |
      | UNHCR   | Technician salary | 53600       |           | 100           |
      | UNHCR   | Replacement parts | 10230       | 10        | 90            |
    When I export the form "Expenditures"
    Then the exported spreadsheet contains:
      | Partner | Comments          | Expenditure  | % Capital | % Maintenance | $ Capital  |
      | UNHCR   | Purchase of Pumps | 23,450       | 100       |               |    23,450  |
      | UNHCR   | Technician salary | 53,600       |           | 100           |         0  |
      | UNHCR   | Replacement parts | 10,230       | 10        | 90            |     1,023  |