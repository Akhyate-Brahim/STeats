Feature: sending notifications when the delivery is IN_PROGRESS

  Scenario: notifying the delivery person and the campus user about the delivery
    Given a user named "Samy"
    And a delivery with the status WAITING
    And "John" a delivery person
    When the delivery status is set as IN_PROGRESS
    Then a notification is sent to the delivery person and the campus user









