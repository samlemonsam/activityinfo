@odk
Feature: Mobile Data Collection with ODK
  As a program manager
  I want users to be able to collect data in the field with ODK Collect
  
  Scenario: Water point data collection
    Given I have created a database "WASH"
      And I have added partner "NRC"
      And I have created a location type "Waterpoint"
      And I have created a form named "Water Quality Survey" with location type "Waterpoint"
      And I have created a enumerated field "Quality" with items:
        | Good   |
        | Bad    |
        | Ugly   |
      And I have created a quantity field "pH"
      And I have created a text field "Contact"
     Then I can submit a "Water Quality Survey" form with:
        | Field         | Value    |
        | Waterpoint    | Rutshuru |
        | Quality       | Good     |
        | pH            | 1.3      |
        | Contact       | Fred     |
    
      
  Scenario: Forms with monthly reporting cannot be submitted via ODK
    Given I have created a monthly form named "Utilisation Rate"
    Then "Utilisation Rate" should not appear in the list of blank forms in ODK
    
  Scenario: Forms bound to admin levels cannot be submitted via ODK
    Given I have created a form named "Total Population" with location type bound to the "Province" level
    Then "Total Population" should not appear in the list of blank forms in ODK
