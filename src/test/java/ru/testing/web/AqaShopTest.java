package ru.testing.web;

import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AqaShopTest {

    private static final String URL = "http://localhost:8080";
    private static final String APPROVED_CARD_NUMBER = "4444 4444 4444 4441";
    private static final String DECLINED_CARD_NUMBER = "4444 4444 4444 4442";

    SelenideElement buyBtn;
    SelenideElement buyCreditBtn;
    SelenideElement continueBtn;
    SelenideElement opNameLabel;
    SelenideElement cardNumberField;
    SelenideElement cardNumberFieldValidationError;
    SelenideElement monthField;
    SelenideElement monthFieldValidationError;
    SelenideElement yearField;
    SelenideElement yearFieldValidationError;
    SelenideElement ownerField;
    SelenideElement ownerFieldValidationError;
    SelenideElement cvvField;
    SelenideElement cvvFieldValidationError;
    SelenideElement notificationTitle;
    SelenideElement notificationContent;

    void setSelectors() {
        buyBtn = $("button").shouldHave(text("Купить"));
        buyCreditBtn = $$("button").filterBy(text("Купить в кредит")).first();
        continueBtn = $$("button").filterBy(text("Продолжить")).first();

        opNameLabel = $("div[class^='App_appContainer'] > h3");

        cardNumberField = $$(".input__box > .input__control").get(0);
        cardNumberFieldValidationError = cardNumberField.parent().sibling(0);

        monthField = $$(".input__box > .input__control").get(1);
        monthFieldValidationError = cardNumberField.parent().sibling(0);

        yearField = $$(".input__box > .input__control").get(2);
        yearFieldValidationError = yearField.parent().sibling(0);

        ownerField = $$(".input__box > .input__control").get(3);
        ownerFieldValidationError = ownerField.parent().sibling(0);

        cvvField = $$(".input__box > .input__control").get(4);
        cvvFieldValidationError = cvvField.parent().sibling(0);

        notificationTitle = $(".notification__title");
        notificationContent = $(".notification__content");

    }

    @BeforeEach
    void prepare(){
        DatabaseUtils.query("DELETE FROM credit_request_entity");
        DatabaseUtils.query("DELETE FROM order_entity");
        DatabaseUtils.query("DELETE FROM payment_entity");
        open(URL);
        setSelectors();
    }

    @Test
    @DisplayName("Тест кейс 1: Успешная покупка тура с оплатой по карте")
    void paymentSuccessWithCardTest() {
        buyBtn.click();
        opNameLabel.shouldHave(text("Оплата по карте"));
        cardNumberField.setValue(APPROVED_CARD_NUMBER);
        monthField.setValue("12");
        yearField.setValue("25");
        ownerField.setValue("John Doe");
        cvvField.setValue("123");
        continueBtn.click();
        notificationTitle.shouldHave(text("Успешно"), Duration.ofSeconds(10));
        notificationContent.shouldHave(text("Операция одобрена банком."));
        List<Map<String, Object>> res = DatabaseUtils.query("SELECT * FROM payment_entity where status = 'APPROVED'");
        assertEquals(1, res.size());
    }
    @Test
    @DisplayName("Тест кейс 2: Отказ в оплате при покупке тура с оплатой по карте")
    void paymentDeclinedWithCardTest() {
        buyBtn.click();
        opNameLabel.shouldHave(text("Оплата по карте"));
        cardNumberField.setValue(DECLINED_CARD_NUMBER);
        monthField.setValue("12");
        yearField.setValue("25");
        ownerField.setValue("John Doe");
        cvvField.setValue("123");
        continueBtn.click();
        notificationTitle.shouldHave(text("Ошибка"), Duration.ofSeconds(10));
        notificationContent.shouldHave(text("Ошибка! Банк отказал в проведении операции."));
        List<Map<String, Object>> res = DatabaseUtils.query("SELECT * FROM payment_entity where status = 'DECLINED'");
        assertEquals(1, res.size());
    }

    @Test
    @DisplayName("Тест кейс 3: Успешная покупка тура в кредит")
    void paymentSuccessWithCreditByCardTest() {
        buyCreditBtn.click();
        opNameLabel.shouldHave(text("Кредит по данным карты"));
        cardNumberField.setValue(APPROVED_CARD_NUMBER);
        monthField.setValue("12");
        yearField.setValue("25");
        ownerField.setValue("John Doe");
        cvvField.setValue("123");
        continueBtn.click();
        notificationTitle.shouldHave(text("Успешно"), Duration.ofSeconds(10));
        notificationContent.shouldHave(text("Операция одобрена банком."));
        List<Map<String, Object>> res = DatabaseUtils.query("SELECT * FROM credit_request_entity where status = 'APPROVED'");
        assertEquals(1, res.size());
    }

    @Test
    @DisplayName("Тест кейс 4: Отказ в оплате при покупке тура в кредит")
    void paymentDeclinedWithCreditByCardTest() {
        buyCreditBtn.click();
        opNameLabel.shouldHave(text("Кредит по данным карты"));
        cardNumberField.setValue(DECLINED_CARD_NUMBER);
        monthField.setValue("12");
        yearField.setValue("25");
        ownerField.setValue("John Doe");
        cvvField.setValue("123");
        continueBtn.click();
        notificationTitle.shouldHave(text("Успешно"), Duration.ofSeconds(10));
        notificationContent.shouldHave(text("Операция одобрена банком."));
        List<Map<String, Object>> res = DatabaseUtils.query("SELECT * FROM credit_request_entity where status = 'DECLINED'");
        assertEquals(1, res.size());
    }

    @Test
    @DisplayName("Тест кейс 5: Валидация полей ввода данных карты")
    void fieldValidationShowErrorOnEmptyFieldsTest() {
        buyBtn.click();
        opNameLabel.shouldHave(text("Оплата по карте"));
        continueBtn.click();
        cardNumberFieldValidationError.shouldHave(text("Неверный формат"));
        monthFieldValidationError.shouldHave(text("Неверный формат"));
        yearFieldValidationError.shouldHave(text("Неверный формат"));
        ownerFieldValidationError.shouldHave(text("Поле обязательно для заполнения"));
        cvvFieldValidationError.shouldHave(text("Неверный формат"));
    }

    @Test
    @DisplayName("Тест кейс 6: Валидация срока действия карты")
    void cardExpirationCheckTest() {
        buyBtn.click();
        opNameLabel.shouldHave(text("Оплата по карте"));
        cardNumberField.setValue(APPROVED_CARD_NUMBER);
        monthField.setValue("12");
        yearField.setValue("22");
        ownerField.setValue("John Doe");
        cvvField.setValue("123");
        continueBtn.click();
        yearFieldValidationError.shouldHave(text("Истёк срок действия карты"));
    }

}
