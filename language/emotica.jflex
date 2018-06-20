/* JFlex example: partial Java language lexer specification */
package ch.samuelblattner.ffhs.fac.emotica.parsing;

import java_cup.runtime.*;

/**
 * This class is a simple example lexer.
 */
%%

%public
%class EmoticaScanner
%unicode
%cup
%line
%column

%{
  StringBuffer string = new StringBuffer();

  StringBuffer assignmentValue = new StringBuffer();

  ComplexSymbolFactory symbolFactory = new ComplexSymbolFactory();

  private Symbol symbol(int type) {
    return symbolFactory.newSymbol(String.format("%d", type), type, new ComplexSymbolFactory.Location(yyline, yycolumn), new ComplexSymbolFactory.Location(yyline, yycolumn + 1));
  }
  private Symbol symbol(int type, Object value) {
    return symbolFactory.newSymbol(
        String.format("%d", type), type, new ComplexSymbolFactory.Location(yyline, yycolumn),
        new ComplexSymbolFactory.Location(yyline, yycolumn + 1),
        (value != null ? value : yytext())
        );
  }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

%eofval{
  return symbol(sym.EOF);
%eofval}


%state STRING

%%

<YYINITIAL> {

  ;                         { return symbol(sym.SEMICOLON); }

  \u23E9                    { return symbol(sym.RDARROW); }
  \u23ED                    { return symbol(sym.RDARROWSTOP); }
  \u26A1                    { return symbol(sym.FLASH); }
  \u270B                    { return symbol(sym.HALTHAND); }
  \u2753                    { return symbol(sym.QMARK); }
  \u27A1                    { return symbol(sym.RARROW); }

  \U01F300                  { return symbol(sym.SPIN); }
  \U01F4E2                  { return symbol(sym.SPEAKER); }
  \U01F4E4                  { return symbol(sym.BOX_OUT); }
  \U01F4E5                  { return symbol(sym.BOX_IN); }
  \U01F641                  { return symbol(sym.EMJ_SAD); }
  \U01F642                  { return symbol(sym.EMJ_SMILE); }

  \U01F449                  { return symbol(sym.RHAND); }
  \U01F4AC                  { string.setLength(0); yybegin(STRING); }

  \u2194\uFE0F               { return symbol(sym.LRARROW); }
  \u267B\uFE0F               { return symbol(sym.CYCLE); }

  /* Operators */
  \+                        { return symbol(sym.PLUS); }
  \-                        { return symbol(sym.MINUS); }
  \/                        { return symbol(sym.DIV); }
  \*                        { return symbol(sym.ASTERISK); }
  \^                        { return symbol(sym.HAT); }
  \%                        { return symbol(sym.PERCENT); }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
  {LineTerminator}                   { /* ignore */ }

  /* Variable */
  [A-Za-z_][A-Za-z0-9_]+     { return symbol(sym.VARIABLE, null); }

  /* Numbers */
  \d*\.?\d+                  { return symbol(sym.NUMBER); }
}


<STRING> {

    \U01F4AC                 { yybegin(YYINITIAL); return symbol(sym.STRING_LITERAL, string.toString()); }
    [^\n\r\U01F4AC\\]+       { string.append( yytext() ); }
    \\t                      { string.append('\t'); }
    \\n                      { string.append('\n'); }

    \\r                      { string.append('\r'); }
    \\\"                     { string.append('\"'); }
    \\                       { string.append('\\'); }
}

/* error fallback */
[^]                              { throw new Error("Illegal character <"+yytext()+">"); }