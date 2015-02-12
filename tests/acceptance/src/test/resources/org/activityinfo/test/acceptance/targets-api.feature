Feature: Indicator Targets API
  As a systems analyst
  I want to be able to define target indicator values through the API
  So I can connect my existing systems with ActivityInfo

  Background:
    Given I have created a database "PAKA"
    And I have added partner "UNHCR PESHAWAR"
    And I have created the project "Direct - HCR Peshawar"
    And I have created a form named "Advocacy Conducted"
    And I have created a quantity field "Advocacy conducted USD" in "Advocacy Conducted"
    
  Scenario: Defining Targets by Partner
    When I execute the command:
      """
      type: AddTarget
      command:
        databaseId: $PAKA
        target:
          name: Pakistan
          partnerId: ${UNHCR PESHAWAR}
          projectId: ${Direct - HCR Peshawar}
          fromDate: 2014-01-01
          toDate: 2014-12-31
      """
    Then the response should be:
      """
      newId: $Pakistan
      """
    When I execute the command:
      """
      type: UpdateTargetValue
      command: 
        targetId: $Pakistan
        indicatorId: ${Advocacy conducted USD}
        changes:
          value: 11996
      """
    Then the response should have status code 200
    When I execute the command:
      """
      type: GetTargets
      command:
        databaseId: $PAKA
      """
    Then the response should be:
      """
      - id: $Pakistan
        name: Pakistan
        fromDate: 2014-01-01
        toDate: 2014-12-31
        partner:
          id: ${UNHCR PESHAWAR}
          name: ~UNHCR PESHAWAR
        project:
          id: ${Direct - HCR Peshawar}
          name: ~Direct - HCR Peshawar
        targetValues:
          - indicatorId: ${Advocacy conducted USD}
            value: 11996
      """
    
