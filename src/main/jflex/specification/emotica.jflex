/* JFlex example: partial Java language lexer specification */
import java_cup.runtime.*;

/**
 * This class is a simple example lexer.
 */
%%

%class Lexer
%unicode
%cup
%line
%column
%standalone
%debug

%{
  StringBuffer string = new StringBuffer();

  StringBuffer assignmentValue = new StringBuffer();

  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

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

  /* Variable */
  [A-Za-z_][A-Za-z0-9_]+     { return symbol(sym.VARIABLE); }

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