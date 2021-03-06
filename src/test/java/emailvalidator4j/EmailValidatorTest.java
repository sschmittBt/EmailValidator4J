package emailvalidator4j;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class EmailValidatorTest {

    @DataProvider
    public static Object[][] invalidEmailProvider() {
        return new Object[][]{
                {"nolocalpart.com"},
                {"test@example.com test"},
                {"user  name@example.com"},
                {"user   name@example.com"},
                {"example.@example.co.uk"},
                {"example@example@example.co.uk"},
                {"(test_exampel@example.fr}"},
                {"example(example)example@example.co.uk"},
                {".example@localhost"},
                {"ex\\ample@localhost"},
                {"example@local\\host"},
                {"example@localhost."},
                {"user name@example.com"},
                {"username@ example . com"},
                {"example@(fake}.com"},
                {"example@(fake.com"},
                {"username@example,com"},
                {"usern,ame@example.com"},
                {"user[na]me@example.com"},
                {"\"\"\"@iana.org"},
                {"\"\\\"@iana.org"},
                {"\"test\"test@iana.org"},
                {"\"test\"\"test\"@iana.org"},
                {"\"test\".\"test\"@iana.org"},
                {"\"test\".test@iana.org"},
                {"\"test\"" + String.valueOf(Character.toChars(0)) + "@iana.org"},
                {String.format("\"test\\%s@iana.org", "\"")},
                {"\r\ntest@iana.org"},
                {"\r\n test@iana.org"},
                {"\r\n \r\ntest@iana.org"},
                {"\r\n \r\ntest@iana.org"},
                {"\r\n \r\n test@iana.org"},
                {"test@iana.org \r\n"},
                {"test@iana.org \r\n "},
                {"test@iana.org \r\n \r\n"},
                {"test@iana.org \r\n\r\n"},
                {"test@iana.org  \r\n\r\n "},
                {"test@iana/icann.org"},
                {"test@foo;bar.com"},
                {(char) 1 + "a@test.com"}
        };
    }

    @Test
    @UseDataProvider("invalidEmailProvider")
    public void isInvalidEmail(String email) {
        EmailValidator validator = new EmailValidator();
        Assert.assertFalse(email + " is an invalid email", validator.isValid(email));
    }

    @DataProvider
    public static Object[][] validEmailsProvider() {
        return new Object[][] {
                {"example@example.com"},
                {"example@example.co.uk"},
                {"example_underscore@example.fr"},
                {"example@localhost"},
                {"exam'ple@example.com"},
                {String.format("exam\\%sple@example.com", " ")},
                {"example((example))@fakedfake.co.uk"},
                {"example@faked(fake).co.uk"},
                {"example+@example.com"},
                {"инфо@письмо.рф"},
                {"\"username\"@example.com"},
                {"\"user,name\"@example.com"},
                {"\"user name\"@example.com"},
                {"\"user@name\"@example.com"},
                {"\"\\a\"@iana.org"},
                {"\"test\\ test\"@iana.org"},
                {"\"\"@iana.org"},
                {String.format("\"\\%s\"@iana.org", "\"")},
        };
    }

    @Test
    @UseDataProvider("validEmailsProvider")
    public void isValidEmail(String validEmail) {
        EmailValidator validator = new EmailValidator();
        Assert.assertTrue(validEmail + " is a valid email", validator.isValid(validEmail));
    }

    @Test
    public void validEmailHasNoWarnings() {
        EmailValidator validator = new EmailValidator();
        validator.isValid("test@example.com");
        Assert.assertFalse(validator.hasWarnings());
    }

    @Test
    public void validEmailHasWarnings() {
        EmailValidator validator = new EmailValidator();
        validator.isValid("test@[127.0.0.0]");
        Assert.assertTrue(validator.hasWarnings());
    }

    @Test
    public void warningsAreExposed() {
        EmailValidator validator = new EmailValidator();
        validator.isValid("test@[127.0.0.0]");
        Assert.assertFalse(validator.getWarnings().isEmpty());
    }
}
