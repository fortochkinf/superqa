package ru.testing.web.page;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class TourBuyPage {

    public static final String CREDIT = "creadit";
    public static final String DEBIT = "debit";

    public static final String CARD_NUMBER_FIELD = "cardNumber";
    public static final String YEAR_FIELD = "year";
    public static final String MONTH_FIELD = "mount";
    public static final String CVV_FIELD = "cvv";
    public static final String OWNER_FIELD = "owner";

    public static final SelenideElement buyBtn = $$("button").filterBy(text("Купить")).first();
    public static final SelenideElement buyCreditBtn = $$("button").filterBy(text("Купить в кредит")).first();
    public static final SelenideElement continueBtn = $$("button").filterBy(text("Продолжить")).first();
    public static final SelenideElement opNameLabel = $("div[class^='App_appContainer'] > h3");
    public static final SelenideElement cardNumberField = $$(".input__box > .input__control").get(0);
    public static final SelenideElement cardNumberFieldValidationError = cardNumberField.parent().sibling(0);
    public static final SelenideElement monthField = $$(".input__box > .input__control").get(1);
    public static final SelenideElement monthFieldValidationError = monthField.parent().sibling(0);
    public static final SelenideElement yearField =$$(".input__box > .input__control").get(2);
    public static final SelenideElement yearFieldValidationError = yearField.parent().sibling(0);
    public static final SelenideElement ownerField = $$(".input__box > .input__control").get(3);
    public static final SelenideElement ownerFieldValidationError = ownerField.parent().sibling(0);
    public static final SelenideElement cvvField = $$(".input__box > .input__control").get(4);
    public static final SelenideElement cvvFieldValidationError = cvvField.parent().sibling(0);
    public static final SelenideElement notificationTitle = $(".notification__title");
    public static final SelenideElement notificationContent = $(".notification__content");


    public TourBuyPage() {
        Selenide.open("http://localhost:8080");
    }

    @Step("Выбрать тип платежа")
    public TourBuyPage choosePaymentType(String paymentType){
        if (paymentType.equals(CREDIT)) {
            buyCreditBtn.click();
            opNameLabel.shouldHave(text("Кредит по данным карты"));
        }else if (paymentType.equals(DEBIT)) {
            buyBtn.click();
            opNameLabel.shouldHave(text("Оплата по карте"));
        }else{
            throw new IllegalArgumentException("Неверный тип платежа: " + paymentType);
        }
        return this;
    }

    @Step("Заполнить данные карты")
    public TourBuyPage fillPaymentData(String cardNumber, String month, String year, String owner, String cvv){
        if(cardNumber != null) {
            cardNumberField.setValue(cardNumber);
        }
        if(month != null) {
            monthField.setValue(month);
        }
        if(year != null) {
            yearField.setValue(year);
        }
        if(owner != null) {
            ownerField.setValue(owner);
        }
        if(cvv != null) {
            cvvField.setValue(cvv);
        }
        return this;
    }

    @Step("Оплатить")
    public TourBuyPage pay(){
        continueBtn.click();
        return this;
    }
    @Step("Получить ошибки валидации данных формы оплаты")
    public Map<String, String> getFormValidationErrors() {
        Map<String, String> errors = new HashMap<>();
        errors.put(CARD_NUMBER_FIELD, cardNumberFieldValidationError.isDisplayed() ? cardNumberFieldValidationError.getText() : null);
        errors.put(MONTH_FIELD, monthFieldValidationError.isDisplayed() ? monthFieldValidationError.getText() : null);
        errors.put(YEAR_FIELD, yearFieldValidationError.isDisplayed() ? yearFieldValidationError.getText() : null);
        errors.put(OWNER_FIELD, ownerFieldValidationError.isDisplayed() ? ownerFieldValidationError.getText() : null);
        errors.put(CVV_FIELD, cvvFieldValidationError.isDisplayed() ? cvvFieldValidationError.getText() : null);
        return errors;
    }

    @Step("Получить статус платежа")
    public boolean getPaymentStatus(){
        notificationTitle.should(visible, Duration.ofSeconds(20));
        if (notificationTitle.getText().equals("Успешно") && notificationContent.getText().equals("Операция одобрена Банком.")){
            return true;
        }else if(notificationTitle.getText().equals("Ошибка") && notificationContent.getText().equals("Ошибка! Банк отказал в проведении операции.")){
            return false;
        }else {
            throw new IllegalStateException("В процессе оплаты получен неожиданный результат. Ошибка: " + notificationTitle.getText() + " описание: " + notificationContent.getText());
        }
    }

}
