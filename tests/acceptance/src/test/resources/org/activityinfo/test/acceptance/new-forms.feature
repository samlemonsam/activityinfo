@web
Feature: Partners in new form layout

  Background:
    Given I have created a database "Patient Registration"
    And I have created a form "Patient Visits" using the new layout

  @AI-1108
  @web
  Scenario: Partners field is represented by suggest box
    When I have added 21 partners
    Then form "Patient Visits" in database "Patient Registration" has "Partner" field represented by "suggestbox"

#  @web
#  Scenario: Partners field is always shown
#      When I open the table for "Patient Visits" in database "Patient Registration"
#     When I open the form designer for "Patient Visits" in database "Patient Registration"
#     Then the partner field should be visible
#     When I add the user group "Southern Office" to "Patient Registration"
#     When I open the form designer for "Patient Visits"
#     When I open a new form submission
#     Then the user group field should be visible
#     When I remove the user group "Southern Office" from "Patient Registration"
#      And I open the form designer for "Patient Visits"
#     Then the user group field should not be visible
    
#  @web @odk
#  Scenario: Submitting a form with implicit user group
#    And I have added a quantity field "Age"
#    When I open data entry for "Patient Visits"
#    Then the the field "User Group" should not be present
#    When I submit the form with:
#    | field | value |
#    | Age   | 18    |
#    And I export "Patient Visits"
#    Then the exported spreadsheet should contain:
#    | user group | age |
#    | Default    | 18  |
#
#  @web @odk
#  Scenario: Submitting a form with explicit user group
#    And I have added partner "Southern Office"
#    And I have added partner "Northern Office"
#    And I have removed the default user group
#    When I open data entry for "Patient Visits"
#    Then the the field "User Group" should be present with the choices:
#      | Southern Office |
#      | Northern Office |
#    When I submit the form with:
#      | field   | value             |
#      | user group | Southern Office   |
#      | Age     | 18                |
#    And I export "Patient Visits"
#    Then the exported spreadsheet should contain:
#      | user group        | age |
#      | Southern Office   | 18  |
#
#  @web
#  Scenario: Importing a form with implicit user group
#    Given I have created a database "Patient Registration"
#    And I have added a quantity field "Age"
#    When I import a spreadsheet containing:
#      | age |
#      | 18  |
#    And I export "Patient Visits"
#    Then the exported spreadsheet should contain:
#      | user group | age |
#      | Default    | 18  |
    
    