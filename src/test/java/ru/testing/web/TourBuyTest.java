package ru.testing.web;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.junit5.ScreenShooterExtension;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.*;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.OutputType;
import ru.testing.web.page.TourBuyPage;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static ru.testing.web.page.TourBuyPage.*;

@ExtendWith({ScreenShooterExtension.class})
@Epic("Оплата туров онлайн")
@Feature("Форма оплаты")
@Story("Пользователь оплачивает тур")
class TourBuyTest {

    private static final String APPROVED_CARD_NUMBER = "4444 4444 4444 4441";
    private static final String DECLINED_CARD_NUMBER = "4444 4444 4444 4442";


    @BeforeAll
    static void setupAllureReports() {
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
                .screenshots(true)
                .savePageSource(false)
        );
    }

    @BeforeEach
    void prepare(){
        DatabaseUtils.query("DELETE FROM credit_request_entity");
        DatabaseUtils.query("DELETE FROM order_entity");
        DatabaseUtils.query("DELETE FROM payment_entity");
    }

    @Test
    @DisplayName("Тест кейс 1: Успешная покупка тура с оплатой по карте")
    @Severity(SeverityLevel.BLOCKER)
    void paymentSuccessWithCardTest() {
        TourBuyPage page = new TourBuyPage();
        assertTrue(page
            .choosePaymentType(TourBuyPage.DEBIT)
            .fillPaymentData(APPROVED_CARD_NUMBER, "12", "25", "John Doe", "123")
            .pay()
            .getPaymentStatus()
        );
        List<Map<String, Object>> res = DatabaseUtils.query("SELECT * FROM payment_entity where status = 'APPROVED'");
        assertEquals(1, res.size());
    }
    @Test
    @DisplayName("Тест кейс 2: Отказ в оплате при покупке тура с оплатой по карте")
    @Severity(SeverityLevel.BLOCKER)
    void paymentDeclinedWithCardTest() {
        TourBuyPage page = new TourBuyPage();
        assertFalse(page
                .choosePaymentType(TourBuyPage.DEBIT)
                .fillPaymentData(DECLINED_CARD_NUMBER, "12", "25", "John Doe", "123")
                .pay()
                .getPaymentStatus()
        );
        List<Map<String, Object>> res = DatabaseUtils.query("SELECT * FROM payment_entity where status = 'DECLINED'");
        assertEquals(1, res.size());
    }

    @Test
    @DisplayName("Тест кейс 3: Успешная покупка тура в кредит")
    @Severity(SeverityLevel.BLOCKER)
    void paymentSuccessWithCreditByCardTest() {
        TourBuyPage page = new TourBuyPage();
        assertTrue(page
                .choosePaymentType(TourBuyPage.CREDIT)
                .fillPaymentData(APPROVED_CARD_NUMBER, "12", "25", "John Doe", "123")
                .pay()
                .getPaymentStatus()
        );
        List<Map<String, Object>> res = DatabaseUtils.query("SELECT * FROM credit_request_entity where status = 'APPROVED'");
        assertEquals(1, res.size());
    }

    @Test
    @DisplayName("Тест кейс 4: Отказ в оплате при покупке тура в кредит")
    @Severity(SeverityLevel.BLOCKER)
    void paymentDeclinedWithCreditByCardTest() {
        TourBuyPage page = new TourBuyPage();
        assertFalse(page
                .choosePaymentType(TourBuyPage.CREDIT)
                .fillPaymentData(DECLINED_CARD_NUMBER, "12", "25", "John Doe", "123")
                .pay()
                .getPaymentStatus()
        );
        List<Map<String, Object>> res = DatabaseUtils.query("SELECT * FROM credit_request_entity where status = 'DECLINED'");
        assertEquals(1, res.size());
    }

    @Test
    @DisplayName("Тест кейс 5: Валидация полей ввода данных карты")
    @Severity(SeverityLevel.MINOR)
    void fieldValidationShowErrorOnEmptyFieldsTest() {
        TourBuyPage page = new TourBuyPage();
        var result = page.choosePaymentType(TourBuyPage.DEBIT).pay().getFormValidationErrors();
        assertEquals("Неверный формат", result.get(CARD_NUMBER_FIELD));
        assertEquals("Неверный формат", result.get(MONTH_FIELD));
        assertEquals("Неверный формат", result.get(YEAR_FIELD));
        assertEquals("Неверный формат", result.get(CVV_FIELD));
        assertEquals("Поле обязательно для заполнения", result.get(OWNER_FIELD));
    }

    @Test
    @DisplayName("Тест кейс 6: Валидация срока действия карты")
    @Severity(SeverityLevel.MINOR)
    void cardExpirationCheckTest() {
        TourBuyPage page = new TourBuyPage();
        var result = page
                .choosePaymentType(TourBuyPage.DEBIT)
                .fillPaymentData(APPROVED_CARD_NUMBER, "12", "22", "John Doe", "123")
                .pay()
                .getFormValidationErrors();
        assertEquals("Истёк срок действия карты", result.get(YEAR_FIELD));
    }

    @Test
    @DisplayName("Тест кейс 7: Поле \"Номер карты\" пустое")
    @Severity(SeverityLevel.MINOR)
    void cardNumberFieldIsEmptyTest() {
        TourBuyPage page = new TourBuyPage();
        var result = page
                .choosePaymentType(TourBuyPage.DEBIT)
                .fillPaymentData(null, "12", "22", "John Doe", "123")
                .pay()
                .getFormValidationErrors();
        assertEquals("Неверный формат", result.get(CARD_NUMBER_FIELD));
    }

    @Test
    @DisplayName("Тест кейс 8: Поле \"Год\" пустое")
    @Severity(SeverityLevel.MINOR)
    void yearFieldIsEmptyTest() {
        TourBuyPage page = new TourBuyPage();
        var result = page
                .choosePaymentType(TourBuyPage.DEBIT)
                .fillPaymentData(APPROVED_CARD_NUMBER, "12", null, "John Doe", "123")
                .pay()
                .getFormValidationErrors();
        assertNull(result.get(CARD_NUMBER_FIELD));
        assertNull(result.get(OWNER_FIELD));
        assertNull(result.get(MONTH_FIELD));
        assertNull(result.get(CVV_FIELD));
        assertEquals("Неверный формат", result.get(YEAR_FIELD));
    }

    @Test
    @DisplayName("Тест кейс 9: Поле \"Месяц\" пустое")
    @Severity(SeverityLevel.MINOR)
    void monthFieldIsEmptyTest() {
        TourBuyPage page = new TourBuyPage();
        var result = page
                .choosePaymentType(TourBuyPage.DEBIT)
                .fillPaymentData(APPROVED_CARD_NUMBER, null, "25", "John Doe", "123")
                .pay()
                .getFormValidationErrors();
        assertNull(result.get(CARD_NUMBER_FIELD));
        assertNull(result.get(OWNER_FIELD));
        assertNull(result.get(YEAR_FIELD));
        assertNull(result.get(CVV_FIELD));
        assertEquals("Неверный формат", result.get(MONTH_FIELD));
    }

    @Test
    @DisplayName("Тест кейс 10: Поле \"Владелец\" пустое")
    @Severity(SeverityLevel.MINOR)
    void ownerFieldIsEmptyTest() {
        TourBuyPage page = new TourBuyPage();
        var result = page
                .choosePaymentType(TourBuyPage.DEBIT)
                .fillPaymentData(APPROVED_CARD_NUMBER, "12", "25", null, "123")
                .pay()
                .getFormValidationErrors();
        assertNull(result.get(CARD_NUMBER_FIELD));
        assertNull(result.get(YEAR_FIELD));
        assertNull(result.get(MONTH_FIELD));
        assertNull(result.get(CVV_FIELD));
        assertEquals("Поле обязательно для заполнения", result.get(OWNER_FIELD));
    }

    @Test
    @DisplayName("Тест кейс 11: Поле \"CVV\" пустое")
    @Severity(SeverityLevel.MINOR)
    void cvvFieldIsEmptyTest() {
        TourBuyPage page = new TourBuyPage();
        var result = page
                .choosePaymentType(TourBuyPage.DEBIT)
                .fillPaymentData(APPROVED_CARD_NUMBER, "12", "25", "John Doe", null)
                .pay()
                .getFormValidationErrors();
        assertNull(result.get(CARD_NUMBER_FIELD));
        assertNull(result.get(OWNER_FIELD));
        assertNull(result.get(MONTH_FIELD));
        assertNull(result.get(YEAR_FIELD));
        assertEquals("Неверный формат", result.get(CVV_FIELD));
    }

    @Test
    @DisplayName("Тест кейс 12: Поле \"Владелец\" на кирилице")
    @Severity(SeverityLevel.MINOR)
    void cyrillicOwnerNameTest() {
        TourBuyPage page = new TourBuyPage();
        var result = page
                .choosePaymentType(TourBuyPage.DEBIT)
                .fillPaymentData(APPROVED_CARD_NUMBER, "12", "25", "Иван Петров", "123")
                .pay()
                .getFormValidationErrors();
        assertNull(result.get(CARD_NUMBER_FIELD));
        assertNull(result.get(CVV_FIELD));
        assertNull(result.get(MONTH_FIELD));
        assertNull(result.get(YEAR_FIELD));
        assertEquals("Неверный формат", result.get(OWNER_FIELD));
    }

    @Test
    @DisplayName("Тест кейс 13: Поле \"Месяц\" = 00")
    @Severity(SeverityLevel.MINOR)
    void month00ValueTest() {
        TourBuyPage page = new TourBuyPage();
        var result = page
                .choosePaymentType(TourBuyPage.DEBIT)
                .fillPaymentData(APPROVED_CARD_NUMBER, "00", "25", "John Doe", "123")
                .pay()
                .getFormValidationErrors();
        assertNull(result.get(CARD_NUMBER_FIELD));
        assertNull(result.get(OWNER_FIELD));
        assertNull(result.get(CVV_FIELD));
        assertNull(result.get(YEAR_FIELD));
        assertEquals("Неверный формат", result.get(MONTH_FIELD));
    }

    @Test
    @DisplayName("Тест кейс 14: Поле \"Месяц\" = 13")
    @Severity(SeverityLevel.MINOR)
    void month13ValueTest() {
        TourBuyPage page = new TourBuyPage();
        var result = page
                .choosePaymentType(TourBuyPage.DEBIT)
                .fillPaymentData(APPROVED_CARD_NUMBER, "13", "25", "John Doe", "123")
                .pay()
                .getFormValidationErrors();
        assertNull(result.get(CARD_NUMBER_FIELD));
        assertNull(result.get(OWNER_FIELD));
        assertNull(result.get(CVV_FIELD));
        assertNull(result.get(YEAR_FIELD));
        assertEquals("Неверно указан срок действия карты", result.get(MONTH_FIELD));
    }


    @Test
    @DisplayName("Тест кейс 15: Поле \"Номер карты\" менее 16 цифр")
    @Severity(SeverityLevel.MINOR)
    void cardNumberLengthBelow16Test() {
        TourBuyPage page = new TourBuyPage();
        var result = page
                .choosePaymentType(TourBuyPage.DEBIT)
                .fillPaymentData("123", "13", "25", "John Doe", "123")
                .pay()
                .getFormValidationErrors();
        assertNull(result.get(MONTH_FIELD));
        assertNull(result.get(OWNER_FIELD));
        assertNull(result.get(CVV_FIELD));
        assertNull(result.get(YEAR_FIELD));
        assertEquals("Неверный формат", result.get(CARD_NUMBER_FIELD));
    }

    @Test
    @DisplayName("Тест кейс 16: Срок действия карты истек в этом году")
    @Severity(SeverityLevel.MINOR)
    void cardIsExpiredInCurrentYearTest() {
        TourBuyPage page = new TourBuyPage();
        var result = page
                .choosePaymentType(TourBuyPage.DEBIT)
                .fillPaymentData(APPROVED_CARD_NUMBER, "02", "24", "John Doe", "123")
                .pay()
                .getFormValidationErrors();
        assertNull(result.get(CARD_NUMBER_FIELD));
        assertNull(result.get(OWNER_FIELD));
        assertNull(result.get(CVV_FIELD));
        assertNull(result.get(YEAR_FIELD));
        assertEquals("Неверно указан срок действия карты", result.get(MONTH_FIELD));
    }

}
