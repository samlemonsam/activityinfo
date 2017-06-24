@web
Feature: Attachment

  Background:
    Given I have created a database "Attachment"
    And I have added partner "NRC" to "Attachment"
    And I have created a form "NFI Distribution" using the new layout

  Scenario: Attachment upload and security checks
    Given I open the form designer for "NFI Distribution" in database "Attachment"
    And drop field:
      | label        | type        |
      | MyImage      | Image       |
      | MyAttachment | Attachments |
    And I submit a "NFI Distribution" form with:
      | field        | value       |
      | partner      | NRC         |
      | MyImage      | ai-test.png |
      | MyAttachment | ai-1262.png |
    Then "MyImage" field contains image.
    Then "MyAttachment" field has downloadable link.
    Then "MyAttachment" field's downloadable link is forbidden for anonymous access.
    Then "MyAttachment" field's downloadable link is forbidden for "bob@bedatadriven.com"
