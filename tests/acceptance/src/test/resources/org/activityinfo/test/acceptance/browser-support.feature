Feature: Cross browser support

Scenario Outline: Internet Explorer Support
Given I am using <browser> on <os>
When I login as "qa@bedatadriven.com" with my correct password
Then my dashboard should open

Examples:
  | browser  | os          | 
  | IE 6     | Windows XP  |
  | IE 7     | Windows XP  |
  | IE 8     | Windows 7   |
  | IE 8     | Windows 7   |
  | IE 9     | Windows 7   |
  | IE 10    | Windows 8   |
  | IE 10    | Windows 8.1 |
  | IE 11    | Windows 8   |
  | IE 11    | Windows 8.1 |
