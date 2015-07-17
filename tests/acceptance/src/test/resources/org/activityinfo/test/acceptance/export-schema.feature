@web @api
Feature: Export schema to CSV

  Background:
    Given I have created a database "Export-schema"
    And I have added partner "NRC" to "Export-schema"

  Scenario: Export schema to CSV
    Given I have created a form named "NFI Distribution" with the submissions:
      | Partner | Donor | Nb. kits  |
      | enum    | enum  | quantity  |
      | NRC     | USAID |  1000     |
      | NRC     | ECHO  |   500     |
      | NRC     | ECHO  |  2000     |
    When I export the schema of "Export-schema" database
    Then the exported csv contains:
      | DatabaseName  | ActivityName     | FormFieldType  | Name     | AttributeValue |
      | Export-schema | NFI Distribution | Indicator      | Nb. kits |                |
      | Export-schema | NFI Distribution | AttributeGroup | Donor    | ECHO           |
      | Export-schema | NFI Distribution | AttributeGroup | Donor    | USAID          |

  @AI-917
  Scenario: Querying database schema
    When I have created a published form named "Distributions"
    And I have created a quantity field "kits" in "Distributions" with code "kits"
    And I have created a quantity field "score" in "Distributions" with code "score"
    And I have created a enumerated field "donor" with items:
      | USAID  |
      | ECHO   |
      | NRC    |
    And I request /resources/database/$Export-schema/schema
    Then the response should be (ignoring position in array):
    """
    allowNestedValues : true
    country :
        name : Rdc
        id : $Rdc
        code : null
    partners :
     -
        name : Default
        id : $Default
        fullName : null
     -
        name : NRC
        id : $NRC
        fullName : null
    activities :
     -
        indicators :
         -
            name : score
            id : $score
            type :
              id: QUANTITY
              parameterFormClass:
                id :
                  domain : _
                ownerId : null
                label : null
                description : null
                elements:
                 -
                    id :
                      domain : u
                    code : null
                    label : Units
                    description: "Describes the unit of measurement. For example: 'households', 'individuals', 'meters', etc."
                    relevanceConditionExpression : null
                    type :
                      typeClass :
                        id : FREE_TEXT
                    readOnly : false
                    visible : true
                    superProperties : []
                    required : false
                    name : units
                fields :
                 -
                    id :
                      domain : u
                    code : null
                    label : Units
                    description: "Describes the unit of measurement. For example: 'households', 'individuals', 'meters', etc."
                    relevanceConditionExpression : null
                    type :
                      typeClass :
                        id : FREE_TEXT
                    readOnly : false
                    visible : true
                    superProperties : []
                    required : false
                    name : units
                parentId : null
                sections : []
            description : null
            expression : null
            calculatedAutomatically : false
            aggregation : 0
            listHeader : null
            category : null
            units : parsects
            mandatory : false
            relevanceCondition : null
            code : score
         -
            name : kits
            id : $kits
            type :
              id: QUANTITY
              parameterFormClass:
                id :
                  domain : _
                ownerId : null
                label : null
                description : null
                elements:
                 -
                    id :
                      domain : u
                    code : null
                    label : Units
                    description: "Describes the unit of measurement. For example: 'households', 'individuals', 'meters', etc."
                    relevanceConditionExpression : null
                    type :
                      typeClass :
                        id : FREE_TEXT
                    readOnly : false
                    visible : true
                    superProperties : []
                    required : false
                    name : units
                fields :
                 -
                    id :
                      domain : u
                    code : null
                    label : Units
                    description: "Describes the unit of measurement. For example: 'households', 'individuals', 'meters', etc."
                    relevanceConditionExpression : null
                    type :
                      typeClass :
                        id : FREE_TEXT
                    readOnly : false
                    visible : true
                    superProperties : []
                    required : false
                    name : units
                parentId : null
                sections : []
            description : null
            expression : null
            calculatedAutomatically : false
            aggregation : 0
            listHeader : null
            category : null
            units : parsects
            mandatory : false
            relevanceCondition : null
            code : kits

        attributeGroups :
         -
            attributes :
             -
                name : ECHO
                id : $ECHO
             -
                name : NRC
                id : $NRC
             -
                name : USAID
                id : $USAID
            name : donor
            id : $donor
            mandatory : false
            multipleAllowed : false
            workflow : false
        lockedPeriods : []
        locationType :
           name: Country
           id : $Country
           adminLevelId : null
        name : Distributions
        id : $Distributions
        reportingFrequency : 0
        category : null
        published : 1
    lockedPeriods : []
    projects : []
    name : Export-schema
    key : db$Export-schema
    ownerEmail : $user
    designAllowed : true
    viewAllAllowed: true
    editAllowed : true
    editAllAllowed : true
    manageUsersAllowed : true
    manageAllUsersAllowed : true
    myPartnerId : 0
    enabledLockedPeriods : []
    entityName : UserDatabase
    ownerName : Alex
    myPartner : null
    description : null
    owned : true
    """
