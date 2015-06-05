@web
Feature: Link indicators

  Background:
    Given I have created a database "Overview"
    And I have added partner "NRC" to "Overview"
    And I have created a form named "Medical Activities" with the submissions:
      | Number of patients | Donor   | Diseases treated this month    | Comments         | Partner |
      | quantity           | enum    | enum                           | text             | NRC     |
      | 230                | USAID   | cholera, malaria, tuberculosis | no comment       | NRC     |
      | 23                 | ECHO    | cholera                        | cholera epidemic | NRC     |
    And I have created a database "Goma project"
    And I have added partner "NRC" to "Goma project"
    And I have created a form named "Activities at Goma hospital" with the submissions:
      | Patients Goma | Patients Goma Name | Donor | Diseases treated this month | Comments         | Partner |
      | quantity      | text               | enum  | enum                        | text             | NRC     |
      | 45            | Mike               | ECHO  | malaria, tuberculosis       | rainy season     | NRC     |
      | 23            | Bob                | USAID | cholera                     | cholera epidemic | NRC     |
    And I have created a database "Khartoum project"
    And I have added partner "NRC" to "Khartoum project"
    And I have created a form named "Activities at Khartoum hospital" with the submissions:
      | Patients Khartoum | Funder | Diseases              | Comments         | Partner |
      | quantity          | enum   | enum                  | text             | NRC     |
      | 12                | ECHO   | malaria, tuberculosis | no comment       | NRC     |
      | 2450              | ECHO   | cholera               | cholera epidemic | NRC     |
      | 3                 | UNICEF | tuberculosis          | no comment       | NRC     |

  Scenario: Only the quantity form fields of the forms appearing as indicators
    When selecting "Goma project" as the source link database
    Then source indicator link database shows:
    | | Activities at Goma hospital |
    | |    Patients Goma            |

  Scenario: Linking form fields with same labels
    And I link indicators:
      | sourceDb     | sourceIndicator | destDb   | destIndicator      |
      | Goma project | Patients Goma   | Overview | Number of patients |
    Then submissions for "Medical Activities" form are:
      | Number of patients | Donor | Diseases treated this month  |
      | 230                | USAID | cholera,malaria,tuberculosis |
      | 23                 | ECHO  | cholera                      |
      | 45				   | ECHO  | malaria,tuberculosis         |
      | 23                 | USAID | cholera                      |

  @AI-857
  Scenario: Design link indicators UI test
    When I have created a database "RRMP"
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
    And I link indicators:
      | sourceDb  | sourceIndicator    | destDb | destIndicator |
      | RRMP      | nb. kits           | PEAR   | kits          |
      | RRMP      | Satisfaction score | PEAR   | beneficiaries |
    Then Linked indicators marked by icon:
      | sourceDb  | sourceIndicator    | destDb | destIndicator |
      | RRMP      | nb. kits           | PEAR   | kits          |
      | RRMP      | Satisfaction score | PEAR   | beneficiaries |
