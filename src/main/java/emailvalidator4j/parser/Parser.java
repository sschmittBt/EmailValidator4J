package emailvalidator4j.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import emailvalidator4j.lexer.EmailLexer;
import emailvalidator4j.lexer.TokenInterface;
import emailvalidator4j.lexer.Tokens;
import emailvalidator4j.parser.exception.ATEXTAfterCFWS;
import emailvalidator4j.parser.exception.ATEXTAfterComment;
import emailvalidator4j.parser.exception.CRLFAtEnd;
import emailvalidator4j.parser.exception.CRWithoutLF;
import emailvalidator4j.parser.exception.ConsecutiveCRLF;
import emailvalidator4j.parser.exception.ConsecutiveDots;
import emailvalidator4j.parser.exception.ExpectedCTEXT;
import emailvalidator4j.parser.exception.InvalidEmail;
import emailvalidator4j.parser.exception.UnclosedComment;
import emailvalidator4j.parser.exception.UnclosedDQUOTE;

abstract class Parser {
  protected List<Warnings> warnings = new ArrayList<>();
  protected EmailLexer     lexer;

  public Parser(EmailLexer lexer) {
    this.lexer = lexer;
  }

  public abstract void parse(String part) throws InvalidEmail;

  public List<Warnings> getWarnings() {
    return this.warnings;
  }

  protected void validateQuotedPair() {
    //only applies for emails like "\"@domain.com or "test\"@domain.com
    this.warnings.add(Warnings.DEPRECATED_QP);
  }

  protected boolean escaped() {
    return this.lexer.getPrevious().equals(Tokens.BACKSLASH) && !this.lexer.getCurrent().equals(Tokens.get("GENERIC"));
  }

  protected void parseComment() throws InvalidEmail {
    this.checkUnclosedComment();
    this.warnings.add(Warnings.COMMENT);

    while (!this.lexer.getCurrent().equals(Tokens.CLOSEPARENTHESIS)) {
      this.lexer.next();
    }

    if (this.lexer.isNextToken(Tokens.get("GENERIC"))) {
      throw new ATEXTAfterComment("");
    }

    if (this.lexer.isNextToken(Tokens.AT)) {
      this.warnings.add(Warnings.DEPRECATED_CFWS_NEAR_AT);
    }
  }

  protected void checkUnclosedComment() throws InvalidEmail {
    if (!this.lexer.find(Tokens.CLOSEPARENTHESIS)) {
      throw new UnclosedComment("Unclosed Comment");
    }
  }

  protected void checkConsecutiveDots() throws InvalidEmail {
    if (this.lexer.getCurrent().equals(Tokens.DOT) && this.lexer.isNextToken(Tokens.DOT)) {
      throw new ConsecutiveDots("Consecutive dots");
    }
  }

  protected boolean checkDoubleQuote(boolean hasClosingQuote) throws InvalidEmail {
    if (!this.lexer.getCurrent().equals(Tokens.DQUOTE)) {
      return hasClosingQuote;
    }

    if (hasClosingQuote) {
      return hasClosingQuote;
    }

    if (this.lexer.isNextToken(Tokens.get("GENERIC")) && this.lexer.getPrevious().equals(Tokens.get("GENERIC"))) {
      //TODO review this condition.
    }

    this.warnings.add(Warnings.RFC5321_QUOTEDSTRING);
    hasClosingQuote = this.lexer.find(Tokens.DQUOTE);
    if (!hasClosingQuote) {
      throw new UnclosedDQUOTE("Unclosed DQUOTE");
    }
    return hasClosingQuote;
  }

  protected boolean isFWS() {
    List<TokenInterface> FWSTokens = new ArrayList<TokenInterface>(
        Arrays.asList(Tokens.HTAB, Tokens.SP, Tokens.CR, Tokens.LF, Tokens.CRLF));

    return !this.escaped() && FWSTokens.contains(this.lexer.getCurrent());
  }

  protected void parseFWS() throws InvalidEmail {
    this.checkCRLFInFWS();
    if (this.lexer.getCurrent().equals(Tokens.CR)) {
      throw new CRWithoutLF("Found CR but no LF");
    }

    if (this.lexer.isNextToken(Tokens.get("GENERIC")) && !this.lexer.getPrevious().equals(Tokens.AT)) {
      throw new ATEXTAfterCFWS("ATEXT found after CFWS");
    }

    if (this.lexer.getCurrent().equals(Tokens.LF) || this.lexer.getCurrent().equals(Tokens.NUL)) {
      throw new ExpectedCTEXT("Expecting CTEXT");
    }

    if (this.lexer.isNextToken(Tokens.AT) || this.lexer.getPrevious().equals(Tokens.AT)) {
      this.warnings.add(Warnings.DEPRECATED_CFWS_NEAR_AT);
    } else {
      this.warnings.add(Warnings.CFWS_FWS);
    }
  }

  private void checkCRLFInFWS() throws InvalidEmail {
    if (!this.lexer.getCurrent().equals(Tokens.CRLF)) {
      return;
    }

    if (this.lexer.getCurrent().equals(Tokens.CRLF)) {
      if (this.lexer.isNextToken(Tokens.CRLF)) {
        throw new ConsecutiveCRLF("Consecutive CRLF");
      }
    }
    if (!this.lexer.isNextToken(new ArrayList<>(Arrays.asList(Tokens.SP, Tokens.HTAB)))) {
      throw new CRLFAtEnd("CRLF at the end");
    }
  }
}
