/*
Syntax is distibuted under the Revised, or 3-clause BSD license
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
    * Neither the name of Jaime Garza nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
===============================================================================
*/
package me.jaimegarza.syntax.model.graph;

import java.util.HashSet;
import java.util.Set;

import me.jaimegarza.syntax.model.graph.symbol.RegexSymbol;
import me.jaimegarza.syntax.util.FormattingPrintStream;

public class Dfa extends DirectedGraph<DfaNode> {

  public DfaNode newNode(Set<NfaNode> closure) {
    DfaNode node = new DfaNode(this, closure);
    nodes.add(node);
    return node;
  }
  
  public DfaNode findNodeByClosure(Set<NfaNode> closure) {
    for (DfaNode node: nodes) {
      if (node.eclosure().equals(closure)) {
        return node;
      }
    }
    return null;
  }
  
  public void generateFromNfa(Nfa graph) {
    // Create initial Dfa state
    for (NfaNode node: graph.getNodes()) {
      if (node.isStarting()) {
        Set<NfaNode> closure = node.eclosure();
        newNode(closure);
        break;
      }
    }
    
    // Create additional states
    for (int i = 0; i < nodes.size(); i++) {
      DfaNode dfaFromNode = nodes.get(i);
      Set<RegexSymbol> symbols = dfaFromNode.getTransitionSymbols();
      for (RegexSymbol symbol : symbols) {
        if (symbol.isEpsilon()) {
          continue;
        }
        Set<NfaNode> toNodes = dfaFromNode.getNfaTransitions(symbol);
        Set<NfaNode> toNodesWithClosure = new HashSet<>();
        boolean isFinal = false;
        for (NfaNode toNode : toNodes) {
          toNodesWithClosure.addAll(toNode.eclosure());
          for (NfaNode n : toNodesWithClosure) {
            if (n.isAccept()) {
              isFinal = true;
              break;
            }
          }
        }
        DfaNode dfaToNode = findNodeByClosure(toNodesWithClosure);
        if (dfaToNode == null) {
          dfaToNode = newNode(toNodesWithClosure);
          dfaToNode.setAccept(isFinal);
        }
        new Transition(dfaFromNode, dfaToNode, symbol);
      }
    }    
  }
  
  public void print(FormattingPrintStream out) {
    out.println("DFA");
    out.println("==================================================================");
    
    for (DfaNode n: nodes) {
      out.printf("%8d - ", n.getId());
      for (Transition t: n.getTransitions()) {
        out.printf("%s ", t.toString());
      }
      out.println();
    }
  }


}
