@web
Feature: Locks

  Background:
    Given I have created a database "Hospital Activities"
    And I have added partner "NRC" to "Hospital Activities"
    And I have created the project "Project1"
    And I have created the project "Project2"
    And I have created a form named "Patient Registration" with the submissions:
      | Name of patient | sex of patient | start date | end date   | Project  | Partner |
      | text            | enum           | date       | date       | enum     | enum    |
      | J. Smith        | M              | 2015-01-05 | 2015-01-15 | Project1 | NRC     |
      | L. Johnson      | F              | 2015-01-25 | 2015-02-05 | Project2 | NRC     |
    And I have created a form named "Staff training" with the submissions:
      | # of students | start date | end date   | Project  | Partner |
      | quantity      | date       | date       | enum     | enum    |
      | 25            | 2014-12-20 | 2014-12-25 | Project1 | NRC     |
      | 3             | 2015-12-29 | 2015-01-05 | Project2 | NRC     |

  Scenario: Lock on a database
    When I add a lock "lock1" on the database "Hospital Activities" from "2015-01-01" to "2015-01-31"
    Then "Hospital Activities" database entry appears with lock in Data Entry and cannot be modified nor deleted with any of these values:
      | field           | value     |
      | Name of patient | J. Smith  |
      | # of students   | 3         |
    Then new entry cannot be submitted in "Patient Registration" form
      | sex of patient | Partner | Start Date | End Date   |
      | enum           | enum    | date       | date       |
      | M              | NRC     | 2015-01-01 | 2015-01-02 |

  Scenario: Lock on a form
    When I add a lock "lock2" on the form "Staff training" from "2015-01-01" to "2015-01-31" in database "Hospital Activities"
    Then "Staff training" form entry appears with lock in Data Entry and cannot be modified nor deleted with any of these values:
      | field           | value     |
      | # of students   | 3         |
    Then new entry cannot be submitted in "Staff training" form
      | # of students | start date | end date   | Project  | Partner |
      | quantity      | date       | date       | enum     | enum    |
      | 25            | 2015-01-02 | 2015-01-03 | Project1 | NRC     |

  Scenario: Lock on a project
    When I add a lock "lock3" on the project "Project1" from "2015-01-01" to "2015-01-31" in database "Hospital Activities"
    Then "Hospital Activities" database entry appears with lock in Data Entry and cannot be modified nor deleted with any of these values:
      | field           | value     |
      | Name of patient | J. Smith  |
    Then new entry cannot be submitted in "Patient Registration" form
      | sex of patient | Partner | Start Date | End Date   |
      | enum           | enum    | date       | date       |
      | M              | NRC     | 2015-01-03 | 2015-01-04 |