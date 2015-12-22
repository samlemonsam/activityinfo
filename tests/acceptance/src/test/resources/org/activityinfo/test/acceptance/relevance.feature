@web
Feature: Relevance

  Background:
    Given I have created a database "Relevance"
    And I have added partner "NRC" to "Relevance"
    And I have created a form "Household Survey" using the new layout
    And I have created a single-valued enumerated field "Gender" with items:
      | Male   |
      | Female |
    And I have created a single-valued enumerated field "Pregnant" with relevance "Gender==Male" and items:
      | Yes |
      | No  |

  @AI-1307
  Scenario: Clear values for skipped fields
    Given I have created a form "Household Survey" using the new layout
    When open table for the "Medical Activities" form in the database "Patient Registration"
    When I begin a new submission for "Household Survey"
    And I select "Female" for the Gender field
    Then the Pregnacy field should be enabled
    When I select "Yes" for the Pregancy field
    And I change "Gender" to Male
    Then the Pregnancy field should be disabled
    When I save the submission
    Then Pregnancy field should be saved with an empty value
