@api
Feature: Site API

  Background:
    Given I have created a database "RRMP"
    And I have added partner "NRC" to "RRMP"
    And I have created a published form named "Distributions"
    And I have created a quantity field "kits" in "Distributions" with code "kits"
    And I have created a quantity field "score" in "Distributions" with code "score"
    And I have created a text field "contact" in "Distributions"
    And I have created a narrative field "description" in "Distributions"
    And I have created a single-valued enumerated field "donor" with items:
      | USAID  |
      | ECHO   |
      | NRC    |
    When I have submitted a "Distributions" form with:
      | field       | value                                        |
      | kits        | 1                                            |
      | score       | 2                                            |
      | partner     | NRC                                          |
      | contact     | Alex B                                       |
      | description | Took place with the support of the community |
      | fromDate    | 2014-01-01                                   |
      | toDate      | 2014-04-10                                   |

  @AI-574
  Scenario: Querying site's points on public database with unauthenticated user
    When Unauthenticated user requests /resources/sites/points?activity=$Distributions
    Then the response should be:
    """
    type : FeatureCollection
    features :
     -
        type : Feature
        id : $SiteId
        properties :
          locationName : RDC
          partnerName : $NRC
          activity : $Distributions
          activityName : Distributions
          startDate : 2014-01-01
          endDate : 2014-04-10
          indicators :
            $kits : 1.0
            $score : 2.0
            $contact : Alex B
            $description: Took place with the support of the community
        geometry :
          type : Point
          coordinates :
           -  21.746970920000003
           -  -4.034950903
    """

  @AI-574
  Scenario: Querying site's points on public database with owner user
    When I request /resources/sites/points?activity=$Distributions
    Then the response should be:
    """
    type : FeatureCollection
    features :
     -
        type : Feature
        id : $SiteId
        properties :
          locationName : RDC
          partnerName : $NRC
          activity : $Distributions
          activityName : Distributions
          startDate : 2014-01-01
          endDate : 2014-04-10
          indicators :
            $kits : 1.0
            $score : 2.0
            $contact : Alex B
        geometry :
          type : Point
          coordinates :
           -  21.746970920000003
           -  -4.034950903
    """

  @AI-574
  Scenario: Querying site's points on public database with another user (not owner)
    When bob@bedatadriven.com requests /resources/sites/points?activity=$Distributions
    Then the response should be:
    """
    type : FeatureCollection
    features :
     -
        type : Feature
        id : $SiteId
        properties :
          locationName : RDC
          partnerName : $NRC
          activity : $Distributions
          activityName : Distributions
          startDate : 2014-01-01
          endDate : 2014-04-10
          indicators :
            $kits : 1.0
            $score : 2.0
            $contact : Alex B
        geometry :
          type : Point
          coordinates :
           -  21.746970920000003
           -  -4.034950903
    """

  @AI-917
  Scenario: Querying database schema
    When I request /resources/database/$RRMP/schema
    Then the response should be:
    """
    name: RRMP
    editAllowed: true
    designAllowed: true
    editAllAllowed: true
    description: null
    owned: true
    country:
      id: 1
      name: RDC
      code: null
    partners: 
      - id: $Default
        name: Default
        fullName: null
      - id: $NRC
        name: NRC
        fullName: null
    lockedPeriods: [ ]
    projects: [ ]
    activities:
      - id: $Distributions
        name: Distributions
        reportingFrequency: 0
        category: null
        published: 1
        locationType:
          id: 100000
          name: Country
          adminLevelId: null
        lockedPeriods: [ ]
        indicators: 
          - id: $kits
            name: kits
            code: kits
            units: parsects
            type: QUANTITY
            description: null
            aggregation: 0
            listHeader: null
            calculatedAutomatically: false
            expression: null
            category: null
            mandatory: false
            
          - id: $score
            code: score        
            name: score
            units: parsects
            type: QUANTITY
            description: null
            aggregation: 0
            listHeader: null
            calculatedAutomatically: false
            expression: null
            category: null
            mandatory: false
            
          - id: $contact
            name: contact
            code: contact
            type: FREE_TEXT
            description: null
            aggregation: 0
            listHeader: null
            units:
            calculatedAutomatically: false
            expression: null
            category: null
            mandatory: false
            
          - id: $description
            name: description
            code: description
            type: NARRATIVE
            description: null
            aggregation: 0
            listHeader: null
            units:
            calculatedAutomatically: false
            expression: null
            category: null
            mandatory: false  
            
        attributeGroups: 
          - id: $donor
            name: donor
            multipleAllowed: false
            workflow: false
            mandatory: false
            attributes: 
              - id: $USAID
                name: USAID
              - id: $ECHO
                name: ECHO
              - id: $NRC
                name: NRC
    """
