package fr.unice.polytech.steats.cucumber.rating;

import fr.unice.polytech.steats.steatspico.components.RatingInfo;
import fr.unice.polytech.steats.steatspico.components.RatingSystem;
import fr.unice.polytech.steats.steatspico.entities.users.CampusUser;
import fr.unice.polytech.steats.steatspico.entities.users.User;
import fr.unice.polytech.steats.steatspico.entities.users.UserRole;
import io.cucumber.java.en.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RatingAndViewDeliveryPersonRatingSteps {
    final User deliveryPerson = new User("DP1");

    final CampusUser campusUser1 = new CampusUser();
    final RatingSystem ratingSystem = new RatingSystem();

    @Given("Delivery Person DP1 has a {string} of ratings")
    public void delivery_person_dp1_has_a_of_ratings(String list) {
        List<RatingInfo> listOfRatingInfo = new ArrayList<>();
        String[] valueArray = list.split(", ");
        for (String value : valueArray) {
            listOfRatingInfo.add(new RatingInfo(new User(),Double.parseDouble(value)));
        }
        deliveryPerson.setUserRole(UserRole.DELIVERY_PERSON);
        ratingSystem.getDeliveryPersonRatings().put(deliveryPerson, listOfRatingInfo);
    }
    @When("the CampusUser1 rates the delivery person with {int} out of 5")
    public void the_campus_user1_rates_the_delivery_person_with_out_of(int rating) {
        ratingSystem.rateDeliveryPerson(deliveryPerson,campusUser1, (double) rating);
    }
    @Then("the rating of this delivery person should be {double} out of 5")
    public void the_rating_of_this_delivery_person_should_be_out_of(double expectedRating) {
        Double actualRating = ratingSystem.averageRatingDeliveryPerson(deliveryPerson);
        assertEquals(actualRating, expectedRating);
    }

    @When("the CampusUser2 checks the rating of the delivery person")
    public void the_campus_user2_checks_the_rating_of_the_delivery_person() {
        assertFalse(ratingSystem.getDeliveryPersonRatings().isEmpty());
        assertTrue(ratingSystem.getDeliveryPersonRatings().containsKey(deliveryPerson));
    }
}
