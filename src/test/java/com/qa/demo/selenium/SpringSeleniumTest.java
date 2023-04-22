package com.qa.demo.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(scripts = { "classpath:cat-schema.sql",
		"classpath:cat-data.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class SpringSeleniumTest {
    
    private WebDriver driver;

    // As we use a random port to run the app this needs to be injected
    @LocalServerPort
    private int port;
    private WebDriverWait wait;

    @BeforeEach
    void init() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        this.driver = new ChromeDriver(options);
        this.driver.manage().window().maximize();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(3));
    }

    @Test
    void testTitle() {
        this.driver.get("http://localhost:" + port + "/");

        WebElement title = this.driver.findElement(By.cssSelector("body > header > h1"));
        assertEquals("CATS", title.getText());
    }
    @Test 
    void testCreate() throws InterruptedException {
        this.driver.get("http://localhost:" + port + "/");

        WebElement nameBox = this.driver.findElement(By.cssSelector("#catName"));
        nameBox.sendKeys("Top Cat");
        
        WebElement lengthBox = this.driver.findElement(By.cssSelector("#catLength"));
        lengthBox.sendKeys("3");

        List<WebElement> elements = this.driver.findElements(By.cssSelector("input[type='checkbox']"));
        elements.stream().forEach((e) -> {
            if(e.isDisplayed()) e.click();
        });

        WebElement successBtn = this.driver.findElement(By.cssSelector("#catForm > div.mt-3 > button.btn.btn-success"));
        successBtn.sendKeys(Keys.ENTER);

        WebElement card = this.wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("#output > div:nth-child(2) > div > div")));

        // see if the cat name is anywhere in the card element
        assertTrue(card.getText().contains("Top Cat"));
        assertTrue(card.getText().contains("3"));
        assertTrue(card.getText().contains("Evil: true"));
        assertTrue(card.getText().contains("Whiskers: true"));
    }

    // Test update button
    @Test
    void testUpdate() throws InterruptedException {
        this.driver.get("http://localhost:" + port + "/");

        // Wait for card to be clickable - give page time to load
        WebElement card = this.wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("#output > div > div")));

        //  Click the update button to open the modal
        WebElement updateBtn = this.driver.findElement(By.cssSelector("#output > div > div > div > button:nth-child(5)"));
        updateBtn.sendKeys(Keys.ENTER);
        
        // Wait for the submit button to be clickable - give modal time to load
        WebElement submitBtn = this.wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("#updateForm > div.mt-3 > button.btn.btn-success")));
        // Update the modal form with new details
        WebElement modal = this.driver.findElement(By.cssSelector(".modal-dialog"));
        WebElement nameInput = modal.findElement(By.cssSelector("#catName"));
        nameInput.clear();
        nameInput.sendKeys("Shrow Dinger");
        WebElement lengthInput = modal.findElement(By.cssSelector("#catLength"));
        lengthInput.clear();
        lengthInput.sendKeys("15");
        List<WebElement> elements = modal.findElements(By.cssSelector("input[type='checkbox']"));
        elements.forEach(e -> e.click());
        // Thread.sleep(3000);
        // Submit changes
        submitBtn.click();
        this.wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".modal-dialog")));
        
        // Wait for card to be clickable - give page time to reload
        // Card is recreated so previous instance has been destroyed
         WebElement newCard = this.wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("#output > div > div")));

        // Test that card contains the new details. 
        assertTrue(newCard.getText().contains("Shrow Dinger"));
        assertTrue(newCard.getText().contains("15"));
        assertTrue(newCard.getText().contains("Evil: false"));
        assertTrue(newCard.getText().contains("Whiskers: false"));
    }

    // Test reset button clears the inputs and checkboxes
    @Test
    void testReset() {
        this.driver.get("http://localhost:" + port + "/");

        WebElement nameBox = this.driver.findElement(By.cssSelector("#catName"));
        nameBox.sendKeys("Top Cat");
        
        WebElement lengthBox = this.driver.findElement(By.cssSelector("#catLength"));
        lengthBox.sendKeys("3");

        WebElement whiskers = this.driver.findElement(By.cssSelector("#catWhiskers"));
        whiskers.click();
        
        WebElement evil = this.driver.findElement(By.cssSelector("#catEvil"));
        evil.click();

        WebElement resetBtn = this.driver.findElement(By.cssSelector("#catForm > div.mt-3 > button.btn.btn-primary"));
        resetBtn.sendKeys(Keys.ENTER);

        // check the form elements are cleared
        assertTrue(nameBox.getText().isEmpty());
        assertTrue(lengthBox.getText().isEmpty());
        assertTrue(!whiskers.isSelected());
        assertTrue(!evil.isSelected());
    }

    // Test that database cat is loaded 
    @Test
    void testGetAll() {
        this.driver.get("http://localhost:" + port + "/");

        WebElement card = this.wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("#output > div > div")));
        // see if the cat name is anywhere in the card element
        assertTrue(card.getText().contains("Mr Bigglesworth"));
    }

    @AfterEach
    void tearDown() {
        // Close the browser
        this.driver.close();
    }
}
