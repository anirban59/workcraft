/* Generated By:JavaCC: Do not edit this line. GenlibParser.java */
package org.workcraft.plugins.circuit.javacc;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

import org.workcraft.dom.Node;
import org.workcraft.util.Pair;
import org.workcraft.util.Triple;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.NotFoundException;

import org.workcraft.plugins.circuit.genlib.Library;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.Function;

public class GenlibParser implements GenlibParserConstants {

  final public Library parseGenlib() throws ParseException {
    List<Gate> gates;
    gates = parseGates();
        {if (true) return new Library(gates);}
    throw new Error("Missing return statement in function");
  }

  final public List<Gate> parseGates() throws ParseException {
    Gate gate;
    List<Gate> gates = new LinkedList<Gate>();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case GATE:
      case LATCH:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case GATE:
        gate = parseGate();
        break;
      case LATCH:
        gate = parseLatch();
        break;
      default:
        jj_la1[1] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
            gates.add(gate);
    }
        {if (true) return gates;}
    throw new Error("Missing return statement in function");
  }

  final public Gate parseGate() throws ParseException {
    String name;
    Function function;
    jj_consume_token(GATE);
    name = parseName();
    jj_consume_token(NUMERAL);
    function = parseFunction();
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PIN:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_2;
      }
      parsePin();
    }
        {if (true) return new Gate(name, function, null, false);}
    throw new Error("Missing return statement in function");
  }

  final public Gate parseLatch() throws ParseException {
    String name;
    Function function;
    String next;
    jj_consume_token(LATCH);
    name = parseName();
    jj_consume_token(NUMERAL);
    function = parseFunction();
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PIN:
        ;
        break;
      default:
        jj_la1[3] = jj_gen;
        break label_3;
      }
      parsePin();
    }
    next = parseSeq();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case CONTROL:
      parseControl();
      break;
    default:
      jj_la1[4] = jj_gen;
      ;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case CONSTRAINT:
      parseConstraint();
      break;
    default:
      jj_la1[5] = jj_gen;
      ;
    }
        {if (true) return new Gate(name, function, next, false);}
    throw new Error("Missing return statement in function");
  }

  final public String parseName() throws ParseException {
    Token nameToken;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NAME:
      nameToken = jj_consume_token(NAME);
                {if (true) return nameToken.image;}
      break;
    case STRING:
      nameToken = jj_consume_token(STRING);
                String s = nameToken.image;
            {if (true) return s.substring(1, s.length()-1);}
      break;
    default:
      jj_la1[6] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public Function parseFunction() throws ParseException {
    Token nameToken;
    Token formulaToken;
    nameToken = jj_consume_token(NAME);
    formulaToken = jj_consume_token(FORMULA);
        String formula = formulaToken.image.replaceAll("^=", "").replaceAll(";$", "");
        {if (true) return new Function(nameToken.image, formula);}
    throw new Error("Missing return statement in function");
  }

  final public void parsePin() throws ParseException {
    jj_consume_token(PIN);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NAME:
      jj_consume_token(NAME);
      break;
    case ANY_NAME:
      jj_consume_token(ANY_NAME);
      break;
    default:
      jj_la1[7] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INV:
    case NONINV:
    case UNKNOWN:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case INV:
        jj_consume_token(INV);
        break;
      case NONINV:
        jj_consume_token(NONINV);
        break;
      case UNKNOWN:
        jj_consume_token(UNKNOWN);
        break;
      default:
        jj_la1[8] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default:
      jj_la1[9] = jj_gen;
      ;
    }
    jj_consume_token(NUMERAL);
    jj_consume_token(NUMERAL);
    jj_consume_token(NUMERAL);
    jj_consume_token(NUMERAL);
    jj_consume_token(NUMERAL);
    jj_consume_token(NUMERAL);
  }

  final public String parseSeq() throws ParseException {
    Token nextToken = null;
    jj_consume_token(SEQ);
    jj_consume_token(NAME);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NAME:
      nextToken = jj_consume_token(NAME);
      break;
    case ANY:
      jj_consume_token(ANY);
      break;
    default:
      jj_la1[10] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ACTIVE_LOW:
      jj_consume_token(ACTIVE_LOW);
      break;
    case ACTIVE_HIGH:
      jj_consume_token(ACTIVE_HIGH);
      break;
    case RISING_EDGE:
      jj_consume_token(RISING_EDGE);
      break;
    case FALLING_EDGE:
      jj_consume_token(FALLING_EDGE);
      break;
    case ASYNCH:
      jj_consume_token(ASYNCH);
      break;
    default:
      jj_la1[11] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
        {if (true) return ((nextToken == null) ? null : nextToken.image);}
    throw new Error("Missing return statement in function");
  }

  final public void parseControl() throws ParseException {
    jj_consume_token(CONTROL);
    jj_consume_token(NAME);
    jj_consume_token(NUMERAL);
    jj_consume_token(NUMERAL);
    jj_consume_token(NUMERAL);
    jj_consume_token(NUMERAL);
    jj_consume_token(NUMERAL);
    jj_consume_token(NUMERAL);
  }

  final public void parseConstraint() throws ParseException {
    jj_consume_token(CONSTRAINT);
    jj_consume_token(NAME);
    jj_consume_token(NUMERAL);
    jj_consume_token(NUMERAL);
  }

  /** Generated Token Manager. */
  public GenlibParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[12];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0xc0,0xc0,0x100,0x100,0x40000,0x80000,0x600000,0x2200000,0xe00,0xe00,0x202000,0x13c000,};
   }

  /** Constructor with InputStream. */
  public GenlibParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public GenlibParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new GenlibParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 12; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 12; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public GenlibParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new GenlibParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 12; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 12; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public GenlibParser(GenlibParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 12; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(GenlibParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 12; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[28];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 12; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 28; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}
