@web
Feature: Design
  Creation/Design of AI entities

  Background:
    Given I have created a database "RRMP"

  Scenario: Creation and deletion of location types
    When I have created a location type "Oblast"
    Then Location type "Oblast" should be visible.
    When I have removed the location type "Oblast"
    Then Location type "Oblast" is no longer visible.