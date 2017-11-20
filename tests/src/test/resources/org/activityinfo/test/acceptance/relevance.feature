@web
Feature: Relevance

  Background:
    Given I have created a database "Relevance"
    And I have added partner "NRC" to "Relevance"
    And I have created a form "Household Survey" using the new layout
    And I have created a single-valued enumerated field "Gender" with items:
      | Male   |
      | Female |
    And I have created a single-valued enumerated field "Pregnant" with items:
      | Yes |
      | No  |

  @AI-1307
  Scenario: Clear values for skipped fields
    Given I open the form designer for "Household Survey" in database "Relevance"
    And set relevance for "Pregnant" field:
      | Gender | Equal | Female | radio |
    When I begin a new submission for "Household Survey"
    And I enter:
      | field  | value  | controlType |
      | Gender | Female | radio       |
    Then the "Pregnant" field should be enabled
    And I enter:
      | field    | value | controlType |
      | Pregnant | Yes   | radio       |
    And I enter:
      | field  | value | controlType |
      | Gender | Male  | radio       |
    Then the "Pregnant" field should be disabled
    And I enter:
      | field      | value      | controlType |
      | Partner    | NRC        | radio       |
      | Start Date | 2015-12-22 | date        |
      | End Date   | 2015-12-22 | date        |
    When I save the submission
    And I open the table for "Household Survey" in database "Relevance"
    And I edit first row
    Then "Pregnant" field should be with an empty value
