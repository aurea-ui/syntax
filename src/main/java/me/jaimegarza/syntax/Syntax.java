/*
 Syntax is distributed under the Revised, or 3-clause BSD license
 ===============================================================================
 Copyright (c) 1985, 2012, 2016, Jaime Garza
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the copyright holder nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ===============================================================================
*/
package me.jaimegarza.syntax;

import me.jaimegarza.syntax.env.Environment;
import me.jaimegarza.syntax.exception.AnalysisException;
import me.jaimegarza.syntax.exception.OutputException;
import me.jaimegarza.syntax.exception.ParsingException;
import me.jaimegarza.syntax.generator.AbstractCodeParser;
import me.jaimegarza.syntax.generator.SyntaxCodeParser;
//import me.jaimegarza.syntax.generator.CodeParser;
import me.jaimegarza.syntax.generator.CodeWriter;
import me.jaimegarza.syntax.generator.StructuralAnalyzer;
import me.jaimegarza.syntax.generator.TableGenerator;

/**
 * Main entry point to syntax
 *
 * Syntax is a compiler compiler written as a tool for my students in college for 
 * the compiler construction course.
 * 
 * I thought that having a yacc like syntax would help the introduction of 
 * LR parsers.  Over time I have added:
 * 
 * - Code to be generated into a lexical analizer (non-regex)
 * - Error messages
 * - Output in java
 * - Output in C
 * - Output for Delphi Pascal
 * - Translated to JAVA from its 1985 C codebase.
 * 
 * - Compile with LALR (yacc) or SLR, more compact, a little more restrictive
 * - Eject the output table in a compressed mode (yacc) or a matrix, for readability.
 * - Unlike yacc, the output is properly formated and readable.
 * 
 * TODO: P2-suport the concept of %external to be able to modularize a parser
 *       into multiple sub parsers.  The parsers may not be LR.  They can actually
 *       be LR (i.e. JavaCC, antlr).  Or they can be LR as well (Cup, BYacc -j).
 *       LR-LL parser combinations are also called LC (left corner) parsers and
 *       can be useful for complex grammars.<p>
 *       What I need is a mechanism to declare:
 *       <ol>
 *       <li>Calling the sub code as an action
 *       <li>Receiving and putting the return value in the stack as $$, $1, etc.
 *       <li>Triggering errors (perhaps on exceptions
 *       <li>naming objects with prefixes so that I can link to syntax parsers
 *       </ol>
 * TODO: P1-Include a regular expression mode for tokens (For LEX-like recognition)
 *       Lexical actions could then be entered in regex format with a predefined
 *       code structure 
 * 
 * @author jaimegarza@gmail.com
 *
 */
public class Syntax {

  private Environment environment;

  /**
   * Initialize syntax with the environment.
   * 
   * @param environment is the syntax environment
   */
  public Syntax(Environment environment) {
    this.environment = environment;
  }

  /**
   * Execute the phases as follows:
   * 
   * 1. Parse the file and create the rule structure
   * 2. Analyze and compute first and (for SLR) follow symbols
   * 3. Generate the states pertinent to the rules
   * 4. Output the components of the resulting code.
   * 
   * Uses the {@link #executeInternal()} routine.
   */
  public void execute() {
    try {
      executeInternal();
    } catch (AnalysisException e) {
      System.err.println("Internal error: " + e.getMessage());
      e.printStackTrace(System.err);
    } catch (ParsingException e) {
      System.err.println("Parsing error: " + e.getMessage());
      e.printStackTrace(System.err);
    } catch (OutputException e) {
      System.err.println("Output error: " + e.getMessage());
      e.printStackTrace(System.err);
    }
  }

  /**
   * Execute the phases as follows:
   * 
   * 1. Parse the file and create the rule structure
   * 2. Analyze and compute first and (for SLR) follow symbols
   * 3. Generate the states pertinent to the rules
   * 4. Output the components of the resulting code.
   * @throws ParsingException when the input file has issues.
   * @throws AnalysisException when the input grammar can be parsed but has issues.
   * @throws OutputException when the generation could not produce the output file
   */
  public void executeInternal() throws ParsingException, AnalysisException, OutputException {
    try {
      environment.reportWriter.preface();
      AbstractCodeParser parser = new SyntaxCodeParser(environment);
      StructuralAnalyzer analyzer = new StructuralAnalyzer(environment);
      TableGenerator generator = new TableGenerator(environment);
      CodeWriter writer = new CodeWriter(environment);
      if (environment.isTokenizerMode()) {
        parser.dumpTokens();
      } else {
        parser.execute();
        analyzer.execute();
        generator.execute();
        writer.execute();
      }
    } finally {
      environment.reportWriter.epilogue();
    }
  }

  /**
   * Entry point 
   * @param args command line arguments
   */
  public static void main(String args[]) {
    Environment environment = new Environment("Syntax", args);
    if (environment.isDebug()) {
      System.out.println("environment\n" + environment);
    }

    try {
      if (environment.isVerbose()) {
        System.out.println("Syntax");
      }
      Syntax syntaxTool = new Syntax(environment);
      syntaxTool.execute();
    } finally {
      if (environment.isVerbose()) {
        System.out.println("Done\n");
      }
      environment.release();
    }
  }

}
